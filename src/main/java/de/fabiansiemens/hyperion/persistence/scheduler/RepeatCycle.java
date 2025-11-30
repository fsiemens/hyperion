package de.fabiansiemens.hyperion.persistence.scheduler;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RepeatCycle {
	YEARLY(ChronoUnit.YEARS, 1),
	QUARTERLY(ChronoUnit.MONTHS, 4),
	MONTHLY(ChronoUnit.MONTHS, 1),
	BI_WEEKLY(ChronoUnit.WEEKS, 2),
	WEEKLY(ChronoUnit.WEEKS, 1),
	DAILY(ChronoUnit.DAYS, 1),
	;
	
	private TemporalUnit temporalUnit;
	private long temporalAmount;
}
