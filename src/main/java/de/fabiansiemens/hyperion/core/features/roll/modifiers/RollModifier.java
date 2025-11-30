package de.fabiansiemens.hyperion.core.features.roll.modifiers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RollModifier {
	private int value;
	private ModifierScope scope;
	
	public boolean isSingle() {
		return scope == ModifierScope.SINGLE;
	}
	
	@Override
	public String toString() {
		return (value >= 0 ? "+" : "") + value;
	}
}
