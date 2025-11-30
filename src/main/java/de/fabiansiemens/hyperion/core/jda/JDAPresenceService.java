package de.fabiansiemens.hyperion.core.jda;

import org.springframework.stereotype.Service;

import lombok.NonNull;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.managers.Presence;

@Service
public class JDAPresenceService {
	
	private final String activityText;
	private final ActivityType activityType;
	private final OnlineStatus onlineStatus;
	
	
	public JDAPresenceService(	@NonNull final String activityText, 
								@NonNull final ActivityType activityType,
								@NonNull final OnlineStatus onlineStatus) {
		this.activityText = activityText;
		this.activityType = activityType;
		this.onlineStatus = onlineStatus;
	}
	
	public Presence setDefaultPresence(JDA jda) {
		Presence presence = jda.getPresence();
		presence.setActivity(Activity.of(activityType, activityText));
		presence.setStatus(onlineStatus);
		return presence;
	}
	
}
