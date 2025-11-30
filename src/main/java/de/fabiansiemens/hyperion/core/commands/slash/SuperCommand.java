package de.fabiansiemens.hyperion.core.commands.slash;

import java.util.Map;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface SuperCommand extends SlashCommand {
	public void onSubcommandActivation(SlashCommandInteractionEvent event);
	public Map<String, SlashCommand> getSubcommands();
	public void addSubcommand(String group, SlashCommand command);
}
