package de.fabiansiemens.hyperion.persistence.scheduler;

import java.util.Date;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@NoArgsConstructor
@DiscriminatorValue("MESSAGE_TASK")
public class MessageTaskEntity extends TaskEntity {
	
	public MessageTaskEntity(String message, Long channelId, Long guildId, Date date) {
		super(date);
		this.message = message;
		this.channelId = channelId;
		this.guildId = guildId;
	}
	
	private String message;
	
	private Long channelId;
	
	private Long guildId;
	
	private boolean repeat;
	
	@Enumerated(EnumType.STRING)
	private RepeatCycle repeatCycle;

}
