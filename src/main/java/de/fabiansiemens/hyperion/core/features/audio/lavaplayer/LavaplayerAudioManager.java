package de.fabiansiemens.hyperion.core.features.audio.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayerManager;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class LavaplayerAudioManager extends DefaultAudioPlayerManager implements JointAudioPlayerManager {
	
	public JointAudioPlayer createPlayer(Guild guild) {
		return new LavaplayerAudioPlayer(super.createPlayer());
	}

	@Override
	public void loadItem(String identifier, LocalizedExpressionService les, InteractionHook hook,
			AudioService audioService, JointAudioPlayer player, boolean isSearch) {
		super.loadItem(identifier, new LavaplayerTrackLoadHandler(les, hook, audioService, player, isSearch));
	}

	@Override
	public JointAudioEventListener createListener(AudioService service, JDAManager jdaManager) {
		return new LavaplayerEventListener(service);
	}
}
