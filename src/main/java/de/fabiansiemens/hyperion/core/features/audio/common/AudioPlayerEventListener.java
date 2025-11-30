package de.fabiansiemens.hyperion.core.features.audio.common;

import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import io.micrometer.common.lang.Nullable;

public interface AudioPlayerEventListener {
	public void onGenericUpdateEvent(JointAudioPlayer player, @Nullable JointAudioTrack track);
	public void onPlayerPause(JointAudioPlayer player);
	public void onPlayerResume(JointAudioPlayer player);
	public void onTrackStart(JointAudioPlayer player, JointAudioTrack track);
	public void onTrackStuck(JointAudioPlayer player, JointAudioTrack track);
	public void onTrackEnd(JointAudioPlayer player, JointAudioTrack track, AudioTrackEndReason endReason);
	public void onTrackException(JointAudioPlayer player, JointAudioTrack track, JointAudioException exception);
}
