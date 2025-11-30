package de.fabiansiemens.hyperion.core.roll;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.features.roll.Roll;
import de.fabiansiemens.hyperion.core.features.roll.RollBundle;
import de.fabiansiemens.hyperion.core.features.roll.RollService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RollServiceTests {

	private RollService rollService;

	@Autowired
	public RollServiceTests(final RollService rollService) {
		this.rollService = rollService;
	}
	
	@Test
	public void testRoll() throws RollException {
		RollBundle bundle = rollService.compileRoll("d1 d1000 2d2 200d200 d20+2 d20 -2000 3d20 +2all 3d20-4_all 3d20 -200all 5d20+200_all d20+4 adv d20 -40all disadv 3d20 advall 2d20 disadv_all 2d20   disadvall 2d20 adv_all d20+4max3 d20 min19 d20 \"Label\"");
		//RollBundle bundle = rollService.compileRoll("2d20 max5 d20 max3 4d20 max18 2d20 min15");
		for(Roll result : bundle.getResults()) {
			System.out.println(result.toString());
		}
		
		assertTrue(true);
	}
	
	@Test
	public void testRollAverages() throws RollException {
		final int sides = 20;
		final int amount = 1000000;
		double res = 0.0;
		double avg = 0.0;
		
		RollBundle standardBundle = rollService.compileRoll(amount + "d" + sides);
		res = standardBundle.getTotalResult() / (double) amount;
		avg = rollService.calculateAverage("d" + sides);
		log.info("Simulated Standard Average: {}. Calculated average: {}. Deviation to calculated average: {}", res, avg, res - avg);
		
		RollBundle advBundle = rollService.compileRoll(amount + "d" + sides + " advall");
		res = advBundle.getTotalResult() / (double) amount;
		avg = rollService.calculateAverage("d" + sides + " adv");
		log.info("Simulated Advantage Average: {}. Calculated average: {}. Deviation to calculated average: {}", res, avg, res - avg);
		
		RollBundle disadvBundle = rollService.compileRoll(amount + "d" + sides + " disadvall");
		res = disadvBundle.getTotalResult() / (double) amount;
		avg = rollService.calculateAverage("d" + sides + " disadv");
		log.info("Simulated Disadvantage Average: {}. Calculated average: {}. Deviation to calculated average: {}", res, avg, res - avg);
		
		RollBundle rr1Bundle = rollService.compileRoll(amount + "d" + sides + " rr<=1all");
		res = rr1Bundle.getTotalResult() / (double) amount;
		avg = rollService.calculateAverage("d" + sides + " rr<=1");
		log.info("Reroll <= 1 Average: {}. Deviation to theoretical standard: {}", res, res - avg);
		
		RollBundle rr3Bundle = rollService.compileRoll(amount + "d" + sides + " rr<=3all");
		res = rr3Bundle.getTotalResult() / (double) amount;
		//avg = rollService.calculateAverage("d" + sides + " rr<=3");
		log.info("Reroll <= 3 Average: {}. Deviation to theoretical standard: {}", res, res - 10.5);
		
		RollBundle min1Bundle = rollService.compileRoll(amount + "d" + sides + " min1all");
		res = min1Bundle.getTotalResult() / (double) amount;
		//avg = rollService.calculateAverage("d" + sides + " min1");
		log.info("Minimum 1 Average: {}. Deviation to theoretical standard: {}", res, res - 10.5);
	}
}
