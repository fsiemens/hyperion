package de.fabiansiemens.hyperion.core.features.audio;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;

import de.fabiansiemens.hyperion.core.cli.LaunchArgumentService;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayerManager;
import de.fabiansiemens.hyperion.core.features.audio.lavalink.LavalinkAudioManager;
import de.fabiansiemens.hyperion.core.features.audio.lavaplayer.LavaplayerAudioManager;
import dev.arbjerg.lavalink.client.Helpers;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.NodeOptions;
import dev.arbjerg.lavalink.client.event.EmittedEvent;
import dev.arbjerg.lavalink.client.event.ReadyEvent;
import dev.arbjerg.lavalink.libraries.jda.JDAVoiceUpdateListener;
import dev.lavalink.youtube.YoutubeSourceOptions;
import dev.lavalink.youtube.clients.AndroidMusic;
import dev.lavalink.youtube.clients.AndroidVr;
import dev.lavalink.youtube.clients.ClientOptions;
import dev.lavalink.youtube.clients.Ios;
import dev.lavalink.youtube.clients.MWeb;
import dev.lavalink.youtube.clients.Music;
import dev.lavalink.youtube.clients.Tv;
import dev.lavalink.youtube.clients.TvHtml5EmbeddedWithThumbnail;
import dev.lavalink.youtube.clients.Web;
import dev.lavalink.youtube.clients.WebEmbedded;
import dev.lavalink.youtube.clients.skeleton.Client;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;

@Slf4j
@Configuration
public class AudioConfiguration {
	
	@Bean
	TrackScheduler trackScheduler(@Lazy AudioService audioService) {
		return new TrackScheduler(audioService);
	}
	
	@DependsOn("nodeOptions")
	@Bean(name = "lavalink-client")
	LavalinkClient lavalinkClient(String jdaToken, List<NodeOptions> nodeOptions) {
		LavalinkClient client = new LavalinkClient(Helpers.getUserIdFromToken(jdaToken));
		nodeOptions.stream().forEach(node -> client.addNode(node));
		client.on(EmittedEvent.class).subscribe(event -> {
			
		});
		client.on(ReadyEvent.class).subscribe(event -> {
			
		});
		return client;
	}
	
	@Bean
	@DependsOn("lavalink-client")
	VoiceDispatchInterceptor voiceDispatchInterceptor(LavalinkClient client) {
		return new JDAVoiceUpdateListener(client);
	}
	
	@Bean(name = "nodeOptions")
	List<NodeOptions> nodeOptions(Environment env) {
		List<NodeOptions> nodeOptions = new LinkedList<>();
		nodeOptions.add(new NodeOptions.Builder()
					.setName("Remote Audio Provider")
					.setSessionId("hyperion")
					.setServerUri(URI.create(env.getProperty("lavalink.node.lbfs.eu.uri")))
					.setPassword(env.getProperty("lavalink.node.lbfs.eu.password"))
					.setHttpTimeout(5000L)
					.build()
				);
		return nodeOptions;
	}
	
	@Bean
	JDAVoiceUpdateListener jdaVoiceUpdateListener(LavalinkClient lavaClient) {
		return new JDAVoiceUpdateListener(lavaClient);
	}
	
	
	@Bean
	JointAudioPlayerManager audioPlayerManager(@Autowired LaunchArgumentService las, @Autowired Environment env, LavalinkClient lavalinkClient) {
		String activeAudioManager = las.env.getProperty("audio.player.manager");
		if(activeAudioManager == null) {
			throw new IllegalArgumentException("A Audio Player must be defined at launch property 'audio.player.manager'. Choose either 'lavaplayer' or 'lavalink'.");
		}
		
		switch(activeAudioManager) {
		case "lavaplayer":
			return lavaplayerAudioPlayerManager(env);
		case "lavalink":
			return lavalinkAudioPlayerManager(env, lavalinkClient);
		default: throw new IllegalArgumentException("Provided value ('" + activeAudioManager + "') for launch property 'audio.player.manager' is not supported.");
		}
	}
	
	@SuppressWarnings("deprecation")
	private LavaplayerAudioManager lavaplayerAudioPlayerManager(Environment env) {
		LavaplayerAudioManager manager = new LavaplayerAudioManager();
		AudioSourceManagers.registerRemoteSources(manager, YoutubeAudioSourceManager.class);
		YoutubeSourceOptions options = new YoutubeSourceOptions()
				.setRemoteCipher("https://cipher.kikkia.dev/", null, "hyperion")
				.setAllowSearch(true)
				.setAllowDirectVideoIds(true)
				.setAllowDirectPlaylistIds(true);
		
		ClientOptions allTrue = ClientOptions.DEFAULT;
		ClientOptions noVideoLoading = ClientOptions.DEFAULT;
			noVideoLoading.setVideoLoading(false);
		ClientOptions noPlaylistLoading = ClientOptions.DEFAULT;
			noPlaylistLoading.setPlaylistLoading(false);
		ClientOptions noPlaylistLoadingAndSearching = noPlaylistLoading.copy();
			noPlaylistLoadingAndSearching.setSearching(false);
		ClientOptions noPlayback = ClientOptions.DEFAULT;
			noPlayback.setPlayback(false);
		ClientOptions noPlaybackAndVideoLoading = noPlayback.copy();
			noPlaybackAndVideoLoading.setVideoLoading(false);
		ClientOptions onlyVideoLoading = noPlaylistLoadingAndSearching.copy();
			onlyVideoLoading.setPlayback(false);
		ClientOptions onlyPlayback = noPlaylistLoadingAndSearching.copy();
			onlyPlayback.setVideoLoading(false);
		ClientOptions onlySearching = noPlaybackAndVideoLoading.copy();
			onlySearching.setPlaylistLoading(false);
		ClientOptions allFalse = onlyVideoLoading.copy();
			allFalse.setVideoLoading(false);
		
		dev.lavalink.youtube.YoutubeAudioSourceManager ytSource = new dev.lavalink.youtube.YoutubeAudioSourceManager(options, new Client[] { 
				new Tv(onlyPlayback),
				new TvHtml5EmbeddedWithThumbnail(noPlaylistLoading), 
				new AndroidVr(allTrue), 
				new Web(allTrue),
				new MWeb(allTrue),
				new WebEmbedded(noPlaylistLoadingAndSearching),
				new Music(onlySearching),
				new AndroidMusic(noPlaylistLoading),
				new Ios(allTrue)
		});
		String refreshToken = env.getProperty("lava.oauth.yt-refresh-token");
		ytSource.useOauth2(refreshToken, false);
		manager.registerSourceManagers(ytSource);
		return manager;
	}
	
	private LavalinkAudioManager lavalinkAudioPlayerManager(Environment env, LavalinkClient lavalinkClient) {
		return new LavalinkAudioManager(lavalinkClient);
	}
	
	
}
