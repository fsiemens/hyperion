package de.fabiansiemens.hyperion.core.features.roll.v2;

import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RollCacheEntry {

	private ExecutionNode node;
	private Long userId;
}
