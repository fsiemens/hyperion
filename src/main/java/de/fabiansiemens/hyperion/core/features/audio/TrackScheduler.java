package de.fabiansiemens.hyperion.core.features.audio;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlaylist;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
public class TrackScheduler {
	
	private AudioService audioService;
	private final Map<Guild, TrackQueue> queueMap;
	
	public TrackScheduler(AudioService audioService) {
		this.audioService = audioService;
		this.queueMap = new HashMap<Guild, TrackQueue>();
	}
	
	public TrackQueue getQueue(JointAudioPlayer player) {
		return this.queueMap.getOrDefault(audioService.getGuild(player), new TrackQueue());
	}
	
	public void clear(JointAudioPlayer player) {
		TrackQueue queue = queueMap.getOrDefault(audioService.getGuild(player), new TrackQueue());
		queue.clear();
	}
	
	public void next(JointAudioPlayer player) {
		startFirstTrackInQueue(player, false, () -> {
			audioService.onQueueEmpty(player);
		});
	}
	
	public void loadTrack(JointAudioPlayer player, JointAudioTrack track) {
		enqueue(player, track);
		startFirstTrackInQueue(player);
	}
	
	public void loadPlaylist(JointAudioPlayer player, JointAudioPlaylist playlist) {
		enqueue(player, playlist.getTracks());
		startFirstTrackInQueue(player);
	}
	
	private void enqueue(JointAudioPlayer player, JointAudioTrack... tracks) {
		enqueue(player, Arrays.asList(tracks));
	}
	
	private void enqueue(JointAudioPlayer player, List<JointAudioTrack> tracks) {
		Guild guild = audioService.getGuild(player);
		TrackQueue queue = this.queueMap.get(guild);
		if(queue == null) {
			log.info("No queue registered, creating new");
			queue = new TrackQueue();
			queueMap.put(guild, queue);
		}
		
		queue.addAll(tracks);
		log.info("Queue length: {}", queue.size());
	}
	
	private void startFirstTrackInQueue(JointAudioPlayer player) {
		startFirstTrackInQueue(player, true);
	}
	
	private void startFirstTrackInQueue(JointAudioPlayer player, boolean noInterrupt) {
		startFirstTrackInQueue(player, noInterrupt, () -> {});
	}
	
	private void startFirstTrackInQueue(JointAudioPlayer player, boolean noInterrupt, Runnable queueEmptyAction) {
		Guild guild = audioService.getGuild(player);
		TrackQueue queue = queueMap.getOrDefault(guild, new TrackQueue());
		if(queue.isEmpty()) {
			queueEmptyAction.run();
			return;
		}
		
		if(player.getPlayingTrack() == null || !noInterrupt) {
			log.info("Trying to play track with noInterrupt: {}", noInterrupt);
			player.startTrack(queue.pop(), noInterrupt);
		}
	}
}
