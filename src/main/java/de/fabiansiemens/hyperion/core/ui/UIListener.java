package de.fabiansiemens.hyperion.core.ui;

import java.lang.reflect.Method;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.exceptions.InteractionErrorHandler;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.ICustomIdInteraction;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

@Slf4j
@JdaListener
@AllArgsConstructor
public class UIListener {

	@Data
	@AllArgsConstructor
	private class JointIdArgs {
		private final String id;
		private final Arguments args;
	}
	
	private final LocalizedExpressionService les;
	private final UIRegistrationService uiRegistrationService;
	private final InteractionErrorHandler errorHandler;
	
	@SubscribeEvent
	public void onButtonInteraction(ButtonInteractionEvent event) {
		onUiInteraction(event);
	}
	
	@SubscribeEvent
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		onUiInteraction(event);
	}
	
	@SubscribeEvent
	public void onModalInteraction(ModalInteractionEvent event) {
		onUiInteraction(event);
	}
	
	private <T extends ICustomIdInteraction & IReplyCallback> void onUiInteraction(T event) {
		JointIdArgs idAndArgs = getIdAndArguments(event.getCustomId());
		Arguments args = idAndArgs.getArgs();
		String id = idAndArgs.getId();
		
		Method callback = uiRegistrationService.getInteractionCallbacks().get(id);
		Object target = uiRegistrationService.getInvokationTargets().get(id);
		
		if(callback == null || target == null) {
			log.warn("Event from unknown interaction component with id '{}'", id);
			errorHandler.reply(event, "error.common.interaction-expired");
			return;
		}
		
		if(!callback.getParameterTypes()[0].equals(event.getClass()) || !callback.getParameterTypes()[1].equals(Arguments.class)) {
			log.warn("Registered InteractionCallback for id '{}' does not accept parameters ({}, Arguments)", id, event.getClass().getName());
			errorHandler.reply(event, "error.common.interaction-expired");
			return;
		}
		
		try {
			callback.invoke(target, event, args);
		} catch (Exception e) {
			errorHandler.handle(event, e);
		}
	}
	
	private JointIdArgs getIdAndArguments(String customId) {
		String[] split = customId.split("\\?", 2);
		String id = split[0];
		
		Arguments args = Arguments.empty();
	
		String[] params = {};
		if(split.length > 1)
			params = split[1].split("&");
		
		for(String param : params) {
			String[] pair = param.split("=", 2);
			String key = pair[0];
			String value = pair.length > 1 ? pair[1] : "";
			args.put(key, value);
		}
		
		return new JointIdArgs(id, args);
	}
}
