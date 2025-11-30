package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.fabiansiemens.hyperion.core.annotations.SubscribeNodeEvent;
import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioEventListenerImpl;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioPlayerEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.lavaplayer.LavaplayerEventListener;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import dev.arbjerg.lavalink.client.event.EmittedEvent;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.client.event.StatsEvent;
import dev.arbjerg.lavalink.client.event.TrackEndEvent;
import dev.arbjerg.lavalink.client.event.TrackExceptionEvent;
import dev.arbjerg.lavalink.client.event.TrackStartEvent;
import dev.arbjerg.lavalink.client.event.TrackStuckEvent;
import dev.arbjerg.lavalink.client.event.WebSocketClosedEvent;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
@AllArgsConstructor
public class LavalinkEventListener implements JointAudioEventListener {

	private final JDAManager jdaManager;
	private final AudioService audioService;
	private final AudioPlayerEventListener jointListener;
	
	public LavalinkEventListener(JDAManager jdaManager, AudioService audioService) {
		this.jdaManager = jdaManager;
		this.audioService = audioService;
		this.jointListener = new AudioEventListenerImpl(audioService);
	}
	
	public void onGenericUpdateEvent(Guild guild, Track track) {
		JointAudioPlayer player = audioService.getPlayer(guild);
		this.jointListener.onGenericUpdateEvent(player, track == null ? null : new LavalinkAudioTrack(track));
	}
	
	@SubscribeNodeEvent(clazz = ReadyEvent.class)
	public void onNodeReady(ReadyEvent readyEvent) {
		log.info("Node On Ready");
	}
	
	@SubscribeNodeEvent(clazz = StatsEvent.class)
	public void onNodeStats(StatsEvent statsEvent) {
		//log.info("Node On Stats");
	}
	
	@SubscribeNodeEvent(clazz = EmittedEvent.class)
	public void onNodeEmitted(EmittedEvent emittedEvent) {
		Guild guild = jdaManager.getJDA().getGuildById(emittedEvent.getGuildId());
		
		if(guild == null)
			return;
		
		JointAudioPlayer player = audioService.getPlayer(guild);
		if(emittedEvent instanceof TrackStartEvent event) {
			jointListener.onTrackStart(player, new LavalinkAudioTrack(event.getTrack()));
		}
		else if(emittedEvent instanceof TrackEndEvent event) {
			jointListener.onTrackEnd(player, new LavalinkAudioTrack(event.getTrack()), mapEndReason(event.getEndReason()));
		}
		else if(emittedEvent instanceof TrackExceptionEvent event) {
			jointListener.onTrackException(player, new LavalinkAudioTrack(event.getTrack()), new LavalinkException(event.getException()));
		}
		else if(emittedEvent instanceof TrackStuckEvent event) {
			jointListener.onTrackStuck(player, new LavalinkAudioTrack(event.getTrack()));
		}
		else if(emittedEvent instanceof WebSocketClosedEvent) {
			//Add WebSocketCloseEvent Processing here
		}
	}
	
	public void onPlayerPaused(Guild guild) {
		JointAudioPlayer player = audioService.getPlayer(guild);
		jointListener.onPlayerPause(player);
	}
	
	public void onPlayerResumed(Guild guild) {
		JointAudioPlayer player = audioService.getPlayer(guild);
		jointListener.onPlayerResume(player);
	}
	
	@Override
	public LavalinkEventListener asLavalinkListener() throws UnsupportedOperationException {
		return this;
	}

	@Override
	public LavaplayerEventListener asLavaplayerListener() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot convert LavalinkEventListener to LavaplayerEventListener");
	}

	private AudioTrackEndReason mapEndReason(
			dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {

		switch(endReason) {
		case CLEANUP:
			return AudioTrackEndReason.CLEANUP;
		case FINISHED:
			return AudioTrackEndReason.FINISHED;
		case LOAD_FAILED:
			return AudioTrackEndReason.LOAD_FAILED;
		case REPLACED:
			return AudioTrackEndReason.REPLACED;
		case STOPPED:
		default:
			return AudioTrackEndReason.STOPPED;
		}
	}
}
