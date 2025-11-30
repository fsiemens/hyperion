package de.fabiansiemens.hyperion.core.systems;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.commands.CommandRegistrationService;
import de.fabiansiemens.hyperion.core.commands.legacy.LegacyCommandListener;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.ui.UIRegistrationService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SystemManager {

	private final JDAManager jdaManager;
	private final LegacyCommandListener legacyCommandListener;
	private final CommandRegistrationService commandRegistrationService;
	private final UIRegistrationService uiRegistrationService;
	
	public void launch() {
		jdaManager.launch();
		legacyCommandListener.launch();
	}
	
	
}
