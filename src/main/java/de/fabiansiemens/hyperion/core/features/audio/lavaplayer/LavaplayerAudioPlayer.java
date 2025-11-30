package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import de.fabiansiemens.hyperion.core.features.audio.AudioPlayerSendHandler;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

@AllArgsConstructor
public class LavaplayerAudioPlayer implements JointAudioPlayer {

	private AudioPlayer player;
	
	@Override
	public void join(AudioChannel channel) {
		AudioManager audioManager = channel.getGuild().getAudioManager();
		audioManager.openAudioConnection(channel);
		audioManager.setSendingHandler(new AudioPlayerSendHandler(this.player));
	}
	
	@Override
	public JointAudioTrack getPlayingTrack() {
		AudioTrack track = player.getPlayingTrack();
		return track == null ? null : new LavaplayerAudioTrack(track);
	}

	@Override
	public void playTrack(JointAudioTrack track) {
		player.playTrack(track.asLavaplayerTrack());
	}

	@Override
	public boolean startTrack(JointAudioTrack track, boolean noInterrupt) {
		return player.startTrack(track.asLavaplayerTrack(), noInterrupt);
	}

	@Override
	public void stopTrack() {
		this.player.stopTrack();
	}

	@Override
	public int getVolume() {
		return this.player.getVolume();
	}

	@Override
	public void setVolume(int volume) {
		this.player.setVolume(volume);
	}

	@Override
	public boolean isPaused() {
		return this.player.isPaused();
	}

	@Override
	public void setPaused(boolean value) {
		this.player.setPaused(value);
	}

	@Override
	public void addListener(JointAudioEventListener listener) {
		this.player.addListener(listener.asLavaplayerListener());
	}

	@Override
	public void removeListener(JointAudioEventListener listener) {
		this.player.removeListener(listener.asLavaplayerListener());
	}

	@Override
	public AudioFrame provide() {
		return this.player.provide();
	}
	
	@Override
	public boolean equals(Object obj) {
		return this.player.equals(obj);
	}
}
