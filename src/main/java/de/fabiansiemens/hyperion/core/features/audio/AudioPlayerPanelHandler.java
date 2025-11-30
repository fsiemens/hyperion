package de.fabiansiemens.hyperion.core.features.audio;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.ui.UiProviderService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import io.micrometer.common.lang.Nullable;
import jakarta.annotation.Nonnull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Message;

@Slf4j
@RequiredArgsConstructor
public class AudioPlayerPanelHandler {
	
	public static final String AUDIO_PLAYER_PANEL_PATH = "audio/MusicPlayerPanel.xml";
	public static final String AUDIO_SETTINGS_PANEL_PATH = "audio/MusicSettingsPanel.xml";
	public static final String AUDIO_QUEUE_PANEL_PATH = "audio/MusicQueuePanel.xml";
	private static int MAX_TRACKS_PER_PAGE = 8;
	
	private final LocalizedExpressionService les;
	private final UiProviderService uiProvider;
	@Getter
	private final Message message;
	@Getter
	@Setter
	@Nonnull
	private PlayerPanelView view;
	
//	@Setter
//	private int queuePage;	//TODO queue paging
	@Setter
	private int selectedTrack;
	
	public static Arguments getPlayerArguments(Arguments args, JointAudioPlayer player, JointAudioTrack track, TrackQueue queue) {
		return getTrackArguments(args, track, 0, -1)
				.put("queueLen", queue.size())
				.put("isPlaying", track != null)
				.put("isPaused", player.isPaused())
				.put("timestamp", String.valueOf(Instant.now().getEpochSecond()));
	}
	
	public static Arguments getSettingsArguments(Arguments args, JointAudioPlayer player) {
		return args.put("vol", player.getVolume());
	}
	
	public static Arguments getQueueArguments(Arguments args, TrackQueue queue, int selectedTrack) {
		int page = 0;
		try {
			page = args.get("page").asInteger();
		}
		catch(ArgumentTypeException e) {
			log.warn("Invalid Argument 'page' for AudioQueuePanel");
		}
		
		args.put("queueLen", queue.size())
			.put("currentPage", page + 1)
			.put("trackSelected", selectedTrack >= 0);
		List<JointAudioTrack> tracks = queue.asList();
		List<Arguments> trackArgs = new LinkedList<>();
		
		int pos = page * MAX_TRACKS_PER_PAGE;
		for(JointAudioTrack track : tracks.subList(pos, Math.min(pos + MAX_TRACKS_PER_PAGE, tracks.size()))) {
			pos++;
			trackArgs.add(getTrackArguments(Arguments.empty(), track, pos, selectedTrack));
		}
		
		args.put("totalPages", Math.ceilDiv(tracks.size(), MAX_TRACKS_PER_PAGE));
		args.put("tracks", trackArgs.toArray(new Arguments[0]));
		return args;
	}
	
	private static Arguments getTrackArguments(Arguments args, @Nullable JointAudioTrack track, int position, int selectedTrack) {
		long duraMillis = 0;
		
		if(track != null) {
			duraMillis = track.getDuration();
		}
		
		Duration duration = Duration.ofMillis(duraMillis);
		long seconds = duration.getSeconds();
		long HH = seconds/3600;
		long MM = (seconds % 3600) / 60;
		long SS = seconds % 60;

		String duraStamp = String.format("%02d:%02d:%02d", HH, MM, SS);
		
		return args.put("author", track == null ? "" : track.getInfo().author)
			.put("title", track == null ? "" : track.getInfo().title)
			.put("url", track == null ? "" : track.getInfo().uri)
			.put("artwork", track == null ? "" : track.getInfo().artworkUrl)
//			.put("source", track == null ? "" : track.getSourceManager().getSourceName())
			.put("duration", track == null ? "??:??" : duraStamp)
			.put("end-timestamp", String.valueOf(Instant.now().getEpochSecond() + seconds))
			.put("pos", position)
			.put("isSelected", position == selectedTrack);
	}
	
	public void refresh(JointAudioPlayer player, TrackScheduler scheduler, Arguments args) {
		JointAudioTrack nowPlaying = player.getPlayingTrack();
		TrackQueue queue = scheduler.getQueue(player);
		String panelPath;
		
		switch(this.view) {
		case PLAYER:
			panelPath = AUDIO_PLAYER_PANEL_PATH;
			getPlayerArguments(args, player, nowPlaying, queue);
			break;
		case QUEUE:
			panelPath = AUDIO_QUEUE_PANEL_PATH;
			args = getQueueArguments(args, queue, this.selectedTrack);
			break;
		case SETTINGS:
			panelPath = AUDIO_SETTINGS_PANEL_PATH;
			args = getSettingsArguments(args, player);
			break;
		default:
			log.error("Illegal PlayerPanelView State '{}' in AudioPlayerPanelHandler for message {}", this.view, message.getId());
			return;
		}
		
		try {
			Container panel = uiProvider.panelOf(panelPath, message.getGuild(), args);
			message.editMessageComponents(panel).useComponentsV2().queue();
		} catch (UiParseException e) {
			message.editMessage(les.getLocalizedExpression(e.getLocalizedMessage(), message.getGuild(), args)).queue();
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AudioPlayerPanelHandler handler)
			return this.message.equals(handler.getMessage());
		
		return false;
	}
	
}
