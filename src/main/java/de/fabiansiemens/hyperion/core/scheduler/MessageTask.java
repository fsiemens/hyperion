package de.fabiansiemens.hyperion.core.scheduler;

import java.util.Date;

import org.springframework.lang.Nullable;

import de.fabiansiemens.hyperion.persistence.scheduler.MessageTaskEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class MessageTask extends Task {

	private final JDA jda;
	private final MessageTaskEntity entity;

	@Override
	public void run() {
		TextChannel channel = getChannel();
		if(channel == null || !channel.canTalk() || entity.getMessage() == null || entity.getMessage().isBlank())
			return;
		
		channel.sendMessage(entity.getMessage()).queue();
	}
	
	public Date getDate() {
		return entity.getDate();
	}
	
	@Nullable
	public Guild getGuild() {
		return jda.getGuildById(entity.getGuildId());
	}
	
	@Nullable
	public TextChannel getChannel() {
		return getGuild().getTextChannelById(entity.getChannelId());
	}
}
