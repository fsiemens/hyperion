package de.fabiansiemens.hyperion.core.audio;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.audio.dummies.DummyTrack;
import de.fabiansiemens.hyperion.core.features.audio.TrackQueue;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class Test_TrackQueue {
	
	@Test
	public void testConstruct() {
		TrackQueue queue = new TrackQueue();
		assertThat(queue).isNotNull();
	}
	
	@Test
	public void testAddAndPop() {
		TrackQueue queue = new TrackQueue();
		DummyTrack track1 = new DummyTrack();
		DummyTrack track2 = new DummyTrack();
		
		queue.add(track1, track2);
		assertThat(queue.size()).isEqualTo(2);
		assertThat(queue.pop()).isEqualTo(track1);
		assertThat(queue.size()).isEqualTo(1);
		assertThat(queue.pop()).isEqualTo(track2);
		
		queue.add(track1);
		queue.add(track2);
		assertThat(queue.size()).isEqualTo(2);
		
		List<JointAudioTrack> tracks = new LinkedList<>();
		tracks.add(track2);
		tracks.add(track1);
		queue.addAll(tracks);
		assertThat(queue.size()).isEqualTo(4);
		assertThat(queue.pop()).isEqualTo(track1);
		assertThat(queue.pop()).isEqualTo(track2);
		assertThat(queue.pop()).isEqualTo(track2);
		assertThat(queue.pop()).isEqualTo(track1);
	}
	
	@Test
	public void testIsEmpty() {
		TrackQueue queue = new TrackQueue();
		assertThat(queue.isEmpty()).isTrue();
		DummyTrack track1 = new DummyTrack();
		queue.add(track1);
		assertThat(queue.isEmpty()).isFalse();
	}
	
	public void testClear() {
		TrackQueue queue = new TrackQueue();
		DummyTrack track1 = new DummyTrack();
		DummyTrack track2 = new DummyTrack();
		
		assertThat(queue.isEmpty()).isTrue();
		queue.add(track1, track2);
		assertThat(queue.isEmpty()).isFalse();
		queue.clear();
		assertThat(queue.isEmpty()).isTrue();
	}
}
