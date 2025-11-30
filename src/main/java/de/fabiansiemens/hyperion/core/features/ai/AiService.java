package de.fabiansiemens.hyperion.core.features.ai;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatMessage;
import io.github.ollama4j.models.chat.OllamaChatMessageRole;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.response.Model;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {
	
	@Value("${ai.model.chat}")
	private String chatModel;

	@Value("${ai.prohibited-content}") 
	private String[] prohibitedContent;
	
	private final File censorFile;
	private final OllamaAPI ollamaApi;

	public File getCensorResponse() {
		return this.censorFile;
	}
	
	public List<Model> getModels() {
		try {
			return ollamaApi.listModels();
		} catch (OllamaBaseException | IOException | InterruptedException | URISyntaxException e) {
			log.warn("Failed to retrieve Model List from Ollama Server: ", e);
			return new LinkedList<>();
		}
	}
	
	@Nullable
	public String chat(String prompt) {
		List<OllamaChatMessage> messages = new LinkedList<>();
		messages.add(new OllamaChatMessage(OllamaChatMessageRole.USER, prompt));
		OllamaChatRequest request = new OllamaChatRequest(chatModel, messages);
		
		try {
			OllamaChatResult result = ollamaApi.chat(request);
			OllamaChatMessage response = result.getChatHistory().getLast();
			return response.getContent();
		} catch (OllamaBaseException | IOException | InterruptedException e) {
			log.warn("Failed to Chat with {}: ", this.chatModel, e);
			return null;
		}
	}

	public boolean containsProhibitedContent(String prompt) {	
		for(String element : this.prohibitedContent) {
			log.info("Checking if {} contains {}", prompt.toLowerCase(), element.toLowerCase());
			if(prompt.toLowerCase().contains(element.toLowerCase()))
				return true;
		}
		return false;
	}
}
