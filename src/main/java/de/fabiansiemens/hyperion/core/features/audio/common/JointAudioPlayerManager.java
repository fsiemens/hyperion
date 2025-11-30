package de.fabiansiemens.hyperion.core.features.audio.common;

import de.fabiansiemens.hyperion.core.features.audio.AudioService;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.InteractionHook;

public interface JointAudioPlayerManager {

	public JointAudioPlayer createPlayer(Guild guild);
	public void loadItem(String identifier, LocalizedExpressionService les, InteractionHook hook, AudioService audioService, JointAudioPlayer player, boolean isSearch);
	public JointAudioEventListener createListener(AudioService service, JDAManager jdaManager);
}
