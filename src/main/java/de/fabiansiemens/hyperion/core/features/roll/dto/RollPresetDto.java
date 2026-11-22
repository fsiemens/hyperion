package de.fabiansiemens.hyperion.core.features.roll.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RollPresetDto {

	private Long id;
	private Long userId;
	private String name;
	private String prompt;
}
