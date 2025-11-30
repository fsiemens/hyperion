package de.fabiansiemens.hyperion.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class CoreConfiguration {

	@Bean
	ExpressionParser expressionParser() {
		return new SpelExpressionParser();
	}
}
