package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioEventListenerImpl;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioPlayerEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.lavalink.LavalinkEventListener;

public class LavaplayerEventListener extends AudioEventAdapter implements JointAudioEventListener {
	
	private final AudioPlayerEventListener jointListener;
	
	public LavaplayerEventListener(AudioService audioService) {
		this.jointListener = new AudioEventListenerImpl(audioService);
	}
	
	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		jointListener.onTrackStart(new LavaplayerAudioPlayer(player), track == null ? null : new LavaplayerAudioTrack(track));
	}
	
	@Override
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
		jointListener.onTrackStuck(new LavaplayerAudioPlayer(player), track == null ? null : new LavaplayerAudioTrack(track));
	}
	
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		jointListener.onTrackEnd(new LavaplayerAudioPlayer(player), track == null ? null : new LavaplayerAudioTrack(track), endReason);
	}
	
	@Override
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		jointListener.onTrackException(new LavaplayerAudioPlayer(player), track == null ? null : new LavaplayerAudioTrack(track), new LavaplayerException(exception));
	}
	
	@Override
	public void onPlayerPause(AudioPlayer player) {
		jointListener.onPlayerPause(new LavaplayerAudioPlayer(player));
	}
	
	@Override
	public void onPlayerResume(AudioPlayer player) {
		jointListener.onPlayerResume(new LavaplayerAudioPlayer(player));
	}

	@Override
	public LavalinkEventListener asLavalinkListener() {
		throw new UnsupportedOperationException("Cannot convert LavaplayerEventListener to LavalinkEventListener");
	}

	@Override
	public LavaplayerEventListener asLavaplayerListener() {
		return this;
	}

	public void onGenericUpdateEvent(AudioPlayer player, AudioTrack track) {
		jointListener.onGenericUpdateEvent(new LavaplayerAudioPlayer(player), track == null ? null : new LavaplayerAudioTrack(track));
	}
}
