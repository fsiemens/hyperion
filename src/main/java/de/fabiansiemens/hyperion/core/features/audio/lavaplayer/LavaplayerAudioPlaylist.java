package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import java.util.List;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlaylist;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LavaplayerAudioPlaylist implements JointAudioPlaylist {

	private final AudioPlaylist playlist;
	
	@Override
	public String getName() {
		return playlist.getName();
	}

	@Override
	public List<JointAudioTrack> getTracks() {
		return playlist.getTracks()
				.stream()
				.map(LavaplayerAudioTrack::new)
				.collect(Collectors.toList());
	}

	@Override
	public JointAudioTrack getSelectedTrack() {
		AudioTrack track = this.playlist.getSelectedTrack();
		return track == null ? null : new LavaplayerAudioTrack(track);
	}

}
