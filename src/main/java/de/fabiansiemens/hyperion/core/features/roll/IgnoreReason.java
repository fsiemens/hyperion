package de.fabiansiemens.hyperion.core.features.roll;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum IgnoreReason {
	NON("Not Ignored"),
	ADVANTAGE("Advantage"), 
	DISADVANTAGE("Disadvantage"), 
	REROLL("Reroll"),
	MINIMUM("Below Minimum"),
	MAXIMUM("Above Maximum");
	
	@Getter
	private String label;
}
