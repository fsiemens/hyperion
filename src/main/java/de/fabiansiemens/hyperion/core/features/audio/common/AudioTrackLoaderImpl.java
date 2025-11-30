package de.fabiansiemens.hyperion.core.features.audio.common;

import java.util.concurrent.TimeUnit;

import de.fabiansiemens.hyperion.core.features.audio.AudioPlayerPanelHandler;
import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
@AllArgsConstructor
public class AudioTrackLoaderImpl implements JointAudioTrackLoader {
	
	private LocalizedExpressionService les;
	private InteractionHook hook;
	private AudioService audioService;
	private JointAudioPlayer player;
	private boolean noPlaylists;
	
	@Override
	public void onTrackLoaded(JointAudioTrack track) {
		log.info("Found Track: {}", track.getInfo().identifier );
		Guild guild = hook.getInteraction().getGuild();
		AudioChannel channel = hook.getInteraction().getMember().getVoiceState().getChannel();
		
		if(channel == null) {
			hook.sendMessage(les.getLocalizedExpression("error.play.not-in-vc", guild)).setEphemeral(true).queue();
			return;
		}
		
		if(guild.getSelfMember().getVoiceState().getChannel() != null && !guild.getSelfMember().getVoiceState().getChannel().equals(channel)) {
			hook.sendMessage(les.getLocalizedExpression("error.play.already-in-use", guild)).setEphemeral(true).queue();
			return;
		}	
		
		audioService.join(channel);
		audioService.getScheduler().loadTrack(player, track);
		audioService.refreshPlayers(guild);
		
		Arguments args = AudioPlayerPanelHandler.getPlayerArguments(Arguments.empty(), player, track, audioService.getScheduler().getQueue(player));
		hook.sendMessage(les.getLocalizedExpression("success.play.queued-single", guild, args)).setEphemeral(true).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}

	@Override
	public void onPlaylistLoaded(JointAudioPlaylist playlist) {
		log.info("Found Playlist: {}", playlist.getName());
		if(noPlaylists) {
			onTrackLoaded(playlist.getTracks().getFirst());
			return;
		}
		
		Guild guild = hook.getInteraction().getGuild();
		AudioChannel channel = hook.getInteraction().getMember().getVoiceState().getChannel();
		
		if(channel == null){
			hook.sendMessage(les.getLocalizedExpression("error.play.not-in-vc", guild)).setEphemeral(true).queue();
			return;
		}
		
		if(guild.getSelfMember().getVoiceState().getChannel() != null && !guild.getSelfMember().getVoiceState().getChannel().equals(channel)) {
			hook.sendMessage(les.getLocalizedExpression("error.play.already-in-use", guild)).setEphemeral(true).queue();
			return;
		}	
		
		audioService.join(channel);
		audioService.getScheduler().loadPlaylist(player, playlist);
		audioService.refreshPlayers(guild);
		
		Arguments args = Arguments.of("title", playlist.getName())
//				.put("source", playlist.getTracks().getFirst().getInfo().)
				.put("amount", playlist.getTracks().size());
		hook.sendMessage(les.getLocalizedExpression("success.play.queued-multiple", guild, args)).setEphemeral(true).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}

	@Override
	public void noMatches() {
		log.debug("Found no matches");
		Guild guild = hook.getInteraction().getGuild();
		audioService.refreshPlayers(guild);
		hook.sendMessage(les.getLocalizedExpression("error.play.no-matches", guild)).setEphemeral(true).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}

	@Override
	public void loadFailed(JointAudioException exception) {
		log.debug("Load failed: {}", exception.getLocalizedMessage() );
		Guild guild = hook.getInteraction().getGuild();
		audioService.refreshPlayers(guild);
		Arguments args = Arguments.of("reason", exception.getLocalizedMessage());
		hook.sendMessage(les.getLocalizedExpression("error.play.load-failed", guild, args)).setEphemeral(true).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}

	public void onSearchResultLoaded(JointAudioSearchResult searchResult) {
		log.info("Search Result Loaded: '{}'", searchResult.getTracks().getFirst().getInfo().title);
		onTrackLoaded(searchResult.getTracks().getFirst());	//TODO give user the option to choose
	}

}
