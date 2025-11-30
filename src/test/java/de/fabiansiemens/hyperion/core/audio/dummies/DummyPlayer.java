package de.fabiansiemens.hyperion.core.audio.dummies;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public class DummyPlayer implements JointAudioPlayer {

	@Override
	public void join(AudioChannel channel) {
		
	}
	
	@Override
	public AudioFrame provide() {
		return null;
	}

	@Override
	public JointAudioTrack getPlayingTrack() {
		return new DummyTrack();
	}

	@Override
	public void playTrack(JointAudioTrack track) {
		
	}

	@Override
	public boolean startTrack(JointAudioTrack track, boolean noInterrupt) {
		return true;
	}

	@Override
	public void stopTrack() {
		
	}

	@Override
	public int getVolume() {
		return 0;
	}

	@Override
	public void setVolume(int volume) {
		
	}

	@Override
	public boolean isPaused() {
		return false;
	}

	@Override
	public void setPaused(boolean value) {
		
	}

	@Override
	public void addListener(JointAudioEventListener listener) {
		
	}

	@Override
	public void removeListener(JointAudioEventListener listener) {
		
	}
}
