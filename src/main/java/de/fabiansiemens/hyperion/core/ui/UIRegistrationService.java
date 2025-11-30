package de.fabiansiemens.hyperion.core.ui;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UIRegistrationService {
	private final ApplicationContext context;
	
	@Getter
	private final Map<String, Method> interactionCallbacks;
	
	@Getter
	private final Map<String, Object> invokationTargets;
	
	@Getter
	private int interactionChains;
	
	public UIRegistrationService(ApplicationContext context) {
		this.context = context;
		this.interactionCallbacks = new ConcurrentHashMap<>();
		this.invokationTargets = new ConcurrentHashMap<>();
		this.interactionChains = 0;
	}
	
	@PostConstruct
	public void register() {
		registerInteractionChains();
	}
	
	public void registerInteractionChains() {
		context.getBeansWithAnnotation(InteractionChain.class).forEach((name, instance) -> {
			interactionChains++;
			final Method[] methods = instance.getClass().getMethods();
			for(Method method : methods) {
				if (method.isAnnotationPresent(InteractionCallback.class)) {
					InteractionCallback annotation = method.getAnnotation(InteractionCallback.class);
					String id = annotation.id();
					registerInteractionCallback(method, instance, id, annotation.annotationType().getName());
				}
			}
		});
		log.info("Successfully registered {} InteractionCallbacks across {} InteractionChains.", interactionCallbacks.size(), interactionChains);
	}
	
	private <T> void registerInteractionCallback(Method method, Object target, String id, String annotationClassName) {
		if(method.getParameters().length != 2) {
			log.error("Interaction Callback Method annotated with @{} must exactly have parameters like ..InteractionEvent and Arguments", annotationClassName);
			return;
		}
		
		if(interactionCallbacks.containsKey(id))
			log.warn("Overwriting existing interactionCallback at key id '{}' ('{}') with '{}'", id, interactionCallbacks.get(id).getName(), method.getName());
		
		interactionCallbacks.put(id, method);
		invokationTargets.put(id, target);
		log.debug("Registered Method '{}' to id '{}'", method.getName(), id);
	}
}
