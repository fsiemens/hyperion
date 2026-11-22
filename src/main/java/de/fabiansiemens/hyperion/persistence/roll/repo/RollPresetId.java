package de.fabiansiemens.hyperion.persistence.roll.repo;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RollPresetId {

	@Column(name = "user_id")
	private Long userId;
	@Column(nullable = false)
	private String name;
}
