package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioException;
import dev.arbjerg.lavalink.client.player.LoadFailed;
import dev.arbjerg.lavalink.client.player.TrackException;
import lombok.Getter;

public class LavalinkException implements JointAudioException {

	@Getter
	private final String localizedMessage;
	@Getter
	private final Severity severity;
	
	
	public LavalinkException(TrackException exception) {
		this.localizedMessage = exception.getMessage();
		this.severity = mapSeverity(exception.getSeverity());
	}

	public LavalinkException(LoadFailed exception) {
		this.localizedMessage = exception.getException().getMessage();
		this.severity = mapSeverity(exception.getException().getSeverity());
	}

	@Override
	public java.lang.Exception getException() {
		java.lang.Exception e = new java.lang.Exception(localizedMessage);
		return e;
	}

	private Severity mapSeverity(dev.arbjerg.lavalink.protocol.v4.Exception.Severity severity) {
		switch(severity) {
		case COMMON:
			return Severity.COMMON;
		case FAULT:
			return Severity.FAULT;
		case SUSPICIOUS:
		default:
			return Severity.SUSPICIOUS;
		}
	}
}
