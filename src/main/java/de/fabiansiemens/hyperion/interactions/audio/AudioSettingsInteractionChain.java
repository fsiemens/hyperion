package de.fabiansiemens.hyperion.interactions.audio;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.PlayerPanelView;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@InteractionChain
public class AudioSettingsInteractionChain extends CommonAudioInteractionChain {

	public AudioSettingsInteractionChain(ApplicationContext context, AudioService audioService,
			LocalizedExpressionService les) {
		super(context, audioService, les);
	}

	@InteractionCallback(id = "b.audio.settings")
	public void onAudioSettingsButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		event.deferEdit().queue();
		audioService.registerPlayerPanel(event.getMessage());
		audioService.setPanelView(event.getMessage(), PlayerPanelView.SETTINGS);
		audioService.refreshPlayers(event.getGuild());
	}
	
	@InteractionCallback(id = "b.audio.vol")
	public void onAudioVolumeButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException {
		event.deferEdit().queue();
		audioService.registerPlayerPanel(event.getMessage());
		audioService.setPanelView(event.getMessage(), PlayerPanelView.SETTINGS);
		
		int step = args.get("step").asInteger();
		int currentVolume = audioService.getVolume(event.getGuild());
		int volume = currentVolume + step;
		
		audioService.setVolume(event.getGuild(), volume);
		audioService.refreshPlayers(event.getGuild());
	}
}
