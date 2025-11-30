package de.fabiansiemens.hyperion.core.features.roll;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.ModifierScope;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollAdvantage;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollCap;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollCheck;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollModifier;
import de.fabiansiemens.hyperion.core.features.roll.modifiers.RollReroll;
import de.fabiansiemens.hyperion.core.util.Arguments;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RollService {

	private final String regex = "(?<amount>[0-9]*)d(?<sides>[0-9]+)( ?(?<modifier>[+-] ?[0-9]+(_?all)?)*(?<adv>(adv|disadv)(_?all)?)?(?<cap>(min|max)[0-9]+(_?all)?)*(?<reroll>rr(<=|<|==?|>|>=)[0-9]+(_?all)?)*(?<check>(beat|check|nail|limit|cut)[0-9]+(_?all)?)*)*";
	private final String labelRegex = "\".*\"";
	
	@Value("${roll.amount.min:1}")
	private int minAmount;
	
	@Value("${roll.amount.max:10000}")
	private int maxAmount;
	
	@Value("${roll.sides.min:1}")
	private int minSides;
	
	@Value("${roll.sides.max:2147483647}")
	private int maxSides = Integer.MAX_VALUE;
	
	@Value("${roll.modifier.min:-2147483648}")
	private int minModifier;
	
	@Value("${roll.modifier.max:2147483647}")
	private int maxModifier;
	
	@Value("${roll.cap.min:-2147483648}")
	private int minCap;
	
	@Value("${roll.cap.max:2147483647}")
	private int maxCap;
	
	@Value("${roll.reroll.min:-2147483648}")
	private int minReroll;
	
	@Value("${roll.reroll.max:2147483647}")
	private int maxReroll;
	
	@Value("${roll.check.min:-2147483648}")
	private int minCheck;
	
	@Value("${roll.check.max:2147483647}")
	private int maxCheck;
	
	public Pattern getRollPattern() {
		return Pattern.compile(regex);
	}
	
	public Pattern getLabelPattern() {
		return Pattern.compile(labelRegex);
	}
	
	public boolean isValid(String rollString) {
		Matcher matcher = getRollPattern().matcher(rollString.toLowerCase());
		return matcher.hasMatch();
	}
	
	public List<Roll> parseRoll(String rollString) throws RollException{
		Matcher matcher = getRollPattern().matcher(rollString.toLowerCase());
		List<Roll> rolls = new LinkedList<Roll>();
		
		List<MatchResult> matches = matcher.results().toList();
		if(matches.size() < 1)
			throw new RollException("error.roll.empty", null);
		
		for(MatchResult result : matches) {
			int amount = parseAmount(result.group("amount"));
			int sides= parseSides(result.group("sides"));
			RollModifier modifier = parseModifier(result.group("modifier"));
			RollAdvantage adv = parseAdvantage(result.group("adv"));
			RollCap cap = parseCap(result.group("cap"));
			RollReroll reroll = parseReroll(result.group("reroll"));
			RollCheck check = parseCheck(result.group("check"));
			
			Roll roll = new Roll(amount, sides, modifier, adv, cap, reroll, check);
			roll.setPrompt(rollString.substring(result.start(), result.end()));
			rolls.add(roll);
		}
		return rolls;
	}
	
	public RollBundle compileRoll(String rollString) throws RollException {
		List<Roll> rolls = parseRoll(rollString);
		
		for(Roll roll : rolls) {
			roll.rollDice();
		}
		
		String label = parseLabel(rollString);
		
		int total = 0;
		for(Roll roll : rolls) {
			total += roll.getTotalResult();
		}
		
		return new RollBundle(rollString, total, rolls, label);
	}
	
	public double calculateAverage(String rollString) throws RollException {
		List<Roll> rolls = parseRoll(rollString);
		double sum = 0.0;
		
		for(Roll roll : rolls) {
			sum += roll.getAverage();
		}
		
		return sum;
	}
	
	private int parseAmount(String input) throws RollException {
		if(input == null || input.isBlank()) return 1; 
		
		return parseOrThrow(input, "@{.roll.amount}@", minAmount, maxAmount);
	}
	
	private int parseSides(String input) throws RollException {
		if(input == null || input.isBlank()) throw new RollException("error.roll.no-sides", null); 
		
		return parseOrThrow(input, "@{.roll.sides}@", this.minSides, this.maxSides);
	}
	
	private RollModifier parseModifier(String input) throws RollException {
		if(input == null || input.isBlank()) return new RollModifier(0, ModifierScope.SINGLE);
		
		input = input.toLowerCase();
		
		ModifierScope scope = getScope(input);
		input = replaceScope(input);
		int modifier = parseOrThrow(input, "@{.roll.modifier}@", this.minModifier, this.maxModifier);
		return new RollModifier(modifier, scope);
	}
	
	private RollAdvantage parseAdvantage(String input) {
		if(input == null || input.isBlank()) return new RollAdvantage(RollAdvantage.Type.NON, ModifierScope.SINGLE);
		
		input = input.toLowerCase();
		
		ModifierScope scope = getScope(input);
		input = replaceScope(input);
		
		switch(input) {
		case "adv": return new RollAdvantage(RollAdvantage.Type.ADVANTAGE, scope);
		case "disadv": return new RollAdvantage(RollAdvantage.Type.DISADVANTAGE, scope);
		default: return new RollAdvantage(RollAdvantage.Type.NON, scope);
		}
	}
	
	private RollReroll parseReroll(String input) throws RollException {
		if(input == null || input.isBlank() || !input.toLowerCase().startsWith("rr")) 
			return new RollReroll(RollReroll.Type.NON, 0, ModifierScope.SINGLE);
		
		input = input.toLowerCase();
		
		ModifierScope scope = getScope(input);
		input = replaceScope(input);
		
		input = input.replaceAll("rr", "");
		RollReroll.Type type = RollReroll.Type.NON;
		
		if(input.startsWith("<=")) {
			type = RollReroll.Type.SMALLER_OR_EQUAL;
			input = input.replaceAll("<=", "");
		}
		else if(input.startsWith("<")) {
			type = RollReroll.Type.SMALLER;
			input = input.replaceAll("<", "");
		}
		else if(input.startsWith("=")) {
			type = RollReroll.Type.EQUAL;
			input = input.replaceAll("=", "");
		}
		else if(input.startsWith(">")) {
			type = RollReroll.Type.GREATER;
			input = input.replaceAll(">", "");
		}
		else if(input.startsWith(">=")) {
			type = RollReroll.Type.GREATER_OR_EQUAL;
			input = input.replaceAll(">=", "");
		}
		
		int threshold = parseOrThrow(input, "@{.roll.reroll-threshold}@", this.minReroll, this.maxReroll);
		return new RollReroll(type, threshold, scope);
	}
	
	private RollCap parseCap(String input) throws RollException {
		if(input == null || input.isBlank()) return new RollCap(RollCap.Type.NON, 0, ModifierScope.SINGLE);
		
		input = input.toLowerCase();
		
		ModifierScope scope = getScope(input);
		input = replaceScope(input);
		
		RollCap.Type type = RollCap.Type.NON;
		
		if(input.startsWith("min")) {
			type = RollCap.Type.MINIMUM;
			input = input.replaceFirst("min", "");
		}
		else if(input.startsWith("max")) {
			type = RollCap.Type.MAXIMUM;
			input = input.replaceFirst("max", "");
		}
		
		int cap = parseOrThrow(input, "@{.roll.cap}@", this.minCap, this.maxCap);
		return new RollCap(type, cap, scope);
	}
	
	private RollCheck parseCheck(String input) throws RollException {
		if(input == null || input.isBlank()) return new RollCheck(RollCheck.Type.NON, 0, ModifierScope.SINGLE);
		
		input = input.toLowerCase();
		
		ModifierScope scope = getScope(input);
		input = replaceScope(input);
		
		RollCheck.Type type = RollCheck.Type.NON;
		
		if(input.startsWith("beat")) {
			type = RollCheck.Type.BEAT;
			input = input.replaceFirst("beat", "");
		}
		else if(input.startsWith("check")) {
			type = RollCheck.Type.CHECK;
			input = input.replaceFirst("check", "");
		}
		else if(input.startsWith("nail")) {
			type = RollCheck.Type.NAIL;
			input = input.replaceFirst("nail", "");
		}
		else if(input.startsWith("limit")) {
			type = RollCheck.Type.LIMIT;
			input = input.replaceFirst("limit", "");
		}
		else if(input.startsWith("cut")) {
			type = RollCheck.Type.CUT;
			input = input.replaceFirst("cut", "");
		}
		
		int threshold = parseOrThrow(input, "@{.roll.check}@", this.minCheck, this.maxCheck);
		return new RollCheck(type, threshold, scope);
	}
	
	@Nullable
	private String parseLabel(String input) {
		if(input == null || input.isBlank()) return null;

		Matcher matcher = getLabelPattern().matcher(input);
		List<MatchResult> matches = matcher.results().toList();
		if(matches.size() < 1)
			return null;
		
		String label = matches.getFirst().group();
		label = label.replaceAll("\"", "");
		
		return label.isBlank() ? null : label;
	}
	
	private ModifierScope getScope(@NonNull String input) {
		if(input.endsWith("all")) {
			return ModifierScope.ALL;
		}
		return ModifierScope.SINGLE;
	}
	
	private String replaceScope(@NonNull String input) {
		return input.replaceAll("_?all", "");
	}
	
	private int parseOrThrow(@NonNull String input, @NonNull String varName, int min, int max) throws RollException {
		Arguments args = Arguments.of("param", varName)
			.put("min", String.valueOf(min))
			.put("max", String.valueOf(max));
		
		input = input.replaceAll(" ", "");
		
		try {
			int val = Integer.parseInt(input);
			if(val < min || val > max)
				throw new RollException("error.common.param-bounds", args);
			return val;
		}
		catch(NumberFormatException e) {
			throw new RollException("error.common.param-type", args);
		}
	}
}
