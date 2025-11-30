package de.fabiansiemens.hyperion.commands.misc;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "misc/Birthday.json")
public class BirthdayCommand extends SlashCommandBase {

	public BirthdayCommand(ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(BirthdayCommand.class, context);
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		//TODO Birthday Command
	}

}
