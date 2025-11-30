package de.fabiansiemens.hyperion.core.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

import de.fabiansiemens.hyperion.core.commands.slash.SuperCommand;

@Component
@Retention(RUNTIME)
@Target(TYPE)
public @interface Subcommand {
	public String dataFile();
	public Class<? extends SuperCommand> parent();
	public String group() default "";
}
