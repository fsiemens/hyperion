package de.fabiansiemens.hyperion.core.jda;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.systems.ManageableSystem;
import de.fabiansiemens.hyperion.core.systems.SystemAvailability;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDA.Status;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.IEventManager;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Slf4j
@Service
public class JDAManager implements ManageableSystem {
	
	private final ConfigurableApplicationContext context;
	private final String jdaToken;
	private final List<CacheFlag> jdaCacheFlags;
	private final MemberCachePolicy jdaMemberCachePolicy;
	private final List<GatewayIntent> jdaGatewayIntents;
	private final boolean jdaAutoReconnect;
	private final boolean jdaRequestTimeoutRetry;
	private final JDAPresenceService jdaPresenceService;
	private final IEventManager eventManager;
	private final VoiceDispatchInterceptor voiceDispatchInterceptor;
	
	@Nullable
	private JDA jda;
	private volatile SystemAvailability availability;
	
	public JDAManager(	@NonNull final ConfigurableApplicationContext context,
						@NonNull final String jdaToken, 
						@NonNull final List<CacheFlag> jdaCacheFlags, 
						@NonNull final MemberCachePolicy jdaMemberCachePolicy,
						@NonNull final List<GatewayIntent> jdaGatewayIntents,
						final boolean jdaAutoReconnect,
						final boolean jdaRequestTimeoutRetry,
						@NonNull final JDAPresenceService jdaPresenceService,
						@NonNull final IEventManager eventManager,
						@NonNull final VoiceDispatchInterceptor voiceDispatchInterceptor
						) {
		
		this.context = context;
		this.jdaToken = jdaToken;
		this.jdaCacheFlags = jdaCacheFlags;
		this.jdaMemberCachePolicy = jdaMemberCachePolicy;
		this.jdaGatewayIntents = jdaGatewayIntents;
		this.jdaAutoReconnect = jdaAutoReconnect;
		this.jdaRequestTimeoutRetry = jdaRequestTimeoutRetry;
		this.jdaPresenceService = jdaPresenceService;
		this.eventManager = eventManager;
		this.voiceDispatchInterceptor = voiceDispatchInterceptor;
		
		this.availability = SystemAvailability.NOT_LAUNCHED;
	}

	@Override
	public void launch() {
		log.info("Launching JDA");
		
		this.jda = JDABuilder.createDefault(jdaToken)
			.enableCache(jdaCacheFlags)
			.enableIntents(jdaGatewayIntents)
			.setMemberCachePolicy(jdaMemberCachePolicy)
			.setAutoReconnect(jdaAutoReconnect)
			.setRequestTimeoutRetry(jdaRequestTimeoutRetry)
			.setEventManager(eventManager)
			.setVoiceDispatchInterceptor(voiceDispatchInterceptor)
			.addEventListeners(retrieveListeners().toArray())
			.build();
		
		jdaPresenceService.setDefaultPresence(this.jda);
		
		try {
			log.info("Awaiting JDA launch completion...");
			jda.awaitReady();
		} catch (InterruptedException e) {
			log.warn("JDAManager was interrupted waiting for launch completion. {}", e.getLocalizedMessage());
		}
		this.availability = SystemAvailability.AVAILABLE;
		log.info("Done launching JDA");
	}

	@Override
	public boolean isAvailable() {
		return 		this.jda != null 
				&& 	this.jda.getStatus() == Status.CONNECTED 
				&& 	this.availability == SystemAvailability.AVAILABLE;
	}
	
	@Override
	public SystemAvailability getAvailabilityState() {
		return availability;
	}
	
	@Nullable
	public JDA getJDA() {
		return this.jda;
	}

	@Override
	public void onError(ManageableSystem system, Throwable throwable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivate(boolean state) {
		if(this.availability == SystemAvailability.NOT_LAUNCHED 
				|| this.availability == SystemAvailability.CRITICAL_ERROR 
				|| this.availability == SystemAvailability.SHUTDOWN)
			throw new IllegalStateException(this.getClass().getSimpleName() + " is not in a state to manually change availability. Consider calling launch() or shutdown().");
		
		this.availability = (state ? SystemAvailability.DEACTIVATED : SystemAvailability.AVAILABLE);
	}

	@Override
	public void shutdown() {
		this.jda.shutdown();
		try {
			this.jda.awaitShutdown(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.warn("JDAManager was interrupted waiting for shutdown completion. {}", e.getLocalizedMessage());
		}
		this.availability = SystemAvailability.SHUTDOWN;
	}
	
	private List<Object> retrieveListeners() {
		log.info("Searching for Listeners...");
		var scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(JdaListener.class));
		
		Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents("de.fabiansiemens.hyperion");
		log.info("Found {} potential Listeners", beanDefinitions.size());
		
		List<Object> listeners = new LinkedList<Object>();
		int count = 0;
		for(BeanDefinition definition : beanDefinitions) {
			Class<?> clazz;
			try {
				clazz = Class.forName(definition.getBeanClassName());
				Object bean = context.getBean(clazz);
				listeners.add(bean);
				count++;
			} catch (Exception e) {
				log.warn("Loading of Listener failed: {}", e.getLocalizedMessage());
			}
		}
		log.info("Added {} Listeners", count);
		return listeners;
	}
}
