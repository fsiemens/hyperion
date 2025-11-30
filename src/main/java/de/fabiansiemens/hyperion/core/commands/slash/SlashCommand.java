package de.fabiansiemens.hyperion.core.commands.slash;

import de.fabiansiemens.hyperion.core.commands.ICommand;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface SlashCommand extends ICommand {
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException;
	public SlashCommandData getSlashCommandData();
}
