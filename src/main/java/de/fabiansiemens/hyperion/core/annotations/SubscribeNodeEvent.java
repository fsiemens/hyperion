package de.fabiansiemens.hyperion.core.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import dev.arbjerg.lavalink.client.event.ClientEvent;

@Retention(RUNTIME)
@Target(METHOD)
public @interface SubscribeNodeEvent {
	public Class<? extends ClientEvent> clazz();
}
