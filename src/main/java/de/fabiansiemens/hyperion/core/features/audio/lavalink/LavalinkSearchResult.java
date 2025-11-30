package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import java.util.List;
import java.util.stream.Collectors;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioSearchResult;
import dev.arbjerg.lavalink.client.player.SearchResult;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LavalinkSearchResult implements JointAudioSearchResult {

	private SearchResult result;
	
	@Override
	public List<JointAudioTrack> getTracks() {
		return result.getTracks()
				.stream()
				.map(LavalinkAudioTrack::new)
				.collect(Collectors.toList());
	}
}
