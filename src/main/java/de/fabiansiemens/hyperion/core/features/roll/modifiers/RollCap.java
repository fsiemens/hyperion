package de.fabiansiemens.hyperion.core.features.roll.modifiers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RollCap {

	public static enum Type {
		NON, MINIMUM, MAXIMUM
	}
	
	private Type type;
	private int threshhold;
	private ModifierScope scope;
	
	public boolean isSingle() {
		return scope == ModifierScope.SINGLE;
	}

	public boolean isNon() {
		return type == Type.NON;
	}
	
	public boolean isMinimum() {
		return type == Type.MINIMUM;
	}
	
	public boolean isMaximum() {
		return type == Type.MAXIMUM;
	}
	
	@Override
	public String toString() {
		if(this.isNon()) return "";
		
		StringBuilder builder = new StringBuilder();
		if(type == Type.MINIMUM)
			builder.append("min");
		else
			builder.append("max");
		
		builder.append(threshhold);
		
		if(scope == ModifierScope.ALL)
			builder.append("all");
		
		return builder.toString();
	}
}
