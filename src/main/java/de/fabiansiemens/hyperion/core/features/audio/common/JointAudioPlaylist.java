package de.fabiansiemens.hyperion.core.features.audio.common;

import java.util.List;

public interface JointAudioPlaylist {
	
	public String getName();
	
	public List<JointAudioTrack> getTracks();
	
	public JointAudioTrack getSelectedTrack();
}
