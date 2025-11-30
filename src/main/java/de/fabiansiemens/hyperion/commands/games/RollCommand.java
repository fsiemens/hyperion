package de.fabiansiemens.hyperion.commands.games;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.commands.universal.UniversalCommand;
import de.fabiansiemens.hyperion.core.events.LegacyCommandEvent;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.interactions.roll.CommonRollInteractionChain;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "games/Roll.json")
public class RollCommand extends SlashCommandBase implements UniversalCommand {
	
	public RollCommand(ApplicationContext context) throws IllegalArgumentException, IOException {
		super(RollCommand.class, context);
	}

	@Override
	public void performLegacyCommand(LegacyCommandEvent event) throws UiParseException {
		CommonRollInteractionChain interactionChain = context.getBean(CommonRollInteractionChain.class);
		interactionChain.onRollLegacyCommandInteraction(event);
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		CommonRollInteractionChain interactionChain = context.getBean(CommonRollInteractionChain.class);
		interactionChain.onRollSlashCommandInteraction(event);
	}

	@Override
	public boolean shouldDeleteInitMessage() {
		return super.getCommandInfo().getLegacyData().isDeleteInit();
	}
}
