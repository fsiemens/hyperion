package de.fabiansiemens.hyperion.commands.settings.user;

import java.io.IOException;
import java.util.Optional;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "settings/user/Crit.json", parent = UserSettingsCommand.class, group = "roll")
public class UserRollCritSettingsCommand extends SlashCommandBase {

	private final UserService userService;
	
	public <T> UserRollCritSettingsCommand(UserService userService, ApplicationContext context)
			throws IOException, IllegalArgumentException {
		super(UserRollCritSettingsCommand.class, context);
		this.userService = userService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		Optional<Boolean> state = Optional.ofNullable(event.getOption("use-custom-settings")).map(mapping -> mapping.getAsBoolean());
		UserData userData = userService.findByUser(event.getUser()).orElse(userService.getDefault(event.getUser()));
		UserSettings settings = userData.getSettings();
		event.deferReply(true).queue();
		
		if(state.isEmpty()) {
			event.getHook().sendMessage(les.getLocalizedExpression("success.settings.overview", event.getGuild())
					+ "\n> **use-custom-settings:** " + String.valueOf(settings.isUseCritSettings()))
			.setEphemeral(true)
			.queue();
			return;
		}
		
		settings.setUseCritSettings(state.get());
		userData.setSettings(settings);
		userService.update(userData);
		event.getHook().sendMessage(les.getLocalizedExpression("success.settings.saved", event.getGuild())).setEphemeral(true).queue();
	}

}
