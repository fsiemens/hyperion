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
public class AudioQueueInteractionChain extends CommonAudioInteractionChain {

	public AudioQueueInteractionChain(ApplicationContext context, AudioService audioService,
			LocalizedExpressionService les) {
		super(context, audioService, les);
	}
	
	@InteractionCallback(id = "b.audio.queue")
	public void onAudioQueueButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		event.deferEdit().queue();
		audioService.registerPlayerPanel(event.getMessage());
		audioService.setPanelView(event.getMessage(), PlayerPanelView.QUEUE);
		audioService.refreshPlayers(event.getGuild(), Arguments.of("page", 0));
	}
	
	@InteractionCallback(id = "b.audio.queue.select")
	public void onAudioQueueSelectButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException {
		int selectedPos = args.get("pos").asInteger();
		
		event.deferEdit().queue();
		audioService.setPanelQueueSelect(event.getMessage(), selectedPos);
	}
	
	@InteractionCallback(id = "b.audio.queue.page-forward")
	public void onAudioQueuePageForwardButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException {
		int page = args.get("page").asInteger();
		event.deferEdit().queue();
		args.put("page", ++page);
		audioService.refreshPlayers(event.getGuild(), args);
	}
	
	@InteractionCallback(id = "b.audio.queue.page-back")
	public void onAudioQueuePageBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException {
		int page = args.get("page").asInteger();
		event.deferEdit().queue();
		args.put("page", --page);
		audioService.refreshPlayers(event.getGuild(), args);
	}
}
