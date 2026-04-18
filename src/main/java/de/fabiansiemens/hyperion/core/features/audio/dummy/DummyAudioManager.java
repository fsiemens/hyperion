package de.fabiansiemens.hyperion.core.features.audio.dummy;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioEventListener;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayer;
import de.fabiansiemens.hyperion.core.features.audio.common.JointAudioPlayerManager;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

public class DummyAudioManager implements JointAudioPlayerManager {

	@Override
	public JointAudioPlayer createPlayer(Guild guild) {
		return new DummyAudioPlayer();
	}

	@Override
	public void loadItem(String identifier, LocalizedExpressionService les, InteractionHook hook,
			AudioService audioService, JointAudioPlayer player, boolean isSearch) {
		hook.sendMessage(les.getLocalizedExpression("error.common.not-available", audioService.getGuild(player)));
	}

	@Override
	public JointAudioEventListener createListener(AudioService service, JDAManager jdaManager) {
		return new DummyAudioEventListener();
	}

}
