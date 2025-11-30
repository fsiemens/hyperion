package de.fabiansiemens.hyperion.core.scheduler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Date;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.jda.JDAManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class Test_SchedulingService {

	private final JDAManager jdaManager;
	private final SchedulingService schedulingService;
	
	@Autowired
	public Test_SchedulingService(JDAManager jdaManager, SchedulingService schedulingService) {
		this.schedulingService = schedulingService;
		this.jdaManager = jdaManager;
	}

	@Test
	public void testScheduling() throws InterruptedException {
		GuildMessageChannel channel = jdaManager.getJDA().getGuildById(496628232762687498L).getTextChannelById(741451565944012810L);
		MessageTask messageTask = schedulingService.createMessageTask("Test-Nachricht", channel, Date.from(Instant.now().plusSeconds(2)));
		this.schedulingService.schedule(messageTask);
		
		Thread.sleep(10_000L);
	}
	
	@Test
	public void testSchedulingForTimeString() throws InterruptedException {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd.MM.uuuu HH:mm[:ss]", Locale.GERMAN);
		Instant instant = LocalDateTime.parse("04.11.2024 02:30:15", df).atZone(ZoneId.of("Europe/Berlin")).toInstant();
		
		GuildMessageChannel channel = jdaManager.getJDA().getGuildById(496628232762687498L).getTextChannelById(741451565944012810L);
		MessageTask messageTask = schedulingService.createMessageTask("Test-Nachricht 2", channel, Date.from(instant) );
		this.schedulingService.schedule(messageTask);
		
		Thread.sleep(100_000L);
	}
	
	@Test
	public void testSchedulingForTimeStringWithDefaults() throws InterruptedException {
		DateTimeFormatter df = new DateTimeFormatterBuilder()
				.appendPattern("dd.MM.uuuu")
				.optionalStart()
				.appendPattern(" HH:mm[:ss]")
				.optionalEnd()
				.parseDefaulting(ChronoField.HOUR_OF_DAY, 2)
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, 36)
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
				.toFormatter(Locale.GERMAN);
		Instant instant = LocalDateTime.parse("04.11.2024", df).atZone(ZoneId.of("Europe/Berlin")).toInstant();
		
		GuildMessageChannel channel = jdaManager.getJDA().getGuildById(496628232762687498L).getTextChannelById(741451565944012810L);
		MessageTask messageTask = schedulingService.createMessageTask("Test-Nachricht 2", channel, Date.from(instant) );
		this.schedulingService.schedule(messageTask);
		
		Thread.sleep(100_000L);
	}
}
