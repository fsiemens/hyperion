package de.fabiansiemens.hyperion.core.features.safeguard;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.persistence.safeguard.SafeguardEntity;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

@Service
@Slf4j
@JdaListener
@AllArgsConstructor
public class SafeguardService {

	private final UserService userService;
	private final LocalizedExpressionService les;

	@Nullable
	public SafeguardEntity getSafeguard(User user) {
		Optional<UserData> data = userService.findByUser(user);
		
		if(data.isEmpty())
			return null;
	
		UserSettings settings = data.get().getSettings();
		return settings.getSafeguard();
	}
	
	@SubscribeEvent
	public void onMemberVoiceChatJoin(GuildVoiceUpdateEvent event) {
		if(event.getChannelJoined() == null)
			return;
		
		Member member = event.getMember();
		
		SafeguardEntity safeguard = getSafeguard(member.getUser());
		
		if(safeguard == null)
			return;
		
		LocalTime currentTime = LocalTime.now();
		
		// 10 - 20 Uhr ist erlaubt --> c > 10 && c < 20
		// 22 - 04 Uhr ist erlaubt --> c > 22 || c < 04
		if( 	(safeguard.getUnlockTime().isBefore(safeguard.getLockTime()) && currentTime.isAfter(safeguard.getUnlockTime()) && currentTime.isBefore(safeguard.getLockTime())) 
			|| 	(safeguard.getUnlockTime().isAfter(safeguard.getLockTime()) && (currentTime.isAfter(safeguard.getUnlockTime()) || currentTime.isBefore(safeguard.getLockTime()))) )
			return;
		
		if(!event.getGuild().getSelfMember().canInteract(member)) {
			log.info("Could not timeout member '{}' on guild '{}', because of insufficient permissions", member.getEffectiveName(), event.getGuild().getName());
			PrivateChannel channel = member.getUser().openPrivateChannel().complete();
			channel.sendMessage(les.getLocalizedExpression("error.safeguard.failed", event.getGuild())).queue();
			return;
		}
		
		member.timeoutFor(1, TimeUnit.MINUTES).queue();
		PrivateChannel channel = member.getUser().openPrivateChannel().complete();
		Arguments args = Arguments.of("guild", event.getGuild().getName())
				.put("lock", safeguard.getLockTime().toString())
				.put("unlock", safeguard.getUnlockTime().toString())
				.put("reason", safeguard.getReason());
		
		
		channel.sendMessage(les.getLocalizedExpression(".safeguard.triggered", event.getGuild(), args)).queue();
	}
}
