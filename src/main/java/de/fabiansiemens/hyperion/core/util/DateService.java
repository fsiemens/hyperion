package de.fabiansiemens.hyperion.core.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.guild.GuildData;
import de.fabiansiemens.hyperion.core.guild.GuildService;
import de.fabiansiemens.hyperion.persistence.scheduler.RepeatCycle;
import net.dv8tion.jda.api.entities.Guild;

@Service
public class DateService {

	@Value("${datetime.default.date.pattern}")
	private String defaultDatePattern;
	
	@Value("${datetime.default.time.pattern}")
	private String defaultTimePattern;
	
	@Value("${datetime.default.timezone}")
	private String defaultTimezone;
	
	private GuildService guildService;
	
	public Date fromString(String string, Guild guild) throws DateTimeParseException {
		DateTimeFormatter formatter = getFormatter(guild);
		Instant instant = LocalDateTime.parse(string, formatter).atZone(ZoneId.of(defaultTimezone)).toInstant();
		return Date.from(instant);
	}
	
	public Date getRepeatDate(Date date, RepeatCycle cycle) {
		return Date.from( date.toInstant().plus(cycle.getTemporalAmount(), cycle.getTemporalUnit()) );
	}

	public DateTimeFormatter getFormatter(Guild guild) {
		Optional<GuildData> guildData = guildService.find(guild);
		Locale locale = Locale.GERMAN;
		if(guildData.isPresent())
			locale = guildData.get().getSettings().getLocale().toLocale();
		
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone(ZoneId.of(defaultTimezone)), locale);
		
		return new DateTimeFormatterBuilder()
				.optionalStart()
				.appendPattern(defaultDatePattern)
				.appendPattern(defaultTimePattern)
				.optionalEnd()
				.parseDefaulting(ChronoField.YEAR, now.get(Calendar.YEAR))
				.parseDefaulting(ChronoField.MONTH_OF_YEAR, now.get(Calendar.MONTH))
				.parseDefaulting(ChronoField.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))
				.parseDefaulting(ChronoField.HOUR_OF_DAY, now.get(Calendar.HOUR))
				.parseDefaulting(ChronoField.MINUTE_OF_HOUR, now.get(Calendar.MINUTE))
				.parseDefaulting(ChronoField.SECOND_OF_MINUTE, now.get(Calendar.SECOND))
				.toFormatter(locale);
	}

}
