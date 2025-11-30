package de.fabiansiemens.hyperion.commands.audio;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.interactions.audio.CommonAudioInteractionChain;
import lombok.NonNull;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "audio/Player.json")
public class PlayerCommand extends SlashCommandBase {

	public PlayerCommand(@NonNull ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(PlayerCommand.class, context);
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		CommonAudioInteractionChain interactionChain = context.getBean(CommonAudioInteractionChain.class);
		interactionChain.onMusicPlayerCommandInteraction(event);
	}

}
