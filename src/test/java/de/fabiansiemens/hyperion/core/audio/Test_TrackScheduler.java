package de.fabiansiemens.hyperion.core.audio;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.audio.dummies.DummyTrack;
import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.TrackScheduler;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import net.dv8tion.jda.internal.JDAImpl;
import net.dv8tion.jda.internal.entities.GuildImpl;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class Test_TrackScheduler {

	private final JDAManager jdaManager;
	private final AudioService audioService;
	
	@Autowired
	public Test_TrackScheduler(JDAManager jdaManager, AudioService audioService) {
		this.jdaManager = jdaManager;
		this.audioService = audioService;
	}
	
	@Test
	public void testEnqueue() {
		
		JointAudioPlayer player = audioService.getPlayer(new GuildImpl((JDAImpl) jdaManager.getJDA(), 123456789));
		TrackScheduler scheduler = new TrackScheduler(audioService);
		scheduler.loadTrack(player, new DummyTrack());
		//TODO write test
	}
}
