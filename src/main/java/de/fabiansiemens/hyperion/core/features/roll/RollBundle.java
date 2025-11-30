package de.fabiansiemens.hyperion.core.features.roll;

import java.util.List;

import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class RollBundle {

	@Nullable
	private final String prompt;
	private final int totalResult;
	@NonNull
	private final List<Roll> results;
	@Nullable
	private String rollLabel;
	
	public String getDetailMessage(LocalizedExpressionService les, Guild guild, boolean verbose) {
		StringBuilder out = new StringBuilder("\n## ");
		out.append(les.getLocalizedExpression(".roll.details-title", guild)).append(": \n");
		if(results.size() <= 1) {
			out.append(results.getFirst().getDetailMessage(les, guild, verbose));
			return out.toString();
		}
		
		out.append("=====================================\n");
		for(Roll result : results) {
			out.append(result.getDetailMessage(les, guild, verbose));
			out.append("=====================================\n");
		}
		out.append("> = ").append(totalResult);
		return out.toString();
	}
	
	public String getPrompt() {
		if(prompt == null)
			return reconstructPrompt();
		return prompt;
	}
	
	public String reconstructPrompt() {
		StringBuilder builder = new StringBuilder();
		for(Roll roll : results) {
			builder.append(roll.getPrompt()).append(" ");
		}
		
		if(getRollLabel() != null)
			builder.append('"').append(getRollLabel()).append('"');
		
		return builder.toString().strip();
	}

	public boolean hasCheck() {
		for(Roll result : results) {
			if(!result.getRollCheck().isNon())
				return true;
		}
		return false;
	}
	
	public boolean isSuccess() {
		int successes = 0;
		int fails = 0;
		
		for(Roll result : results) {
			if(result.getRollCheck().isSuccess())
				successes++;
			else
				fails++;
		}
		return successes > fails;
	}
	
	public boolean isFail() {
		int successes = 0;
		int fails = 0;
		
		for(Roll result : results) {
			if(result.getRollCheck().isSuccess())
				successes++;
			else
				fails++;
		}
		return successes < fails;
	}

	public boolean hasCritSuccess() {
		for(Roll result : results) {
			if(result.hasCritSuccess())
				return true;
		}
		return false;
	}
	
	public boolean hasCritFail() {
		for(Roll result : results) {
			if(result.hasCritFail())
				return true;
		}
		return false;
	}
}
