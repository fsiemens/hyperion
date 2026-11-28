package de.fabiansiemens.hyperion.persistence.draw;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A draw panel: owner, configuration and participants.
 * Results of a draw are intentionally not persisted.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "draw_session")
public class DrawEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private long guildId;

	@Column(nullable = false)
	private long channelId;

	/** Id of the public panel message; null until the panel was sent. */
	@Column(unique = true)
	private Long messageId;

	@Column(nullable = false)
	private long ownerId;

	/** null = unlimited */
	private Integer maxParticipants;

	/** null = localized default */
	@Column(length = 64)
	private String defaultPrizeName;

	@Column(length = 500)
	private String defaultPrizeMessage;

	private int round;

	private Instant lastDrawAt;

	@CreationTimestamp
	private Instant createdAt;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "draw_participant", joinColumns = @JoinColumn(name = "draw_id"))
	@OrderColumn(name = "position")
	@Column(name = "user_id", nullable = false)
	private List<Long> participants = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "draw_id")
	@OrderColumn(name = "position")
	private List<DrawPrizeEntity> prizes = new ArrayList<>();
}
