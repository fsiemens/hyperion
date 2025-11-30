package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioTrackLoaderImpl;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import dev.arbjerg.lavalink.client.AbstractAudioLoadResultHandler;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.PlaylistLoaded;
import dev.arbjerg.lavalink.client.player.SearchResult;
import dev.arbjerg.lavalink.client.player.TrackLoaded;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class LavalinkTrackLoadHandler extends AbstractAudioLoadResultHandler {

	private AudioTrackLoaderImpl loader;
	
	public LavalinkTrackLoadHandler(
			LocalizedExpressionService les, 
			InteractionHook hook, 
			AudioService audioService, 
			JointAudioPlayer player,
			boolean noPlaylists
		) {
		this.loader = new AudioTrackLoaderImpl(les, hook, audioService, player, noPlaylists);
	}
	
	@Override
	public void noMatches() {
		loader.noMatches();
	}

	@Override
	public void loadFailed(LoadFailed arg0) {
		loader.loadFailed(new LavalinkException(arg0));
	}

	@Override
	public void onPlaylistLoaded(PlaylistLoaded arg0) {
		loader.onPlaylistLoaded(new LavalinkAudioPlaylist(arg0));
	}

	@Override
	public void onSearchResultLoaded(SearchResult arg0) {
		loader.onSearchResultLoaded(new LavalinkSearchResult(arg0));
		return;
	}

	@Override
	public void ontrackLoaded(TrackLoaded arg0) {
		log.info("Track loaded: {}", arg0.getTrack().getInfo().getTitle());
		loader.onTrackLoaded(new LavalinkAudioTrack(arg0.getTrack()));
	}
}
