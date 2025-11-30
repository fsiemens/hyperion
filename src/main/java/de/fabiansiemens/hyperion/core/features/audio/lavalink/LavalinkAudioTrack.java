package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.TrackInfo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LavalinkAudioTrack implements JointAudioTrack {

	private final Track track;
	
	@Override
	public AudioTrackInfo getInfo() {
		TrackInfo info = track.getInfo();
		return new AudioTrackInfo(info.getTitle(), info.getAuthor(), info.getLength(), info.getIdentifier(), info.isStream(), info.getUri(), info.getArtworkUrl(), info.getIsrc());
	}

	@Override
	public String getIdentifier() {
		return track.getInfo().getIdentifier();
	}

	@Override
	public long getDuration() {
		return track.getInfo().getLength();
	}

	@Override
	public JointAudioTrack makeClone() {
		return new LavalinkAudioTrack(this.track.makeClone());
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
		String raw = track.getUserData().toString();
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(raw, klass);
		} catch (JsonProcessingException e) {
			log.warn("Exception while converting user data:", e);
			return null;
		}
	}
	
	@Override
	public Track asLavalinkTrack() throws UnsupportedOperationException {
		return track;
	}
	
	@Override
	public AudioTrack asLavaplayerTrack() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Cannot convert Lavalink-Track to Lavaplayer-AudioTrack");
	}

}
