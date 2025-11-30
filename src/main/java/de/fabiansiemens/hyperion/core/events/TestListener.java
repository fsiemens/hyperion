package de.fabiansiemens.hyperion.core.events;

import org.springframework.stereotype.Component;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.StatusChangeEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@Slf4j
@Component
@JdaListener
public class TestListener {
	
	@SubscribeEvent
	public void onStatusChangeEvent(StatusChangeEvent event) {
		log.info("Detected StatusChangeEvent: {}", event);
	}
	
	@SubscribeEvent
	public void onMessageReceivedEvent(MessageReceivedEvent event) {
		log.info("Detected MessageReceivedEvent: {}", event);
	}
}
