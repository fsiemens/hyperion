package de.fabiansiemens.hyperion.core.regex;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
public class RollRegexTests {
	
	private String randomRollGenerator() {
		Random random = new Random();
		
		StringBuilder stringBuilder = new StringBuilder();
		
		for(int i = 0; i < random.nextInt(1, 4); i++) {
			stringBuilder.append(" ");
			
			int amount = random.nextInt(0, 50);
			int sides = random.nextInt(0, 100);
			
			stringBuilder.append(amount).append("d").append(sides);
			
			switch(random.nextInt(3)) {
			case 0: break;
			case 1: stringBuilder.append(" +").append(random.nextInt(50)); break;
			case 2: stringBuilder.append(" -").append(random.nextInt(50)); break;
			}
			
			switch(random.nextInt(10)) {
			case 0,1,2,3: break;
			case 4,5: stringBuilder.append(" adv"); break;
			case 6: stringBuilder.append(" adv_all"); break;
			case 7,8: stringBuilder.append(" disadv"); break;
			case 9: stringBuilder.append(" disadv_all");
			}
			
			int rerollThreshhold = random.nextInt(0, sides);
			switch(random.nextInt(10)) {
			case 0,1,2,3,4: break;
			case 5: stringBuilder.append(" rr<").append(rerollThreshhold); break;
			case 6: stringBuilder.append(" rr<=").append(rerollThreshhold); break;
			case 7: stringBuilder.append(" rr=").append(rerollThreshhold); break;
			case 8: stringBuilder.append(" rr>").append(rerollThreshhold); break;
			case 9: stringBuilder.append(" rr>=").append(rerollThreshhold); break;
			}
			
			int minMaxThreshhold = random.nextInt(0, sides);
			switch(random.nextInt(10)) {
			case 0,1,2,3,4,5: break;
			case 6,7: stringBuilder.append(" min").append(minMaxThreshhold); break;
			case 8,9: stringBuilder.append(" max").append(minMaxThreshhold); break;
			}
		}
		
		String string = stringBuilder.toString();
		System.out.println(string);
		return string;
	}
	
	private Pattern getRollPattern() {
		String pattern = 	"""
                        (?<amount>[0-9]*)\
                        d(?<sides>[0-9]+)\
                        ( ?(?<modifier>[+-][0-9]+)*\
                        (?<adv>(adv|disadv)(_all)?)?\
                        (?<minmax>(min|max)[0-9]+)*\
                        (?<reroll>rr(<=|<|=|>|>=)[0-9]+a?)*)*\
                        """;
		return Pattern.compile(pattern, 2);
	}
	
	@Test
	public void testRollRegex() {
		String rollString = randomRollGenerator();
		Matcher matcher = getRollPattern().matcher(rollString);
		
		int rollIndex = 1;
		for(MatchResult result : matcher.results().toList()) {
			System.out.println("ROLL " + rollIndex + "\n=========================");
			System.out.println(result.namedGroups());
			System.out.println("RollString = " + rollString.subSequence(result.start(), result.end()));
			System.out.println("Amount = " + result.group("amount"));
			System.out.println("Sides = " + result.group("sides"));
			System.out.println("Modifier = " + result.group("modifier"));
			System.out.println("Advantage = " + result.group("adv"));
			System.out.println("MinMax = " + result.group("minmax"));
			System.out.println("Reroll = " + result.group("reroll"));
			System.out.println();
			rollIndex++;
		}
		
	}
}
