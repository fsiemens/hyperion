package de.fabiansiemens.hyperion.core.jda;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import de.fabiansiemens.hyperion.core.cli.LaunchArgumentService;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

@Slf4j
@Configuration
public class JDAConfiguration {
	
	private static final String COMMAND_PREFIX = "command-prefix";
	private static final String JDA_TOKEN = "jda.token";
	private static final String JDA_AUTORECONNECT = "jda.autoreconnect";
	private static final String JDA_REQUEST_TIMEOUT_RETRY = "jda.request-timeout-retry";
	private static final String JDA_CACHE_FLAGS = "jda.cache-flags";
	private static final String JDA_GATEWAY_INTENTS = "jda.gateway-intents";
	private static final String JDA_MEMBER_CACHE_POLICY = "jda.member-cache-policy";
	private static final String APPEARENCE_ACTIVITY_TYPE = "appearence.activity.type";
	private static final String APPEARENCE_ACTIVITY_TEXT = "appearence.activity.text";
	private static final String APPEARENCE_ONLINESTATUS = "appearence.onlinestatus";
	
	private static final String DEFAULT_COMMAND_SYMBOL = "!";
	private static final boolean DEFAULT_AUTO_RECONNECT = true;
	private static final boolean DEFAULT_REQUEST_TIMEOUT_RETRY = true;
	private static final List<CacheFlag> DEFAULT_FLAGS = List.of(CacheFlag.ONLINE_STATUS, CacheFlag.VOICE_STATE, CacheFlag.EMOJI);
	private static final List<GatewayIntent> DEFAULT_GATEWAY_INTENTS = List.of(
			GatewayIntent.GUILD_MEMBERS,
			GatewayIntent.GUILD_MESSAGES, 
			GatewayIntent.GUILD_MESSAGE_REACTIONS, 
			GatewayIntent.GUILD_PRESENCES,  
			GatewayIntent.GUILD_VOICE_STATES,
			GatewayIntent.MESSAGE_CONTENT
		);
	private static final ActivityType DEFAULT_ACTIVITY_TYPE = ActivityType.LISTENING;
	private static final MemberCachePolicy DEFAULT_MEMBER_CACHE_POLICY = MemberCachePolicy.ALL;
	private static final OnlineStatus DEFAULT_ONLINE_STATUS = OnlineStatus.ONLINE;
	
	@Bean(name = "eventManager")
	AnnotatedEventManager eventManager() {
		return new AnnotatedEventManager();
	}
	
	@Bean(name = "commandPrefix")
	String commandPrefix(@Autowired Environment env) {
		String symbol = env.getProperty(COMMAND_PREFIX);
		
		if(symbol == null || symbol.isBlank()) {
			log.warn("Command symbol not set. Using default: {}", DEFAULT_COMMAND_SYMBOL);
			return DEFAULT_COMMAND_SYMBOL;
		}
		
		log.debug("Using command symbol '{}'", symbol);
		return symbol;
	}
	
	@DependsOn("launchArgumentService")
	@Bean(name = "jdaToken")
	String jdaToken(@Autowired LaunchArgumentService las, @Autowired Environment env) {
		String jdaToken = env.getProperty(JDA_TOKEN);
		
		if(jdaToken != null)
			log.debug("Using token: {}...{}", jdaToken.substring(0, 3), jdaToken.substring(jdaToken.length() -3, jdaToken.length()));
		else
			log.error("No JDA-Token provided");
		
		log.info("Using Token: {}...", jdaToken.substring(0, 5));
		return jdaToken;
	}
	
	@Bean(name = "jdaAutoReconnect")
	boolean jdaAutoReconnect(@Autowired Environment env) {
		String value = env.getProperty(JDA_AUTORECONNECT);
		
		if(value == null) {
			log.warn("JDA auto reconnect not set. Using default: {}", DEFAULT_AUTO_RECONNECT);
			return DEFAULT_AUTO_RECONNECT;
		}
		
		log.debug("Using jda auto reconnect: '{}'", value);
		return Boolean.parseBoolean(value);
	}
	
	@Bean(name = "jdaRequestTimeoutRetry")
	boolean jdaRequestTimeoutRetry(@Autowired Environment env) {
		String value = env.getProperty(JDA_REQUEST_TIMEOUT_RETRY);
		
		if(value == null){
			log.warn("JDA auto reconnect not set. Using default: {}", DEFAULT_REQUEST_TIMEOUT_RETRY);
			return DEFAULT_REQUEST_TIMEOUT_RETRY;
		}
		
		log.debug("Using jda request timeout retry: '{}'", value);
		return Boolean.parseBoolean(value);
	}
	
	@Bean(name = "jdaCacheFlags")
	List<CacheFlag> jdaCacheFlags(@Autowired Environment env) {	
		List<CacheFlag> cacheFlags = new LinkedList<CacheFlag>();
		String propertyValue = env.getProperty(JDA_CACHE_FLAGS);
		String[] flagStrings = propertyValue.split(",");
		
		if(flagStrings == null) {
			log.warn("Cache flags not set. Using default: {}", DEFAULT_FLAGS);
			return DEFAULT_FLAGS;
		}
		
		for(String elem : flagStrings) {
			elem = elem.strip().toUpperCase();
			try {
				cacheFlags.add(CacheFlag.valueOf(elem));
			}
			catch(Exception e) {
				continue;
			}
		}
		
		if(cacheFlags.isEmpty()) {
			log.warn("Cache flags not set. Using default: {}", DEFAULT_FLAGS);
			return DEFAULT_FLAGS;
		}
		
		log.debug("Using cache flags: '{}'", cacheFlags);
		return cacheFlags;
	}

	@Bean(name = "jdaGatewayIntents")
	List<GatewayIntent> jdaGatewayIntents(@Autowired Environment env) {
		List<GatewayIntent> gatewayIntents = new LinkedList<GatewayIntent>();
		String propertyValue = env.getProperty(JDA_GATEWAY_INTENTS);
		String[] intentStrings = propertyValue.split(",");
		
		if(intentStrings == null) {
			log.warn("Gateway Intents not set. Using default: {}", DEFAULT_GATEWAY_INTENTS);
			return DEFAULT_GATEWAY_INTENTS;
		}
		
		for(String elem : intentStrings) {
			elem = elem.strip().toUpperCase();
			try {
				gatewayIntents.add(GatewayIntent.valueOf(elem));
			}
			catch(Exception e) {
				continue;
			}
		}
		
		if(gatewayIntents.isEmpty()) {
			log.warn("Gateway Intents not set. Using default: {}", DEFAULT_GATEWAY_INTENTS);
			return DEFAULT_GATEWAY_INTENTS;
		}
		
		log.debug("Using gateway intents: '{}'", gatewayIntents);
		return gatewayIntents;
	}
	
	@Bean(name = "jdaMemberCachePolicy")
	MemberCachePolicy jdaMemberCachePolicy(@Autowired Environment env) {
		String propertyValue = env.getProperty(JDA_MEMBER_CACHE_POLICY);
		
		if(propertyValue == null) {
			log.warn("Member Cache policy not set. Using default: {}", DEFAULT_MEMBER_CACHE_POLICY);
			return DEFAULT_MEMBER_CACHE_POLICY;
		}
		
		List<MemberCachePolicy> enabledPolicies = new LinkedList<MemberCachePolicy>();
		String[] policyStrings = propertyValue.split(",");
		for(String elem : policyStrings) {
			elem = elem.strip().toUpperCase();
			MemberCachePolicy policy = switch(elem) {
				case "NONE" -> MemberCachePolicy.NONE;
				case "DEFAULT" -> MemberCachePolicy.DEFAULT;
				case "OWNER" -> MemberCachePolicy.OWNER;
				case "BOOSTER" -> MemberCachePolicy.BOOSTER;
				case "PENDING" -> MemberCachePolicy.PENDING;
				case "VOICE" -> MemberCachePolicy.VOICE;
				case "ONLINE" -> MemberCachePolicy.ONLINE;
				case "ALL" -> MemberCachePolicy.ALL;
				default -> MemberCachePolicy.ALL;
			};
			enabledPolicies.add(policy);
		}
		
		log.debug("Using member cache policys: '{}'", propertyValue);
		MemberCachePolicy[] policies = enabledPolicies.toArray(new MemberCachePolicy[0]);
		return MemberCachePolicy.any(MemberCachePolicy.NONE, policies);
	}
	
	@Bean(name = "activityType")
	ActivityType activityType(@Autowired Environment env) {
		String propertyValue = env.getProperty(APPEARENCE_ACTIVITY_TYPE);
		
		if(propertyValue == null) {
			log.warn("Activity Type not set. Using default: {}", DEFAULT_ACTIVITY_TYPE);
			return DEFAULT_ACTIVITY_TYPE;
		}
		
		try {
			ActivityType type = ActivityType.valueOf(propertyValue);
			log.debug("Using activity type: '{}'", type);
			return type;
		}
		catch(Exception e) {
			log.warn("Activity Type not set. Using default: {}", DEFAULT_ACTIVITY_TYPE);
			return DEFAULT_ACTIVITY_TYPE;
		}
	}
	
	@DependsOn("commandPrefix")
	@Bean(name = "activityText")
	String activityText(@Autowired String commandPrefix, @Autowired Environment env){
		String activityText = env.getProperty(APPEARENCE_ACTIVITY_TEXT);
		
		if(activityText == null || activityText.isBlank()) {
			String defaultText = commandPrefix + "help und /help";
			log.warn("Activity Text not set. Using default: {}", defaultText);
			return defaultText;
		}
			
		log.debug("Using activity text: '{}'", activityText);
		return activityText;
	}
	
	@Bean(name = "onlineStatus")
	OnlineStatus onlineStatus(@Autowired Environment env) {
		String propertyValue = env.getProperty(APPEARENCE_ONLINESTATUS);
		
		if(propertyValue == null) {
			log.warn("Online status not set. Using default: {}", DEFAULT_ONLINE_STATUS);
			return DEFAULT_ONLINE_STATUS;
		}
		
		try {
			OnlineStatus status = OnlineStatus.valueOf(propertyValue);
			log.debug("Using online status: '{}'", status);
			return status;
		}
		catch(Exception e) {
			log.warn("Online status not set. Using default: {}", DEFAULT_ONLINE_STATUS);
			return DEFAULT_ONLINE_STATUS;
		}
	}
}
