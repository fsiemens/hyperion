package de.fabiansiemens.hyperion.persistence.draw;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A role or prize of a draw, available {@code count} times per round.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "draw_prize")
public class DrawPrizeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 64)
	private String name;

	private int count;

	@Column(length = 500)
	private String message;

	public DrawPrizeEntity(String name, int count, String message) {
		this.name = name;
		this.count = count;
		this.message = message;
	}
}
