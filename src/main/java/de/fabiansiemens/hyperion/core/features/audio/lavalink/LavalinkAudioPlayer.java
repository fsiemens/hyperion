package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.Link;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;

@Slf4j
public class LavalinkAudioPlayer implements JointAudioPlayer {

	@Getter
	private final Guild guild;
	@Getter
	private final LavalinkClient lavalink;
	
	private final List<LavalinkEventListener> listeners;
	
	public LavalinkAudioPlayer(Guild guild, LavalinkClient lavalink) {
		this.guild = guild;
		this.lavalink = lavalink;
		this.listeners = new LinkedList<>();
	}
	
	@Override
	public void join(AudioChannel channel) {
		channel.getJDA().getDirectAudioController().connect(channel);
	}
	
	@Override
	public JointAudioTrack getPlayingTrack() {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null) {
			return null;
		}
			
		
		Track track = player.getTrack();
		if(track == null) {
			return null;
		}
			
		
		return new LavalinkAudioTrack(track);
	}

	@Override
	public void playTrack(JointAudioTrack track) {
		startTrack(track, false);
	}

	@Override
	public boolean startTrack(JointAudioTrack track, boolean noInterrupt) {
		Link link = this.lavalink.getLinkIfCached(this.guild.getIdLong());
		if(link == null) {
			log.warn("No cached link, aborting track start");
			return false;
		}
		
		link.createOrUpdatePlayer()
			.setTrack(track.asLavalinkTrack())
			.subscribe(t -> {
				listeners.forEach(listener -> {
					listener.onGenericUpdateEvent(guild, t.getTrack());
				});
			});
		
		return true;
	}

	@Override
	public void stopTrack() {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null)
			return;
		
		player.stopTrack().subscribe(t -> {
			listeners.forEach(listener -> {
				listener.onGenericUpdateEvent(guild, t.getTrack());
			});
		});
	}

	@Override
	public int getVolume() {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null)
			return -1;
		
		return player.getVolume();
	}

	@Override
	public void setVolume(int volume) {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null)
			return;
		
		player.setVolume(volume).subscribe(t -> {
			listeners.forEach(listener -> {
				listener.onGenericUpdateEvent(guild, t.getTrack());
			});
		});
	}

	@Override
	public boolean isPaused() {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null)
			return false;
		
		return player.getPaused();
	}

	@Override
	public void setPaused(boolean value) {
		LavalinkPlayer player = getPlayer().orElse(null);
		if(player == null)
			return;
		
		player.setPaused(value).subscribe((t) -> {
			listeners.forEach(listener -> {
				if(t.getPaused())
					listener.onPlayerPaused(this.guild);
				else
					listener.onPlayerResumed(this.guild);
			});
		});
	}

	@Override
	public void addListener(JointAudioEventListener listener) {
		listeners.add(listener.asLavalinkListener());
	}

	@Override
	public void removeListener(JointAudioEventListener listener) {
		listeners.remove(listener.asLavalinkListener());
	}

	
	private Optional<Link> getLink(){
		return Optional.ofNullable(this.lavalink.getLinkIfCached(this.guild.getIdLong()));
	}
	
	private Optional<LavalinkPlayer> getPlayer() {
		return this.getLink().map(Link::getCachedPlayer);
	}

	@Override
	public AudioFrame provide() {
		log.warn("provide is not supported for LavalinkAudioPlayer");
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof LavalinkAudioPlayer lp)
			return this.guild.equals(lp.getGuild()) && this.lavalink.equals(lp.getLavalink());
		
		return false;
	}
}
