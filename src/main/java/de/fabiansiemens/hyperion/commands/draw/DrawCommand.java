package de.fabiansiemens.hyperion.commands.draw;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.interactions.draw.DrawInteractionChain;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "draw/Draw.json")
public class DrawCommand extends SlashCommandBase {

	public DrawCommand(ApplicationContext context) throws IOException, IllegalArgumentException {
		super(DrawCommand.class, context);
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		DrawInteractionChain interactionChain = context.getBean(DrawInteractionChain.class);
		interactionChain.onDrawCommand(event);
	}
}
