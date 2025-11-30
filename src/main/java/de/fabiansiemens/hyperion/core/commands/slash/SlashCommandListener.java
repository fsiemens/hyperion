package de.fabiansiemens.hyperion.core.commands.slash;

import org.springframework.beans.factory.annotation.Value;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.commands.CommandRegistrationService;
import de.fabiansiemens.hyperion.core.commands.ICommand;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@Slf4j
@JdaListener
@AllArgsConstructor
public class SlashCommandListener {
	
	@Value("${command-prefix}")
	private String commandPrefix;
	
	private CommandRegistrationService commandRegistrationService;
	private LocalizedExpressionService les;
	
	@SubscribeEvent
	public void onSlashCommandInteractionEvent(SlashCommandInteractionEvent event) {
		ICommand command = commandRegistrationService.getCommands().get(event.getName());
		
		if(command == null) {
			event.reply(les.getLocalizedExpression("error.slash-command.not-found", event.getGuild())).setEphemeral(true).queue();
			log.warn("Could not find a implementation for guild-registered command {}", event.getName());
			return;
		}
		
		if(!(command instanceof SlashCommand)) {
			Arguments args = Arguments.of("command", command.getName())
				.put("cmdSymbol", commandPrefix);
			event.reply(les.getLocalizedExpression("error.slash-command.not-slash", event.getGuild(), args)).setEphemeral(true).queue();
			log.warn("Guild-registered command {} is not a legacy command", event.getName());
			return;
		}
		
		try {
			((SlashCommand) command).performSlashCommand(event);
		} catch (UiParseException e) {
			if(event.isAcknowledged()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.common.ui", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			event.reply(les.getLocalizedExpression("error.common.ui", event.getGuild())).setEphemeral(true).queue();
		}
	}
}
