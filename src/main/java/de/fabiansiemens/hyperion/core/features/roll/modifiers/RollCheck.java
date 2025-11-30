package de.fabiansiemens.hyperion.core.features.roll.modifiers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RollCheck {
	public static enum Type {
		NON, 
		BEAT, 
		CHECK, 
		NAIL, 
		LIMIT, 
		CUT,
	}
	
	@Data
	@AllArgsConstructor
	public static class Result{
		private int successes;
		private int fails;
	
		public static Result empty() {
			return new Result(0, 0);
		}
		
		public void succeed() {
			successes++;
		}
		
		public void fail() {
			fails++;
		}
	}

	private Type type;
	private int threshold;
	private ModifierScope scope;
	private Result result;
	
	public RollCheck(Type type, int threshold, ModifierScope scope) {
		this(type, threshold, scope, Result.empty());
	}
	
	public boolean isSingle() {
		return scope == ModifierScope.SINGLE;
	}

	public boolean isNon() {
		return type == Type.NON;
	}
	
	public boolean isBeat() {
		return type == Type.BEAT;
	}
	
	public boolean isCheck() {
		return type == Type.CHECK;
	}
	
	public boolean isNail() {
		return type == Type.NAIL;
	}
	
	public boolean isLimit() {
		return type == Type.LIMIT;
	}
	
	public boolean isCut() {
		return type == Type.CUT;
	}
	
	public void succeed() {
		this.result.succeed();
	}
	
	public void fail() {
		this.result.fail();
	}
	
	public boolean isSuccess() {
		return this.result.getSuccesses() > this.result.getFails();
	}
	
	public boolean isDraw() {
		return this.result.getSuccesses() == this.result.getFails();
	}
	
	public boolean isFail() {
		return this.result.getSuccesses() < this.result.getFails();
	}

	public void check(int input) {
		switch(this.type) {
		case BEAT:	check(threshold < input);
			break;
		case CHECK:	check(threshold <= input);
			break;
		case NAIL: 	check(threshold == input);
			break;
		case LIMIT: check(threshold >= input);
			break;
		case CUT: 	check(threshold > input);
			break;
		default: return;
		}
	}
	
	@Override
	public String toString() {
		
		StringBuilder builder = new StringBuilder();
		switch(type) {
		case BEAT:	builder.append("beat");
			break;
		case CHECK:	builder.append("check");
			break;
		case CUT:	builder.append("cut");
			break;
		case LIMIT:	builder.append("limit");
			break;
		case NAIL:	builder.append("nail");
			break;
		case NON:
		default: 	return "";
		}
		
		builder.append(threshold);
		
		if(scope == ModifierScope.ALL)
			builder.append("all");
		
		return builder.toString();
	}
	
	private void check(boolean input) {
		if(input)
			succeed();
		else
			fail();
	}
}
