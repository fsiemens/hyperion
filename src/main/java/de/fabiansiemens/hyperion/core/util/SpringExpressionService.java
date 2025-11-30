package de.fabiansiemens.hyperion.core.util;

import java.util.Map;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SpringExpressionService {
	
	private final ExpressionParser parser;
	
	public boolean evaluateBoolean(String expString, Map<String, Object> variables) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		variables.forEach(context::setVariable);
		Expression expression = parser.parseExpression(expString);
		return expression.getValue(context, Boolean.class);
	}

	public String evaluateString(String expString, Map<String, Object> variables) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		variables.forEach(context::setVariable);
		Expression expression = parser.parseExpression(expString);
		return expression.getValue(context, String.class);
	}
}
