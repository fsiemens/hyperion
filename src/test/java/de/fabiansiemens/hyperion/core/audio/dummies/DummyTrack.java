package de.fabiansiemens.hyperion.core.audio.dummies;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import dev.arbjerg.lavalink.client.player.Track;

public class DummyTrack implements JointAudioTrack {

	@Override
	public AudioTrackInfo getInfo() {
		return new AudioTrackInfo("dummy", "dummy", 0, "dummy", false, "dummy");
	}

	@Override
	public String getIdentifier() {
		return "dummy";
	}

	@Override
	public long getDuration() {
		return 0;
	}

	@Override
	public JointAudioTrack makeClone() {
		return new DummyTrack();
	}

	@Override
	public void setUserData(Object userData) {
		
	}

	@Override
	public Object getUserData() {
		return null;
	}

	@Override
	public <T> T getUserData(Class<T> klass) {
		return null;
	}

	@Override
	public Track asLavalinkTrack() throws UnsupportedOperationException {
		return null;
	}

	@Override
	public AudioTrack asLavaplayerTrack() throws UnsupportedOperationException {
		return null;
	}
	
}
