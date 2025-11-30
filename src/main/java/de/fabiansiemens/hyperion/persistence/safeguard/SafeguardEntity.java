package de.fabiansiemens.hyperion.persistence.safeguard;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SafeguardEntity {

	public SafeguardEntity(LocalTime unlockTime, LocalTime lockTime, String reason) {
		this.unlockTime = unlockTime;
		this.lockTime = lockTime;
		this.reason = reason;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long id;
	private LocalTime unlockTime;
	private LocalTime lockTime;
	private String reason;

}
