package de.fabiansiemens.hyperion.core.features.draw;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

class DrawAlgorithmTest {

	private static List<Long> participants(int n) {
		return LongStream.rangeClosed(1, n).boxed().toList();
	}

	private static Map<Integer, Integer> histogram(Map<Long, Integer> assignment) {
		Map<Integer, Integer> counts = new HashMap<>();
		assignment.values().forEach(prize -> counts.merge(prize, 1, Integer::sum));
		return counts;
	}

	@Test
	void moreParticipantsThanSlots_allSlotsFilledRestDefault() {
		Map<Long, Integer> result = DrawAlgorithm.assign(participants(10), List.of(1, 2), new Random(1));

		assertEquals(10, result.size());
		Map<Integer, Integer> h = histogram(result);
		assertEquals(1, h.get(0));
		assertEquals(2, h.get(1));
		assertEquals(7, h.get(DrawAlgorithm.DEFAULT_PRIZE));
	}

	@Test
	void moreSlotsThanParticipants_nobodyGetsDefault() {
		Map<Long, Integer> result = DrawAlgorithm.assign(participants(4), List.of(2, 3), new Random(2));

		assertEquals(4, result.size());
		assertTrue(result.values().stream().noneMatch(p -> p == DrawAlgorithm.DEFAULT_PRIZE));
		Map<Integer, Integer> h = histogram(result);
		assertTrue(h.getOrDefault(0, 0) <= 2);
		assertTrue(h.getOrDefault(1, 0) <= 3);
	}

	@Test
	void noPrizes_everybodyGetsDefault() {
		Map<Long, Integer> result = DrawAlgorithm.assign(participants(3), List.of(), new Random(3));

		assertTrue(result.values().stream().allMatch(p -> p == DrawAlgorithm.DEFAULT_PRIZE));
	}

	@Test
	void keepsParticipantOrderAndIsReproducibleWithSeed() {
		List<Long> people = participants(8);
		Map<Long, Integer> a = DrawAlgorithm.assign(people, List.of(1, 1, 1), new Random(42));
		Map<Long, Integer> b = DrawAlgorithm.assign(people, List.of(1, 1, 1), new Random(42));

		assertEquals(people, List.copyOf(a.keySet()));
		assertEquals(a, b);
	}

	@Test
	void scarceSlots_leftOutSlotsAreRandom() {
		// 2 participants, slots: prize 0 twice, prize 1 twice -> both prizes must occur over many runs
		Random random = new Random(7);
		boolean sawOnlyPrize0 = false, sawOnlyPrize1 = false;
		for(int i = 0; i < 500; i++) {
			Map<Integer, Integer> h = histogram(DrawAlgorithm.assign(participants(2), List.of(2, 2), random));
			sawOnlyPrize0 |= h.getOrDefault(0, 0) == 2;
			sawOnlyPrize1 |= h.getOrDefault(1, 0) == 2;
		}
		assertTrue(sawOnlyPrize0 && sawOnlyPrize1);
	}

	@Test
	void sanitizeNeutralizesLocaleExpressions() {
		assertEquals("@​{.common.name}@", DrawService.sanitize("  @{.common.name}@ "));
		assertEquals(null, DrawService.sanitize(null));
	}
}
