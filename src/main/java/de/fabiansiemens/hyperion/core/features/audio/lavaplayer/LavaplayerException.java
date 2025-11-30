package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LavaplayerException implements JointAudioException {

	private final FriendlyException exception;
	
	@Override
	public Exception getException() {
		return exception;
	}

	@Override
	public Severity getSeverity() {
		return this.exception.severity;
	}

	@Override
	public String getLocalizedMessage() {
		return this.exception.getLocalizedMessage();
	}

}
