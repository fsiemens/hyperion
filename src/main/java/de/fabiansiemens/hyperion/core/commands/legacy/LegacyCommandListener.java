package de.fabiansiemens.hyperion.core.commands.legacy;

import org.springframework.beans.factory.annotation.Value;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.commands.CommandRegistrationService;
import de.fabiansiemens.hyperion.core.commands.ICommand;
import de.fabiansiemens.hyperion.core.events.LegacyCommandEvent;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.jda.JDAManager;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.systems.ManageableSystem;
import de.fabiansiemens.hyperion.core.systems.SystemAvailability;
import de.fabiansiemens.hyperion.core.util.Arguments;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@Slf4j
@JdaListener
public class LegacyCommandListener implements ManageableSystem {
	
	@Value("${command-prefix}")
	private String commandPrefix;
	private volatile SystemAvailability availability;
	private final JDAManager jdaManager;
	private final LocalizedExpressionService les;
	private final CommandRegistrationService commandRegistrationService;
	
	public LegacyCommandListener(
			@NonNull final JDAManager jdaManager, 
			@NonNull final LocalizedExpressionService les,
			@NonNull final CommandRegistrationService commandRegistrationService) {
		this.jdaManager = jdaManager;
		this.les = les;
		this.commandRegistrationService = commandRegistrationService;
		this.availability = SystemAvailability.NOT_LAUNCHED;
	}
	
	@SubscribeEvent
	public void onMessageReceived(@NonNull MessageReceivedEvent event) {
		if(event.getAuthor().equals(event.getJDA().getSelfUser()))
			return;
		
		var channel = event.getChannel();
		
		if(availability != SystemAvailability.AVAILABLE) {
			if(jdaManager.isAvailable() && channel.canTalk()) {
				sendLocalized("error.legacy-commands.deactivated", event);
			}
			return;	
		}
		
		String content = event.getMessage().getContentRaw();
		if(!content.startsWith(commandPrefix) || !channel.canTalk()) {
			return;
		}
		
		content = content.replaceFirst(commandPrefix, "");
		@Nullable ICommand command = getCommand(content);
		
		if(command == null) {
			sendLocalized("error.commands.not-found", event);
			return;
		}
		
		Arguments args = Arguments.empty();
		
		if(!(command instanceof LegacyCommand)) {
			args.put("command", command.getName());
			sendLocalized("error.legacy-command.not-legacy", event, args);
		}
		
		try {
			if(((LegacyCommand) command).shouldDeleteInitMessage())
				event.getMessage().delete().queue();
		} catch(Exception e) {}
		
		String prompt = content.substring(command.getName().length()).strip();
		LegacyCommandEvent legacyEvent = new LegacyCommandEvent(event, prompt, (LegacyCommand) command);
		try {
			((LegacyCommand) command).performLegacyCommand(legacyEvent);
		} catch (UiParseException e) {
			channel.sendMessage(les.getLocalizedExpression("error.common.ui", event.getGuild())).queue();
		}
	}

	@Override
	public void launch() {
		this.availability = SystemAvailability.AVAILABLE;
	}
	
	@Override
	public boolean isAvailable() {
		return availability.equals(SystemAvailability.AVAILABLE);
	}
	
	@Override
	public SystemAvailability getAvailabilityState() {
		return availability;
	}
	
	@Override
	public void onError(ManageableSystem system, Throwable throwable) {
		//TODO Do something if other systems experience a error
	}

	@Override
	public void shutdown() {
		this.availability = SystemAvailability.SHUTDOWN;
	}

	@Override
	public void deactivate(boolean state) {
		if(this.availability == SystemAvailability.NOT_LAUNCHED 
				|| this.availability == SystemAvailability.CRITICAL_ERROR 
				|| this.availability == SystemAvailability.SHUTDOWN)
			throw new IllegalStateException(this.getClass().getSimpleName() + " is not in a state to manually change availability. Consider calling launch() or shutdown().");
		
		this.availability = (state ? SystemAvailability.DEACTIVATED : SystemAvailability.AVAILABLE);
	}
	
	@Nullable
	private ICommand getCommand(String messageContent) {
		for(String name : commandRegistrationService.getCommands().keySet()) {
			if(messageContent.toLowerCase().startsWith(name.toLowerCase()))
				return commandRegistrationService.getCommands().get(name);
		}
		return null;
	}
	
	
	private void sendLocalized(String textId, MessageReceivedEvent event) {
		sendLocalized(textId, event, Arguments.empty());
	}
	
	private void sendLocalized(String textId, MessageReceivedEvent event, Arguments args) {
		if(event.isFromGuild())
			event.getMessage().reply(les.getLocalizedExpression(textId, event.getGuild(), args)).queue();
		else
			event.getMessage().reply(les.getDefaultExpression(textId, args)).queue();
	}
}
