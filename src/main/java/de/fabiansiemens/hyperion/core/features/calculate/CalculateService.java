package de.fabiansiemens.hyperion.core.features.calculate;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.features.roll.RollService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class CalculateService {

	private final RollService rollServ;
	
	public int calculateMTL(int charLvl) {
		return Math.clamp(Math.ceilDiv(charLvl, 2), 1, 10);
	}
	
	public int calculateMML(int charLvl, int maxTechLvlUsed) {
		int mtl = calculateMTL(charLvl);
		return Math.clamp(mtl - maxTechLvlUsed, 0, 9);
	}

	public int calculateHealth(int charLvl, int hitDie, int conMod, boolean isTough, boolean isHillDwarf) throws RollException {
		int compoundMod = conMod + (isTough ? 2 : 0) + (isHillDwarf ? 1 : 0);
		return (hitDie + compoundMod) + ((charLvl - 1) * ((int) Math.ceil(rollServ.calculateAverage("d" +  hitDie)) + compoundMod));
	}
}
