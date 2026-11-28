package de.fabiansiemens.hyperion.core.features.draw;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.random.RandomGenerator;

import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.exceptions.DrawException;
import de.fabiansiemens.hyperion.persistence.draw.DrawEntity;
import de.fabiansiemens.hyperion.persistence.draw.DrawPersistenceService;
import de.fabiansiemens.hyperion.persistence.draw.DrawPrizeEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages draw panels: participants, prizes and drawing.
 * All mutating operations of one draw are serialized by a per-draw lock.
 */
@Slf4j
@Service
public class DrawService {

	public static final int MAX_NAME_LENGTH = 64;
	public static final int MAX_MESSAGE_LENGTH = 500;
	public static final int MAX_PRIZES = 25;
	public static final int MAX_PRIZE_COUNT = 100;
	public static final int MAX_PARTICIPANT_LIMIT = 1000;

	private final DrawPersistenceService persistence;
	private final RandomGenerator random = new SecureRandom();
	private final Map<Long, Object> locks = new ConcurrentHashMap<>();
	private final Map<Long, DrawResult> results = new ConcurrentHashMap<>();

	public DrawService(DrawPersistenceService persistence) {
		this.persistence = persistence;
	}

	public DrawEntity create(long guildId, long channelId, long ownerId) {
		DrawEntity draw = new DrawEntity();
		draw.setGuildId(guildId);
		draw.setChannelId(channelId);
		draw.setOwnerId(ownerId);
		return persistence.update(draw);
	}

	public DrawEntity attachMessage(long drawId, long messageId) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			draw.setMessageId(messageId);
			return persistence.update(draw);
		}
	}

	public DrawEntity get(long drawId) {
		return persistence.findById(drawId).orElseThrow(() -> new DrawException("error.draw.not-found"));
	}

	public boolean isOwner(DrawEntity draw, long userId) {
		return draw.getOwnerId() == userId;
	}

	public void requireOwner(DrawEntity draw, long userId) {
		if(!isOwner(draw, userId))
			throw new DrawException("error.draw.not-owner");
	}

	public boolean isFull(DrawEntity draw) {
		return draw.getMaxParticipants() != null && draw.getParticipants().size() >= draw.getMaxParticipants();
	}

	// ===== Participants =====

	public DrawEntity join(long drawId, long userId) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			if(draw.getParticipants().contains(userId))
				throw new DrawException("error.draw.already-joined");
			if(isFull(draw))
				throw new DrawException("error.draw.full");
			draw.getParticipants().add(userId);
			return persistence.update(draw);
		}
	}

	public DrawEntity leave(long drawId, long userId) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			if(!draw.getParticipants().remove(userId))
				throw new DrawException("error.draw.not-joined");
			return persistence.update(draw);
		}
	}

	/** @return number of removed participants */
	public int removeParticipants(long drawId, long actorId, Collection<Long> userIds) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			int before = draw.getParticipants().size();
			draw.getParticipants().removeAll(userIds);
			int removed = before - draw.getParticipants().size();
			if(removed > 0)
				persistence.update(draw);
			return removed;
		}
	}

	// ===== Configuration =====

	public DrawEntity addPrize(long drawId, long actorId, String name, int count, @Nullable String message) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			if(draw.getPrizes().size() >= MAX_PRIZES)
				throw new DrawException("error.draw.too-many-prizes");
			draw.getPrizes().add(new DrawPrizeEntity(validateName(name), validateCount(count), validateMessage(message)));
			return persistence.update(draw);
		}
	}

	public DrawEntity updatePrize(long drawId, long actorId, long prizeId, String name, int count, @Nullable String message) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			DrawPrizeEntity prize = findPrize(draw, prizeId);
			prize.setName(validateName(name));
			prize.setCount(validateCount(count));
			prize.setMessage(validateMessage(message));
			return persistence.update(draw);
		}
	}

	public DrawEntity removePrize(long drawId, long actorId, long prizeId) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			draw.getPrizes().remove(findPrize(draw, prizeId));
			return persistence.update(draw);
		}
	}

	/** Blank name resets to the localized default. */
	public DrawEntity setDefaultPrize(long drawId, long actorId, @Nullable String name, @Nullable String message) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			draw.setDefaultPrizeName(name == null || name.isBlank() ? null : validateName(name));
			draw.setDefaultPrizeMessage(validateMessage(message));
			return persistence.update(draw);
		}
	}

	/** null = unlimited. Lowering below the current count only blocks new joins. */
	public DrawEntity setMaxParticipants(long drawId, long actorId, @Nullable Integer max) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			if(max != null && (max < 1 || max > MAX_PARTICIPANT_LIMIT))
				throw new DrawException("error.draw.invalid-max");
			draw.setMaxParticipants(max);
			return persistence.update(draw);
		}
	}

	public DrawPrizeEntity findPrize(DrawEntity draw, long prizeId) {
		return draw.getPrizes().stream()
				.filter(prize -> prize.getId() == prizeId)
				.findFirst()
				.orElseThrow(() -> new DrawException("error.draw.prize-not-found"));
	}

	// ===== Drawing =====

	/**
	 * Draws a new round. Participants and configuration stay unchanged.
	 * @param defaultPrizeName localized name used if the owner did not set one
	 */
	public DrawResult draw(long drawId, long actorId, String defaultPrizeName) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			if(draw.getParticipants().isEmpty())
				throw new DrawException("error.draw.no-participants");

			List<DrawPrizeEntity> prizes = draw.getPrizes();
			Map<Long, Integer> assignment = DrawAlgorithm.assign(
					draw.getParticipants(),
					prizes.stream().map(DrawPrizeEntity::getCount).toList(),
					random);

			DrawResult.Prize defaultPrize = new DrawResult.Prize(
					draw.getDefaultPrizeName() != null ? draw.getDefaultPrizeName() : defaultPrizeName,
					draw.getDefaultPrizeMessage(),
					true);

			Map<Long, DrawResult.Prize> assignments = new LinkedHashMap<>();
			assignment.forEach((userId, index) -> {
				if(index == DrawAlgorithm.DEFAULT_PRIZE) {
					assignments.put(userId, defaultPrize);
					return;
				}
				DrawPrizeEntity prize = prizes.get(index);
				assignments.put(userId, new DrawResult.Prize(prize.getName(), prize.getMessage(), false));
			});

			Instant now = Instant.now();
			draw.setRound(draw.getRound() + 1);
			draw.setLastDrawAt(now);
			persistence.update(draw);

			DrawResult result = new DrawResult(draw.getRound(), now, Map.copyOf(assignments), false);
			results.put(drawId, result);
			log.info("Draw {} round {}: {} participants, {} prize slots", drawId, result.round(), assignments.size(),
					prizes.stream().mapToInt(DrawPrizeEntity::getCount).sum());
			return result;
		}
	}

	public DrawResult reveal(long drawId, long actorId) {
		synchronized(lock(drawId)) {
			DrawEntity draw = get(drawId);
			requireOwner(draw, actorId);
			DrawResult result = getResult(drawId).orElseThrow(() -> new DrawException("error.draw.no-result"));
			DrawResult revealed = result.asRevealed();
			results.put(drawId, revealed);
			return revealed;
		}
	}

	public Optional<DrawResult> getResult(long drawId) {
		return Optional.ofNullable(results.get(drawId));
	}

	// ===== Helpers =====

	private Object lock(long drawId) {
		return locks.computeIfAbsent(drawId, id -> new Object());
	}

	private String validateName(String name) {
		String value = sanitize(name);
		if(value == null || value.isBlank())
			throw new DrawException("error.draw.invalid-name");
		if(value.length() > MAX_NAME_LENGTH)
			throw new DrawException("error.draw.invalid-name");
		return value;
	}

	private int validateCount(int count) {
		if(count < 1 || count > MAX_PRIZE_COUNT)
			throw new DrawException("error.draw.invalid-count");
		return count;
	}

	private String validateMessage(@Nullable String message) {
		String value = sanitize(message);
		if(value == null || value.isBlank())
			return null;
		if(value.length() > MAX_MESSAGE_LENGTH)
			throw new DrawException("error.draw.invalid-message");
		return value;
	}

	/**
	 * Trims user input and neutralizes the locale expression marker "@{",
	 * so user text is never resolved as a locale expression.
	 */
	static String sanitize(@Nullable String input) {
		if(input == null)
			return null;
		return input.strip().replace("@{", "@​{");
	}
}
