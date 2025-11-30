package de.fabiansiemens.hyperion.core.features.audio.common;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;

public interface JointAudioException {
	public Exception getException();
	public Severity getSeverity();
	public String getLocalizedMessage();
}
