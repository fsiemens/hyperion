package de.fabiansiemens.hyperion.core.features.audio;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;

public class TrackQueue {
	
	private final Queue<JointAudioTrack> queue;
	
	public TrackQueue() {
		this.queue = new LinkedList<JointAudioTrack>();
	}

	public void clear() {
		this.queue.clear();
	}
	
	public void add(JointAudioTrack... tracks) {
		addAll(List.of(tracks));
	}

	public void addAll(List<JointAudioTrack> tracks) {
		this.queue.addAll(tracks);
	}
	
	public JointAudioTrack pop() {
		return this.queue.poll();
	}

	public boolean isEmpty() {
		return this.queue.isEmpty();
	}
	
	public long size() {
		return this.queue.size();
	}
	
	public List<JointAudioTrack> asList() {
		return (LinkedList<JointAudioTrack>) queue;
	}
}
