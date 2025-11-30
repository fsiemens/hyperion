package de.fabiansiemens.hyperion.persistence.scheduler;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SchedulingPersistenceService {

	private final SchedulingRepository repos;
	
	public MessageTaskEntity create(Date date, String message, Long channelId, Long guildId) {
		return repos.save(new MessageTaskEntity(message, channelId, guildId, date));
	}
	
	public List<TaskEntity> findAll(){
		return repos.findAll();
	}
	
	public <T extends TaskEntity> T update(T task) {
		return repos.save(task);
	}
	
	public void delete(TaskEntity task) {
		repos.delete(task);
	}

}
