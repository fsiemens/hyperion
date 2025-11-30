package de.fabiansiemens.hyperion.core.features.audio.common;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

public interface JointAudioPlayer {
	public JointAudioTrack getPlayingTrack();
	public void join(AudioChannel channel);
	public void playTrack(JointAudioTrack track);
	public boolean startTrack(JointAudioTrack track, boolean noInterrupt);
	public void stopTrack();
	public int getVolume();
	public void setVolume(int volume);
	public boolean isPaused();
	public void setPaused(boolean value);
	public void addListener(JointAudioEventListener listener);
	public void removeListener(JointAudioEventListener listener);
	public AudioFrame provide();
	
}
