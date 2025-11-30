package de.fabiansiemens.hyperion.core.features.roll;

import java.util.Random;

import lombok.Getter;
import lombok.Setter;

public class Dice {	
	@Setter
	@Getter
	private int sides;
	private Random random;
	@Getter
	@Setter
	private int result;
	private IgnoreReason ignored;
	
	public Dice(final int sides) {
		this.sides = sides;
		this.result = 0;
		this.ignored = IgnoreReason.NON;
		this.random = new Random();
		roll();
	}
	
	private int roll() {
		this.result = this.random.nextInt(0, this.sides) +1;
		return this.result;
	}
	
	public void ignore(IgnoreReason ignore) {
		this.ignored = ignore;
	}
	
	public boolean isIgnored() {
		return this.ignored != IgnoreReason.NON;
	}
	
	public IgnoreReason getIgnoreReason() {
		return this.ignored;
	}
	
	@Override
	public String toString() {
		if(!isIgnored())
			return String.valueOf(getResult());
		
		StringBuilder out = new StringBuilder();
		out.append("~~")
			.append(getResult())
			.append("~~ *")
			.append(getIgnoreReason().getLabel())
			.append("*");
		return out.toString();
	}
}
