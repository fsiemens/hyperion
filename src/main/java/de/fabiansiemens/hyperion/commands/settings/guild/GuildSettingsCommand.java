package de.fabiansiemens.hyperion.commands.settings.guild;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SuperCommandBase;

@Command(dataFile = "settings/guild/GuildSettings.json")
public class GuildSettingsCommand extends SuperCommandBase {

	public <T> GuildSettingsCommand(ApplicationContext context)
			throws IllegalArgumentException, IOException {
		super(GuildSettingsCommand.class, context);
	}

}
