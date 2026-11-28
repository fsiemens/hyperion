package de.fabiansiemens.hyperion.core.features.draw;

import java.time.Instant;
import java.util.Map;

/**
 * Result of one draw round. Held in memory only; prizes are snapshots taken at draw time.
 */
public record DrawResult(int round, Instant drawnAt, Map<Long, Prize> assignments, boolean revealed) {

	public record Prize(String name, String message, boolean isDefault) {}

	public DrawResult asRevealed() {
		return new DrawResult(round, drawnAt, assignments, true);
	}
}
