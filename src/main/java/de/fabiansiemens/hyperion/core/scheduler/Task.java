package de.fabiansiemens.hyperion.core.scheduler;

import java.util.Date;
import java.util.TimerTask;

import de.fabiansiemens.hyperion.persistence.scheduler.TaskEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class Task extends TimerTask {
	
	public abstract void run();
	
	public abstract Date getDate();
	
	public abstract TaskEntity getEntity();
}
