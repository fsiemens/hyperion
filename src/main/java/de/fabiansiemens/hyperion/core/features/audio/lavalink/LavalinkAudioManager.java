package de.fabiansiemens.hyperion.core.features.audio.lavalink;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayerManager;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import dev.arbjerg.lavalink.client.LavalinkClient;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Slf4j
public class LavalinkAudioManager implements JointAudioPlayerManager {

	private final LavalinkClient client;
	
	public LavalinkAudioManager(LavalinkClient client) {
		this.client = client;
	}
	
	@Override
	public JointAudioPlayer createPlayer(Guild guild) {
		return new LavalinkAudioPlayer(guild, client);
	}

	@Override
	public void loadItem(String identifier, LocalizedExpressionService les, InteractionHook hook, AudioService audioService, JointAudioPlayer player, boolean isSearch) {
		log.info("Loading item: {}", identifier);
		LavalinkTrackLoadHandler loader = new LavalinkTrackLoadHandler(les, hook, audioService, player, isSearch);
		client.getOrCreateLink(hook.getInteraction().getGuild().getIdLong()).loadItem(identifier).subscribe(loader);
	}

	@Override
	public JointAudioEventListener createListener(AudioService service, JDAManager jdaManager) {
		return new LavalinkEventListener(jdaManager, service);
	}

}
