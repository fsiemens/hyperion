package de.fabiansiemens.hyperion.core.features.audio.common;

import de.fabiansiemens.hyperion.core.features.audio.lavalink.LavalinkEventListener;
import de.fabiansiemens.hyperion.core.features.audio.lavaplayer.LavaplayerEventListener;

public interface JointAudioEventListener {
	public LavalinkEventListener asLavalinkListener() throws UnsupportedOperationException;
	public LavaplayerEventListener asLavaplayerListener() throws UnsupportedOperationException;
}
