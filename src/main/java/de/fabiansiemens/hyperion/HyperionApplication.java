package de.fabiansiemens.hyperion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import de.fabiansiemens.hyperion.core.systems.SystemManager;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = "de.fabiansiemens.hyperion.*")
public class HyperionApplication {

	@Getter
	private static ConfigurableApplicationContext context;
	private final SystemManager systemManager;
	
	public static void main(String[] args) {
		context = SpringApplication.run(HyperionApplication.class, args);
	} 
	
	public HyperionApplication(@NonNull final SystemManager systemManager) {
		this.systemManager = systemManager;
	}
	
	@EventListener(classes = ApplicationReadyEvent.class)
	public void start() {
		log.info("Starting Hyperion!");
		systemManager.launch();
	}
}
