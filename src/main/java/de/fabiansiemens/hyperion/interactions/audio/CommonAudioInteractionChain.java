package de.fabiansiemens.hyperion.interactions.audio;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.AuthorizationException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.PlayerPanelView;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.interactions.InteractionChainBase;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.modals.Modal;

@Primary
@InteractionChain
public class CommonAudioInteractionChain extends InteractionChainBase {

	@Value("${lavalink.search-prefix.youtube}")
	public String ytSearchPrefix;
	
	@Value("${lavalink.search-prefix.youtube-music}")
	public String ytmSearchPrefix;
	
	protected final AudioService audioService;
	protected final LocalizedExpressionService les;
	
	protected static final String QUEUE_MODAL_PATH = "audio/AudioSearchModal.xml";
	
	public CommonAudioInteractionChain(ApplicationContext context, AudioService audioService, LocalizedExpressionService les) {
		super(context);
		this.audioService = audioService;
		this.les = les;
	}

	public void onMusicPlayerCommandInteraction(SlashCommandInteractionEvent event) throws UiParseException {
		if(event.getGuild() == null)
			event.reply(les.getLocalizedExpression("error.common.guild-only", event.getGuild())).setEphemeral(true).queue();
		
		if(event.getGuild().getSelfMember().getVoiceState().getChannel() != null 
				&& event.getMember().getVoiceState().getChannel() == null 
				&& !event.getMember().getVoiceState().getChannel().equals(event.getGuild().getSelfMember().getVoiceState().getChannel())) {
			event.reply(les.getLocalizedExpression("error.play.not-same-channel", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Message message = event.deferReply(false).useComponentsV2().complete().retrieveOriginal().complete();
//		Arguments args = AudioPlayerPanelHandler.getPlayerArguments(audioService.getPlayer(event.getGuild()), null, audioService.getScheduler().getQueue(audioService.getPlayer(event.getGuild())));
//		Container musicPlayerPanel = uiProvider.panelOf(AudioPlayerPanelHandler.AUDIO_PLAYER_PANEL_PATH, event.getGuild(), args);
//		Message message = event.replyComponents(musicPlayerPanel).useComponentsV2().complete().retrieveOriginal().complete();
		audioService.registerPlayerPanel(message);
		audioService.refreshPlayers(event.getGuild());
	}
	
	@InteractionCallback(id = "b.audio.search")
	public void onSearchButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AuthorizationException, UiParseException {
		authorize(event, false);
		Modal modal = uiProvider.modalOf(QUEUE_MODAL_PATH, event.getGuild());
		event.replyModal(modal).queue();
		audioService.registerPlayerPanel(event.getMessage());
	}
	
	@InteractionCallback(id = "m.audio.search")
	public void onQueueModalInteraction(ModalInteractionEvent event, Arguments args) {
		event.deferReply(true).queue();
		String source = event.getValue("source").getAsStringList().get(0);
		String query = event.getValue("query").getAsString();
		boolean isSearch = false;
		String identifier;
		try {
			identifier = switch(source) {
				case "yt":
//					if(query.matches("https?:\\/\\/(.+\\.)?youtu[\\/\\S]+"))
//						yield query;
					
					isSearch = true;
					yield ytSearchPrefix + ":" + query;
				case "ytm":
					if(query.matches("https?:\\/\\/music.youtu[\\/\\S]+"))
						yield query;
					
					isSearch = true;
					yield ytmSearchPrefix + ":" + query;
				case "ttv":
				case "sp":
				case "sc":
				case "bc":
					yield query;
				case "loc":
				default:
					throw new Exception("error.play.invalid-source");
			};
		}
		catch(Exception e) {
			event.reply(les.getLocalizedExpression(e.getLocalizedMessage(), event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
//		if(event.getGuild().getSelfMember().getVoiceState().getChannel() == null)
//			audioService.join(event.getMember().getVoiceState().getChannel());
		
		audioService.loadItem(event.getHook(), identifier, isSearch);
	}
	
	@InteractionCallback(id = "b.audio.pause")
	public void onPauseButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AuthorizationException {
		event.deferEdit().queue();
		authorize(event, true);
		
		if(audioService.isPaused(event.getGuild()))
			audioService.resume(event.getGuild());
		else
			audioService.pause(event.getGuild());
		audioService.registerPlayerPanel(event.getMessage());
	}
	
	@InteractionCallback(id = "b.audio.skip")
	public void onSkipButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AuthorizationException {
		event.deferEdit().queue();
		authorize(event, true);
		
		audioService.skip(event.getGuild());
		audioService.registerPlayerPanel(event.getMessage());
	}
	
	@InteractionCallback(id = "b.audio.stop")
	public void onStopButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AuthorizationException {
		event.deferEdit().queue();
		authorize(event, true);
		
		audioService.stop(event.getGuild());
		audioService.registerPlayerPanel(event.getMessage());
	}
	
	@Deprecated
	@InteractionCallback(id = "b.audio.refresh")
	public void onRefreshButtonInteraction(ButtonInteractionEvent event, Arguments args) throws AuthorizationException {
		event.deferEdit().queue();
		audioService.registerPlayerPanel(event.getMessage());
		audioService.refreshPlayers(event.getGuild());
	}
	
	@InteractionCallback(id = "b.audio.back")
	public void onAudioBackButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		event.deferEdit().queue();
		audioService.registerPlayerPanel(event.getMessage());
		audioService.setPanelView(event.getMessage(), PlayerPanelView.PLAYER);
		audioService.refreshPlayers(event.getGuild());
	}
	
	private void authorize(ButtonInteractionEvent event, boolean requiresBotInChannel) throws AuthorizationException {
		if(event.getGuild() == null)
			throw new AuthorizationException("error.common.guild-only");
		
		authorize(event.getMember(), event.getGuild().getSelfMember().getVoiceState(), requiresBotInChannel);
	}
	
	private void authorize(Member member, GuildVoiceState vcState, boolean requiresBotInChannel) throws AuthorizationException {
		if((vcState.getChannel() == null && member.getVoiceState().getChannel() != null && !requiresBotInChannel)
			|| (vcState.getChannel() != null && Objects.equals(vcState.getChannel(), member.getVoiceState().getChannel())))
			return;
		
		//Improve Error response for case user in channel but bot not and requiresBotInChannel = true
		throw new AuthorizationException("error.play.not-same-channel");
	}
}
