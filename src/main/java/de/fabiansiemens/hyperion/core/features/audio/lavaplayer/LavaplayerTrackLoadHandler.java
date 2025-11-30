package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.AudioTrackLoaderImpl;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
@AllArgsConstructor
public class LavaplayerTrackLoadHandler implements AudioLoadResultHandler {

	private final AudioTrackLoaderImpl loader;
	
	public LavaplayerTrackLoadHandler(
			LocalizedExpressionService les,
			InteractionHook hook,
			AudioService audioService,
			JointAudioPlayer player,
			boolean noPlaylists
		) {
		this.loader = new AudioTrackLoaderImpl(les, hook, audioService, player, noPlaylists);
	}
	
	@Override
	public void trackLoaded(AudioTrack track) {
		loader.onTrackLoaded(track == null ? null : new LavaplayerAudioTrack(track));
	}

	@Override
	public void playlistLoaded(AudioPlaylist playlist) {
		loader.onPlaylistLoaded(playlist == null ? null : new LavaplayerAudioPlaylist(playlist));
	}

	@Override
	public void noMatches() {
		loader.noMatches();
	}

	@Override
	public void loadFailed(FriendlyException exception) {
		loader.loadFailed(new LavaplayerLoadException(exception));
	}
}
