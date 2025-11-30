package de.fabiansiemens.hyperion.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Annotation for JDA Listeners.
 * Classes annotated with this Annotation, will be automatically added to the JDA Listeners
 */
@Service
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JdaListener {

}
