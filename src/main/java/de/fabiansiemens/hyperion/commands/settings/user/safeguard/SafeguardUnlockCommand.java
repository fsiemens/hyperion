package de.fabiansiemens.hyperion.commands.settings.user.safeguard;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.commands.settings.user.UserSettingsCommand;
import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "settings/user/safeguard/Unlock.json", parent = UserSettingsCommand.class, group = "safeguard")
public class SafeguardUnlockCommand extends SlashCommandBase {

	private final UserService userService;
	
	public <T> SafeguardUnlockCommand(ApplicationContext context, UserService userService)
			throws IOException, IllegalArgumentException {
		super(SafeguardUnlockCommand.class, context);
		this.userService = userService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		UserData data = userService.findByUser(event.getMember().getUser()).orElseGet(() -> userService.getDefault(event.getMember().getUser()));
		UserSettings settings = data.getSettings();
		settings.setSafeguard(null);
		data.setSettings(settings);
		userService.update(data);
		
		Arguments args = Arguments.of("user", event.getMember().getAsMention());
		event.reply(les.getLocalizedExpression("success.safeguard.unlocked", event.getGuild(), args)).setEphemeral(true).queue();
	}
}
