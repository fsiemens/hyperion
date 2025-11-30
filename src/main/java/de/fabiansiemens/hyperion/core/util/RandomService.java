package de.fabiansiemens.hyperion.core.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.springframework.stereotype.Service;

@Service
public class RandomService {

	private final Random random;
	
	public RandomService() {
		this.random = new Random();
	}

	public boolean isTrue(double perc) {
		double result = random.nextDouble(100);
		return result < perc;
	}
	
	public int outOf(int ceil) {
		return random.nextInt(ceil);
	}
	
	public <T> T oneOutOf(T... elements) {
		int size = elements.length;
		int index = random.nextInt(size);
		return elements[index];
	}
	
	public <T> T oneOutOfWeighted(Map<T, Double> weightedElements) {
		double totalProb = 0.0;
		for(Entry<T, Double> entry : weightedElements.entrySet()) {
			totalProb += entry.getValue();
		}
		if(totalProb > 100.0001 || totalProb < 100)
			throw new IllegalArgumentException("Total Probability of all weighted elements must be 100%");
		
		Double result = random.nextDouble(100);
		Double c = 0.0;
		for(Entry<T, Double> entry : weightedElements.entrySet()) {
			c += entry.getValue();
			if(result < c)
				return entry.getKey();
		}
		return null;
	}
}
