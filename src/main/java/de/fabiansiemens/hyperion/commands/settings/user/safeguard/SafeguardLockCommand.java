package de.fabiansiemens.hyperion.commands.settings.user.safeguard;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.commands.settings.user.UserSettingsCommand;
import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.persistence.safeguard.SafeguardEntity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "settings/user/safeguard/Lock.json", parent = UserSettingsCommand.class, group = "safeguard")
public class SafeguardLockCommand extends SlashCommandBase {

	private final UserService userService;
	
	public <T> SafeguardLockCommand(ApplicationContext context, UserService userService)
			throws IOException, IllegalArgumentException {
		super(SafeguardLockCommand.class, context);
		this.userService = userService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		String lockTimeRaw = event.getOption("lock").getAsString();
		String unlockTimeRaw = event.getOption("unlock").getAsString();
		String reason = event.getOption("reason", mapping -> "");
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm[:ss]");
		LocalTime lockTime = LocalTime.from(formatter.parse(lockTimeRaw));
		LocalTime unlockTime = LocalTime.from(formatter.parse(unlockTimeRaw));
		
		UserData data = userService.findByUser(event.getMember().getUser()).orElseGet(() -> userService.getDefault(event.getMember().getUser()));
		UserSettings settings = data.getSettings();
		
		settings.setSafeguard(new SafeguardEntity(unlockTime, lockTime, reason));
		data.setSettings(settings);
		userService.update(data);
		
		Arguments args = Arguments.of("user", event.getMember().getAsMention())
			.put("lock", lockTime.toString())
			.put("unlock", unlockTime.toString());
		event.reply(les.getLocalizedExpression("success.safeguard.locked", event.getGuild(), args)).setEphemeral(true).queue();
	}

}
