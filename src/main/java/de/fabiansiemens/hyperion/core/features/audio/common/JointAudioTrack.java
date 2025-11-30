package de.fabiansiemens.hyperion.core.features.audio.common;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import de.fabiansiemens.hyperion.core.features.audio.lavalink.LavalinkAudioTrack;
import de.fabiansiemens.hyperion.core.features.audio.lavaplayer.LavaplayerAudioTrack;
import dev.arbjerg.lavalink.client.player.Track;

public interface JointAudioTrack {
	
	public AudioTrackInfo getInfo();
	public String getIdentifier();
	public long getDuration();
	public Object getUserData();
	public JointAudioTrack makeClone();
	public void setUserData(Object userData);
	public <T> T getUserData(Class<T> klass);
	
	public Track asLavalinkTrack() throws UnsupportedOperationException;
	public AudioTrack asLavaplayerTrack() throws UnsupportedOperationException;
	
	public static JointAudioTrack of(AudioTrack track) {
		return track == null ? null : new LavaplayerAudioTrack(track);
	}
	
	public static JointAudioTrack of(Track track) {
		return track == null ? null : new LavalinkAudioTrack(track);
	}
}
