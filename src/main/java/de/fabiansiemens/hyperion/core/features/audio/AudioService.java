package de.fabiansiemens.hyperion.core.features.audio;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import de.fabiansiemens.hyperion.core.annotations.SubscribeNodeEvent;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioException;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayerManager;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioTrack;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.ui.UiProviderService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import dev.arbjerg.lavalink.client.LavalinkClient;
import dev.arbjerg.lavalink.client.event.ClientEvent;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.managers.DirectAudioController;

@Slf4j
@Service
public class AudioService {
	
	private LavalinkClient lavalinkClient;
	private UiProviderService uiProvider;
	private LocalizedExpressionService les;
	private JointAudioPlayerManager manager;
	@Getter
	private TrackScheduler scheduler;
	private JointAudioEventListener listener;
	private BiMap<Guild, JointAudioPlayer> playerMap;
	private Map<Guild, List<AudioPlayerPanelHandler>> panelHandlers;
	
	public AudioService(UiProviderService uiProvider, LocalizedExpressionService les, JointAudioPlayerManager manager, @Lazy TrackScheduler scheduler, LavalinkClient lavalinkClient, JDAManager jdaManager) {
		this.uiProvider = uiProvider;
		this.les = les;
		this.manager = manager;
		this.scheduler = scheduler;
		this.playerMap = HashBiMap.create();
		this.panelHandlers = new HashMap<>();
		this.lavalinkClient = lavalinkClient;
		
		this.listener = manager.createListener(this, jdaManager);
	}
	
	@PostConstruct
	public void init() {
		try {
			int c = 0;
			for(Method method : listener.asLavalinkListener().getClass().getMethods()) {
				if(method.isAnnotationPresent(SubscribeNodeEvent.class)) {
					SubscribeNodeEvent anno = method.getAnnotation(SubscribeNodeEvent.class);
					Class<? extends ClientEvent> eventType = anno.clazz();
					lavalinkClient.on(eventType).subscribe(toConsumer(listener.asLavalinkListener(), method));
					c++;
					continue;
				}
			}
			log.info("Successfully registered {} Lavalink ClientEvents.", c);
		}
		catch (UnsupportedOperationException e) {
			log.warn("Lavalink Listener initialization is not supported when lavaplayer is selected as audio player");
		}
	}
	
	public void join(AudioChannel channel) {
		if(channel.getGuild().getSelfMember().getVoiceState().inAudioChannel())
			return;
		
		JointAudioPlayer player = getPlayer(channel.getGuild());
		player.join(channel);
		player.setPaused(false);
	}
	
	public void pause(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		if(player == null || player.isPaused())
			return;
		
		player.setPaused(true);
	}
	
	public void resume(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		if(player == null || !player.isPaused())
			return;
		
		player.setPaused(false);
	}
	
	public boolean isPaused(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		return player.isPaused();
	}
	
	public void skip(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		if(player == null)
			return;
		
		skip(player);
	}
	
	public void skip(JointAudioPlayer player) {
		player.setPaused(false);
		scheduler.next(player);
	}
	
	public void stop(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		scheduler.clear(player);
		player.stopTrack();
		refreshPlayers(guild);
		
		unregisterPlayerPanels(guild);
		
		if(!guild.getSelfMember().getVoiceState().inAudioChannel())
			return;
		
		DirectAudioController audioController = guild.getJDA().getDirectAudioController();
		audioController.disconnect(guild);
	}
	
	public int getVolume(Guild guild) {
		JointAudioPlayer player = getPlayer(guild);
		return player.getVolume();
	}
	
	public void setVolume(Guild guild, int volume) {
		JointAudioPlayer player = getPlayer(guild);
		player.setVolume(volume);
	}
	
	public void loadItem(InteractionHook hook, String identifier, boolean isSearch) {
		log.info("Trying to load Track: {}", identifier);
		Guild guild = hook.getInteraction().getGuild();
		JointAudioPlayer player = getPlayer(guild);
		manager.loadItem(identifier, les, hook, this, player, isSearch);
	}

	public void onTrackStart(JointAudioPlayer player, JointAudioTrack track) {
		Guild guild = getGuild(player);
		setPanelView(player, PlayerPanelView.PLAYER);
		refreshPlayers(guild);
	}

	public void onQueueEmpty(JointAudioPlayer player) {
		stop(getGuild(player));
	}

	public void onTrackStuck(JointAudioPlayer player, JointAudioTrack track) {
		Message message = panelHandlers.get(getGuild(player)).getLast().getMessage();
		message.reply(les.getLocalizedExpression("error.play.track-stuck", message.getGuild())).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}
	
	public void onTrackEnd(JointAudioPlayer player, JointAudioTrack track, AudioTrackEndReason endReason) {
		log.info("Track ended: {}", endReason);
		//NOOP
	}

	public void onTrackException(JointAudioPlayer player, JointAudioTrack track, JointAudioException exception) {
		Message message = panelHandlers.get(getGuild(player)).getLast().getMessage();
		Arguments args = Arguments.of("reason", exception.getLocalizedMessage())
				.put("severity", exception.getSeverity().toString());
		message.reply(les.getLocalizedExpression("error.play.track-exception", message.getGuild(), args)).complete().delete().queueAfter(5, TimeUnit.SECONDS);
	}

	public void onPlayerPause(JointAudioPlayer player) {
		Guild guild = getGuild(player);
		refreshPlayers(guild);
	}

	public void onPlayerResume(JointAudioPlayer player) {
		Guild guild = getGuild(player);
		refreshPlayers(guild);
	}
	
	public JointAudioPlayer getPlayer(Guild guild) {
		JointAudioPlayer player = playerMap.get(guild);
		
		if(player == null) {
			player = manager.createPlayer(guild);
			player.addListener(listener);
			playerMap.put(guild, player);
		}
		
		return player;
	}
	
	public Guild getGuild(JointAudioPlayer player) {
		return playerMap.inverse().get(player);
	}

	public void registerPlayerPanel(Message message) {
		List<AudioPlayerPanelHandler> guildPanelHandlers = panelHandlers.getOrDefault(message.getGuild(), new LinkedList<AudioPlayerPanelHandler>());
		AudioPlayerPanelHandler handler = new AudioPlayerPanelHandler(les, uiProvider, message, PlayerPanelView.PLAYER);
		
		if(!guildPanelHandlers.contains(handler))
			guildPanelHandlers.add(handler);
		
		if(!panelHandlers.containsKey(message.getGuild()))
			panelHandlers.put(message.getGuild(), guildPanelHandlers);
	}
	
	private void setPanelView(JointAudioPlayer player, PlayerPanelView view) {
		getPlayerPanelHandlers(getGuild(player))
			.stream()
			.forEach(handler -> {
				handler.setView(view);
			});
	}
	
	public void setPanelView(Message message, PlayerPanelView view) {
		Optional<AudioPlayerPanelHandler> optPanelHandler = getPlayerPanelHandlers(message.getGuild()).stream()
			.filter(handler -> handler.getMessage().equals(message))
			.findFirst();
		
		if(optPanelHandler.isEmpty())
			return;
		
		optPanelHandler.get().setView(view);
	}
	
	public void setPanelQueueSelect(Message message, int selected) {
		Optional<AudioPlayerPanelHandler> optPanelHandler = getPlayerPanelHandlers(message.getGuild()).stream()
				.filter(handler -> handler.getMessage().equals(message))
				.findFirst();
			
		if(optPanelHandler.isEmpty())
			return;
		
		optPanelHandler.get().setSelectedTrack(selected);
	}
	
	public void refreshPlayers(Guild guild) {
		refreshPlayers(guild, Arguments.empty());
	}
	
	public void refreshPlayers(Guild guild, Arguments args) {
		JointAudioPlayer player = getPlayer(guild);
		List<AudioPlayerPanelHandler> panelHandlers = getPlayerPanelHandlers(guild);
		panelHandlers.stream()
			.forEach(handler -> handler.refresh(player, scheduler, args));
	}
	
	public List<AudioPlayerPanelHandler> getPlayerPanelHandlers(Guild guild) {
		return panelHandlers.getOrDefault(guild, new LinkedList<>());
	}
	
	private void unregisterPlayerPanels(Guild guild) {
		panelHandlers.remove(guild);
	}
	
	private <T> Consumer<T> toConsumer(Object annotated, Method m) {
		return param -> {
			try {
				m.invoke(annotated, param);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		};
	}
}
