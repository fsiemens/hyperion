package de.fabiansiemens.hyperion.persistence.roll.repo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RollPresetEntity {

	@Id
	@GeneratedValue
	private Long id;
	@Column(name = "user_id", nullable = false)
	private Long userId;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String prompt;
}
