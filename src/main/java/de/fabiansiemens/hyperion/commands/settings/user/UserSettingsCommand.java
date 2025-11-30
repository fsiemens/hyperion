package de.fabiansiemens.hyperion.commands.settings.user;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SuperCommandBase;

@Command(dataFile = "settings/user/Settings.json")
public class UserSettingsCommand extends SuperCommandBase {

	public UserSettingsCommand(ApplicationContext context)
			throws IllegalArgumentException, IOException {
		super(UserSettingsCommand.class, context);
	}
}
