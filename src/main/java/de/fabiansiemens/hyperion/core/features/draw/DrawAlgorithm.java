package de.fabiansiemens.hyperion.core.features.draw;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.random.RandomGenerator;

/**
 * Pure assignment logic of a draw, free of Discord and persistence.
 */
public final class DrawAlgorithm {

	/** Marks participants that receive the default prize. */
	public static final int DEFAULT_PRIZE = -1;

	private DrawAlgorithm() {}

	/**
	 * Assigns prize slots to participants.
	 * Every prize i provides {@code counts.get(i)} slots. All slots are shuffled together,
	 * so if there are more slots than participants, the slots left empty are chosen at random.
	 * Participants without a slot receive {@link #DEFAULT_PRIZE}.
	 *
	 * @return participant id -> prize index or DEFAULT_PRIZE, in participant order
	 */
	public static Map<Long, Integer> assign(List<Long> participants, List<Integer> counts, RandomGenerator random) {
		List<Integer> slots = new ArrayList<>();
		for(int prize = 0; prize < counts.size(); prize++) {
			for(int i = 0; i < counts.get(prize); i++)
				slots.add(prize);
		}

		List<Long> shuffled = new ArrayList<>(participants);
		Collections.shuffle(shuffled, random);
		Collections.shuffle(slots, random);

		Map<Long, Integer> assignment = new LinkedHashMap<>();
		for(Long participant : participants)
			assignment.put(participant, DEFAULT_PRIZE);

		for(int i = 0; i < shuffled.size() && i < slots.size(); i++)
			assignment.put(shuffled.get(i), slots.get(i));

		return assignment;
	}
}
