package de.fabiansiemens.hyperion.core.features.roll.modifiers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RollReroll {
	
	public static enum Type {
		NON, SMALLER, SMALLER_OR_EQUAL, EQUAL, GREATER_OR_EQUAL, GREATER
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
	
	@Override
	public String toString() {
		if(this.isNon()) ;
		
		StringBuilder builder = new StringBuilder();
		builder.append("rr");
		switch(type) {
		case EQUAL:				builder.append("==");
			break;
		case GREATER:			builder.append(">");
			break;
		case GREATER_OR_EQUAL:	builder.append(">=");
			break;
		case SMALLER:			builder.append("<");
			break;
		case SMALLER_OR_EQUAL:	builder.append("<=");
			break;
		case NON:
		default: return "";
		}
		builder.append(threshhold);
		
		if(scope == ModifierScope.ALL)
			builder.append("all");
		
		return builder.toString();
	}
}
