package de.fabiansiemens.hyperion.core.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
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
import net.dv8tion.jda.api.interactions.InteractionHook;

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
	
	@SubscribeEvent
	public void onButtonInteraction(ButtonInteractionEvent event) {
		onUiInteraction(event, event.getHook());
	}
	
	@SubscribeEvent
	public void onStringSelectInteraction(StringSelectInteractionEvent event) {
		onUiInteraction(event, event.getHook());
	}
	
	@SubscribeEvent
	public void onModalInteraction(ModalInteractionEvent event) {
		onUiInteraction(event, event.getHook());
	}
	
	private <T extends ICustomIdInteraction> void onUiInteraction(T event, InteractionHook hook) {
		JointIdArgs idAndArgs = getIdAndArguments(event.getCustomId());
		Arguments args = idAndArgs.getArgs();
		String id = idAndArgs.getId();
		
		Method callback = uiRegistrationService.getInteractionCallbacks().get(id);
		Object target = uiRegistrationService.getInvokationTargets().get(id);
		
		if(callback != null && target != null) {
			if(!callback.getParameterTypes()[0].equals(event.getClass()) || !callback.getParameterTypes()[1].equals(Arguments.class)) {
				log.warn("Registered InteractionCallback does not accept parameters ({}, Arguments)", event.getClass().getName());
				return;
			}
			
			try {
				callback.invoke(target, event, args);
			} catch (IllegalAccessException | InvocationTargetException e) {
				log.warn("Uncaught Exception in UIListener: {}", e);
				hook.sendMessage(les.getLocalizedExpression(e.getCause().getLocalizedMessage(), hook.getInteraction().getGuild())).setEphemeral(true).queue();
			}
			return;
		}
		
		log.warn("Event from unknown interaction component with id '{}'", id);
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
