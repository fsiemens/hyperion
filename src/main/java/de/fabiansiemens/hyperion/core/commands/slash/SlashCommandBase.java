package de.fabiansiemens.hyperion.core.commands.slash;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.commands.CommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slf4j
public abstract class SlashCommandBase extends CommandBase implements SlashCommand {

	public <T> SlashCommandBase(@NonNull Class<T> clazz, @NonNull ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(clazz, context);
	}

	@Override
	public abstract void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException;

	@Override
	public SlashCommandData getSlashCommandData() {
		return super.getCommandInfo().getData();
	}

}
