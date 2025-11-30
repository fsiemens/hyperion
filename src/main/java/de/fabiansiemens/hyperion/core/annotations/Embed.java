package de.fabiansiemens.hyperion.core.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

@Deprecated
@Component
@Retention(RUNTIME)
@Target(TYPE)
public @interface Embed {
	public String dataFile();
	String id() default "";
}
