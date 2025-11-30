package de.fabiansiemens.hyperion.core.features.audio.common;

public interface JointAudioTrackLoader {
	
	public void onTrackLoaded(JointAudioTrack track);
	public void onPlaylistLoaded(JointAudioPlaylist playlist);
//	public void onSearchResultLoaded(JointSearchResult searchResult);
	public void noMatches();
	public void loadFailed(JointAudioException exception);
}
