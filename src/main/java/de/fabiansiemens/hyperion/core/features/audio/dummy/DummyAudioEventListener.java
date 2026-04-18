package de.fabiansiemens.hyperion.core.features.audio.dummy;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.lavalink.LavalinkEventListener;
import de.fabiansiemens.hyperion.core.features.audio.lavaplayer.LavaplayerEventListener;

public class DummyAudioEventListener implements JointAudioEventListener {

	@Override
	public LavalinkEventListener asLavalinkListener() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Override
	public LavaplayerEventListener asLavaplayerListener() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

}
