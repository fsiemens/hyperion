package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import java.util.List;
import java.util.stream.Collectors;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlaylist;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LavalinkAudioPlaylist implements JointAudioPlaylist {

	private final PlaylistLoaded playlist;
	
	@Override
	public String getName() {
		return playlist.getInfo().getName();
	}

	@Override
	public List<JointAudioTrack> getTracks() {
		return playlist.getTracks()
				.stream()
				.map(LavalinkAudioTrack::new)
				.collect(Collectors.toList());
	}

	@Override
	public JointAudioTrack getSelectedTrack() {
		return new LavalinkAudioTrack(playlist.getTracks().get(playlist.getInfo().getSelectedTrack()));
	}

}
