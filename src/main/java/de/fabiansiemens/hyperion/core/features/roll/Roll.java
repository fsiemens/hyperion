package de.fabiansiemens.hyperion.core.features.roll;

import java.util.LinkedList;
import java.util.List;

import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.ModifierScope;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollAdvantage;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollCap;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollCheck;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollModifier;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollReroll;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollReroll.Type;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;

@Slf4j
@Getter
public class Roll {
	
	private List<Dice> results;
	
	@Setter
	private String prompt;
	private int amount;
	private int sides;
	private RollModifier modifier;
	@Setter
	private RollAdvantage advantage;
	@Setter
	private RollReroll reroll;
	@Setter
	private RollCap rollCap;
	private int critThreshold;
	private int failThreshold;
	private RollCheck rollCheck;
	
	public Roll() {
		this(1, 20);
	}
	
	public Roll(int amount, int sides) {
		this(amount, sides, new RollModifier(0, ModifierScope.SINGLE));
	}
	
	public Roll(int amount, int sides, RollModifier modifier) {
		this(
			amount,
			sides, 
			modifier,
			new RollAdvantage(RollAdvantage.Type.NON, ModifierScope.SINGLE),
			new RollCap(RollCap.Type.NON, 0, ModifierScope.SINGLE),
			new RollReroll(RollReroll.Type.NON, 0, ModifierScope.SINGLE),
			new RollCheck(RollCheck.Type.NON, 0, ModifierScope.SINGLE)
		);
	}
	
	public Roll(int amount, int sides, RollModifier modifier, RollAdvantage advantage, RollCap cap, RollReroll reroll, RollCheck check) {
		this(
			amount, 
			sides, 
			modifier, 
			advantage, 
			cap, 
			reroll, 
			check,
			1,
			20
		);
	}
	
	public Roll(int amount, int sides, RollModifier modifier, RollAdvantage advantage, RollCap cap, RollReroll reroll, RollCheck check, int failThreshold, int critThreshold) {
		this.amount = amount;
		this.sides = sides;
		this.modifier = modifier;
		this.advantage = advantage;
		this.rollCap = cap;
		this.reroll = reroll;
		this.rollCheck = check;
		this.critThreshold = critThreshold;
		this.failThreshold = failThreshold;
	}

	public void rollDice() {
		List<Dice> firstRound = new LinkedList<Dice>();
		for(int i = 0; i < this.amount; i++) {
			firstRound.add(new Dice(this.sides));
		}
		this.results = firstRound;
		this.results = applyAdvantage(this.results, this.advantage);
		this.results = applyReroll(this.results, this.reroll);
		this.results = applyCap(this.results, this.rollCap);	
		this.rollCheck = applyRollCheck(this.results, this.modifier, this.rollCheck);
	}
	
	public String getPrompt() {
		if(this.prompt == null)
			return reconstructPrompt();
		return this.prompt;
	}
	
	public double getAverage() throws RollException {
		int modificatorCount = 0;
		if(!advantage.isNon())
			modificatorCount++;
		if(!rollCap.isNon())
			modificatorCount++;
		if(!reroll.isNon())
			modificatorCount++;
		
		if(modificatorCount > 1)
			throw new RollException("error.calculate.average.too-many-modificators", null);
		
		double baseDieAvg = (sides + 1) / 2.0;
		double firstDieAvg = baseDieAvg;
		
		//Covers advantage
		if(!this.advantage.isNon()) {
			double average = 0.0;
			
			if(this.advantage.isAdvantage())
				average = ((double) ((sides +1)*(4*sides -1))) / ((double) (6*sides));
			else
				average = ((double) ((sides +1)*(2*sides +1)) / (double) (6*sides));
			
			if(this.advantage.getScope() == ModifierScope.ALL)
				firstDieAvg = baseDieAvg = average;
			else
				firstDieAvg = average;
		}
		
		//TODO Implement Reroll and Cap
		if(!this.reroll.isNon()) {
			double average = 0.0;
			
			switch(this.reroll.getType()) {
			case EQUAL:	
				break;
			case GREATER:
				break;
			case GREATER_OR_EQUAL:	average = ((double) (Math.pow(sides, 2) + sides + sides * reroll.getThreshhold() - Math.pow(reroll.getThreshhold(), 2))) / ((double) (2* sides));
				break;
			case NON:
				break;
			case SMALLER:
				break;
			case SMALLER_OR_EQUAL:
				break;
			default:
				break;
			
			}
			
			if(this.reroll.getScope() == ModifierScope.ALL)
				firstDieAvg = baseDieAvg = average;
			else
				firstDieAvg = average;
		}
		
		double avgForAmount = firstDieAvg + baseDieAvg*(amount - 1);
		
		double bonus = 0;
		if(modifier.isSingle())
			bonus = modifier.getValue();
		else
			bonus = modifier.getValue() * amount;
		
		return avgForAmount + bonus;
	}
	
	public int getTotalResult() {
		int sum = 0;
		for(Dice dice : this.results) {
			if(dice.isIgnored())
				continue;
			sum += dice.getResult();
			
			if(!getModifier().isSingle())
				sum += getModifier().getValue();
			
		}
		
		if(getModifier().isSingle())
			sum += getModifier().getValue();
		
		return sum;
	}
	
	public List<Dice> applyAdvantage(List<Dice> results, RollAdvantage advantage) {
		
		if(advantage == null || advantage.isNon())
			return results;
		
		List<Dice> secondRound = new LinkedList<Dice>();
		boolean advantageAdded = false;
		for(Dice dice : results) {
			secondRound.add(dice);
			if(dice.isIgnored())
				continue;
			
			if(!advantageAdded) {
				if(advantage.isSingle())
					advantageAdded = true;
				
				Dice newDice = new Dice(dice.getSides());
				if(advantage.isAdvantage()) {
					if(newDice.getResult() > dice.getResult())
						dice.ignore(IgnoreReason.ADVANTAGE);
					else
						newDice.ignore(IgnoreReason.ADVANTAGE);
				}
				else { //DISADV
					if(newDice.getResult() < dice.getResult())
						dice.ignore(IgnoreReason.DISADVANTAGE);
					else
						newDice.ignore(IgnoreReason.DISADVANTAGE);
				}
				secondRound.add(newDice);
			}
		}
		return secondRound;
	}
	
	public List<Dice> applyReroll(List<Dice> results, RollReroll reroll) {
		if(reroll == null || reroll.isNon())
			return results;
		
		List<Dice> secondRound = new LinkedList<Dice>();
		boolean rerollApplied = false;
		for(Dice dice : results) {
			secondRound.add(dice);
			if(dice.isIgnored())
				continue;
			
			if(!rerollApplied) {
				
				Dice newDice = new Dice(dice.getSides());
				
				boolean shouldRR = switch(reroll.getType()) {
				case EQUAL -> dice.getResult() == reroll.getThreshhold();
				case GREATER -> dice.getResult() > reroll.getThreshhold();
				case GREATER_OR_EQUAL -> dice.getResult() >= reroll.getThreshhold();
				case SMALLER -> dice.getResult() < reroll.getThreshhold();
				case SMALLER_OR_EQUAL -> dice.getResult() <= reroll.getThreshhold();
				default -> false;
				};
				
				if(shouldRR) {
					dice.ignore(IgnoreReason.REROLL);
					secondRound.add(newDice);
					
					if(reroll.isSingle())
						rerollApplied = true;
				}
			}
		}
		return secondRound;
	}
	
	public List<Dice> applyCap(List<Dice> results, RollCap cap) {
		if(cap == null || cap.isNon())
			return results;
		
		List<Dice> secondRound = new LinkedList<Dice>();
		boolean capApplied = false;
		for(Dice dice : results) {
			secondRound.add(dice);
			if(dice.isIgnored())
				continue;
			
			if(!capApplied) {
				
				Dice newDice = new Dice(dice.getSides());
				newDice.setResult(cap.getThreshhold());
				
				if(cap.isMinimum()) {
					if(dice.getResult() < cap.getThreshhold()) {
						dice.ignore(IgnoreReason.MINIMUM);
						secondRound.add(newDice);
						
						if(cap.isSingle())
							capApplied = true;
					}
				}
				else {
					if(dice.getResult() > cap.getThreshhold()) {
						dice.ignore(IgnoreReason.MAXIMUM);
						secondRound.add(newDice);
						
						if(cap.isSingle())
							capApplied = true;
					}
				}
			}
		}
		
		return secondRound;
	}
	
	public RollCheck applyRollCheck(List<Dice> results, RollModifier modifier, RollCheck rollCheck) {
		if(rollCheck == null || rollCheck.isNon())
			return rollCheck;
		
		if(rollCheck.isSingle()) {
			rollCheck.check(getTotalResult());
			return rollCheck;
		}
		
		for(Dice dice : results) {
			if(dice.isIgnored())
				continue;
			
			if(!modifier.isSingle())
				rollCheck.check(dice.getResult() + modifier.getValue());
			else
				rollCheck.check(dice.getResult());
		}
		return rollCheck;
	}
	
	public boolean hasCritSuccess() {
		if(getSides() != 20)
			return false;
		
		for(Dice dice : getResults()) {
			if(dice.getResult() >= getCritThreshold())
				return true;
		}
		return false;
	}
	
	public boolean hasCritFail() {
		if(getSides() != 20)
			return false;
		
		for(Dice dice : getResults()) {
			if(dice.getResult() <= getFailThreshold())
				return true;
		}
		return false;
	}
	
	public int getCritSuccessCount() {
		int c = 0;
		if(getSides() != 20)
			return c;
		
		for(Dice dice : getResults()) {
			if(dice.getResult() >= getCritThreshold())
				c++;
		}
		return c;
	}
	
	public int getCritFailCount() {
		int c = 0;
		if(getSides() != 20)
			return c;
		
		for(Dice dice : getResults()) {
			if(dice.getResult() <= getFailThreshold())
				c++;
		}
		return c;
	}
	
	@Override
	public String toString() {
		String out = this.getPrompt() + ": \n";
		for(Dice dice : getResults()) {
			out += (dice.isIgnored() ? "~~" : "") + dice.getResult() + (dice.isIgnored() ? "~~" : "");
		}
		out += "= " + getTotalResult();
		return out;
	}
	
	public String getDetailMessage(LocalizedExpressionService les, Guild guild, boolean verbose) {
		StringBuilder out = new StringBuilder();
		out.append("> ").append(les.getLocalizedExpression(".roll.roll-title", guild)).append(": ").append(getPrompt()).append("\n");
		if(!getRollCheck().isNon()) {
			out.append("> **").append(getRollCheck().getResult().getSuccesses()).append("x SUCCESS**\n");
			out.append("> **").append(getRollCheck().getResult().getFails()).append("x FAIL**\n");
		}
		if(hasCritSuccess())
			out.append("> **").append(getCritSuccessCount()).append("x CRIT**\n");
		if(hasCritFail())
			out.append("> **").append(getCritFailCount()).append("x Crit FAIL**\n");
		out.append(">---------------------------\n");
		
		//TODO Wenn über x als Datei anhängen
		if(verbose || getResults().size() <= 50) {
			out.append("> **").append(les.getLocalizedExpression(".roll.result-title", guild)).append(":**\n");
			for(Dice dice : getResults()) {
				out.append("> ")
					.append(dice.toString());
				
				if(!getModifier().isSingle() && !dice.isIgnored()) {
					int mod = getModifier().getValue();
					out.append(" +")
						.append(mod)
						.append(" = ")
						.append(dice.getResult() + mod);
				}
				
				out.append("\n");
			}
			out.append(">---------------------------\n");
		}
		
		out.append("> **").append(les.getLocalizedExpression(".roll.modifier-title", guild)).append(":**\n")
			.append("> ");
		
		if(getModifier().isSingle()) {
			if(getModifier().getValue() >= 0)
				out.append("+");
			out.append(getModifier().getValue());
		}
		else {
			out.append("*(");
			if(getModifier().getValue() >= 0)
				out.append("+");
			out.append(getModifier().getValue()*getAmount())
				.append(")*");
		}
		
		out.append("\n>---------------------------\n")
			.append("> = ")
			.append(getTotalResult())
			.append("\n");
		
		return out.toString();
	}
	
	private String reconstructPrompt() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.amount)
			.append("d")
			.append(this.sides).append(" ")
			.append(this.modifier.toString()).append(" ")
			.append(this.advantage.toString()).append(" ")
			.append(this.reroll.toString()).append(" ")
			.append(this.rollCap.toString()).append(" ")
			.append(this.rollCheck.toString());
		
		return builder.toString();
	}
}
