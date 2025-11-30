package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LavaplayerAudioTrack implements JointAudioTrack {

	private final AudioTrack track;
	
	@Override
	public AudioTrackInfo getInfo() {
		return track.getInfo();
	}

	@Override
	public String getIdentifier() {
		return track.getIdentifier();
	}

	@Override
	public long getDuration() {
		return track.getDuration();
	}

	@Override
	public JointAudioTrack makeClone() {
		return new LavaplayerAudioTrack(track.makeClone());
	}

	@Override
	public void setUserData(Object userData) {
		track.setUserData(userData);
	}

	@Override
	public Object getUserData() {
		return track.getUserData();
	}

	@Override
	public <T> T getUserData(Class<T> klass) {
		return track.getUserData(klass);
	}

	@Override
	public Track asLavalinkTrack() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot convert Lavaplayer-AudioTrack to Lavalink-Track");
	}

	@Override
	public AudioTrack asLavaplayerTrack() throws UnsupportedOperationException {
		return track;
	}

}
