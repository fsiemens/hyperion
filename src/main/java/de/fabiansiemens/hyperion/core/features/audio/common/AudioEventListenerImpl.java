package de.fabiansiemens.hyperion.core.features.audio.common;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AudioEventListenerImpl implements AudioPlayerEventListener {
	
	private final AudioService audioService;
	
	@Override
	public void onPlayerPause(JointAudioPlayer player) {
		audioService.onPlayerPause(player);
	}
	
	@Override
	public void onPlayerResume(JointAudioPlayer player) {
		audioService.onPlayerResume(player);
	}

	@Override
	public void onTrackStart(JointAudioPlayer player, JointAudioTrack track) {
		audioService.onTrackStart(player, track);
	}

	@Override
	public void onTrackStuck(JointAudioPlayer player, JointAudioTrack track) {
		audioService.onTrackStuck(player, track);
		audioService.skip(player);
	}
	
	@Override
	public void onTrackEnd(JointAudioPlayer player, JointAudioTrack track, AudioTrackEndReason endReason) {
		if(!endReason.mayStartNext) {
			audioService.onTrackEnd(player, track, endReason);
			return;
		}
		
		audioService.skip(player);
	}

	@Override
	public void onTrackException(JointAudioPlayer player, JointAudioTrack track, JointAudioException exception) {
		audioService.onTrackException(player, track, exception);
		audioService.skip(player);
	}

	@Override
	public void onGenericUpdateEvent(JointAudioPlayer player, JointAudioTrack track) {
		audioService.refreshPlayers(audioService.getGuild(player));
		
	}
}
