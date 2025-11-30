package de.fabiansiemens.hyperion.core.commands.slash;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;

import com.google.common.collect.ImmutableMap;

import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;

@Slf4j
public class SuperCommandBase extends SlashCommandBase implements SuperCommand {

	private Map<String, SubcommandGroupData> groupedSubcommands;
	private List<SubcommandData> ungroupedSubcommands;
	private Map<String,SlashCommand> allSubcommands;
	
	public <T> SuperCommandBase(Class<T> clazz, ApplicationContext context) throws IllegalArgumentException, IOException {
		super(clazz, context);
		this.ungroupedSubcommands = new LinkedList<>();
		this.groupedSubcommands = new ConcurrentHashMap<>();
		this.allSubcommands = new ConcurrentHashMap<>();
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		onSubcommandActivation(event);
		String path = event.getFullCommandName().substring(getName().length()).strip();
		SlashCommand command = allSubcommands.get(path);
		
		if(command == null) {
			event.reply(les.getLocalizedExpression("error.commands.not-found", event.getGuild())).queue();
			return;
		}
		
		command.performSlashCommand(event);
	}
	
	public void onSubcommandActivation(SlashCommandInteractionEvent event) {}

	/**
	 * Returns a immutable copy of the subcommands map
	 */
	@Override
	public Map<String, SlashCommand> getSubcommands() {
		return ImmutableMap.copyOf(this.allSubcommands);
	}

	@Override
	public void addSubcommand(String group, SlashCommand command) {
		this.allSubcommands.put((group + " " + command.getName()).strip(), command);
		
		if(group.isBlank()) {
			ungroupedSubcommands.add(SubcommandData.fromData(command.getSlashCommandData().toData()));
			return;
		}
		
		SubcommandGroupData groupData = groupedSubcommands.getOrDefault(group, new SubcommandGroupData(group, group));
		SubcommandData subData = SubcommandData.fromData(command.getSlashCommandData().toData());
		groupData.addSubcommands(subData);
		groupedSubcommands.put(group, groupData);
	}
	
	@Override
	public SlashCommandData getSlashCommandData() {
		SlashCommandData commandData = super.getSlashCommandData();
		commandData.removeSubcommandGroups(in -> true);
		commandData.removeSubcommands(in -> true);
		
		commandData.addSubcommandGroups(groupedSubcommands.values());
		commandData.addSubcommands(ungroupedSubcommands);
		
		return commandData;
	}
}
