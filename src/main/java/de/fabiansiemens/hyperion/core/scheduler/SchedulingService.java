package de.fabiansiemens.hyperion.core.scheduler;

import java.util.Date;
import java.util.Timer;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.scheduler.MessageTaskEntity;
import de.fabiansiemens.hyperion.persistence.scheduler.SchedulingPersistenceService;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

@Service
public class SchedulingService {

	private final Timer timer;
	private final SchedulingPersistenceService persistenceService;
	
	public SchedulingService(SchedulingPersistenceService persistenceService) {
		this.timer = new Timer();
		this.persistenceService = persistenceService;
	}
	
	public MessageTask createMessageTask(String message, GuildMessageChannel channel, Date date) {
		MessageTaskEntity mte = persistenceService.create(date, message, channel.getIdLong(), channel.getGuild().getIdLong());
		return new MessageTask(channel.getJDA(), mte);
	}
	
	public void schedule(Task task) {
		//TODO store in DB
		persistenceService.update(task.getEntity());
		//TODO proceed
		timer.schedule(task, task.getDate());
	}

}
