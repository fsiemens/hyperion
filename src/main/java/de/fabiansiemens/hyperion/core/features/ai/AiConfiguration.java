package de.fabiansiemens.hyperion.core.features.ai;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.ollama4j.OllamaAPI;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class AiConfiguration {

	@Bean
	OllamaAPI ollamaApi(
			@Value("${ai.ollama.host}") String host,
			@Value("${ai.ollama.user}") String user,
			@Value("${ai.ollama.key}") String key
			) {
		OllamaAPI ollamaApi = new OllamaAPI(host);
		ollamaApi.setBasicAuth(user, key);
		ollamaApi.setVerbose(false);
		ollamaApi.setRequestTimeoutSeconds(360);
		return ollamaApi;
	}
	
	@Bean(name = "censorFile")
	File censorFile(@Value("${ai.censor.path}") String path) {
		return new File(path);
	}
	
	@Bean(name = "prohibitedContent")
	String[] prohibitedContent(@Value("${ai.prohibited-content}") String[] prohibitedContent) {
		log.info("Using {} prohibition filters.", prohibitedContent.length);
		return prohibitedContent;
	}
}
