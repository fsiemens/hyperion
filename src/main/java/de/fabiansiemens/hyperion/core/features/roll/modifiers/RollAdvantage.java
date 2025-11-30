package de.fabiansiemens.hyperion.core.features.roll.modifiers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RollAdvantage {
	
	public static enum Type {
		NON, ADVANTAGE, DISADVANTAGE
	}
	
	private Type type;
	private ModifierScope scope;
	
	public boolean isSingle() {
		return scope == ModifierScope.SINGLE;
	}

	public boolean isNon() {
		return type == Type.NON;
	}
	
	public boolean isAdvantage() {
		return type == Type.ADVANTAGE;
	}
	
	public boolean isDisadvantage() {
		return type == Type.DISADVANTAGE;
	}
	
	@Override
	public String toString() {
		if(this.isNon()) return "";
		
		StringBuilder builder = new StringBuilder();
		if(type == Type.ADVANTAGE)
			builder.append("adv");
		else
			builder.append("disadv");
		
		if(scope == ModifierScope.ALL)
			builder.append("all");
		
		return builder.toString();
	}
}
