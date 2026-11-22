/**
 * 
 */
package de.fabiansiemens.hyperion.commands.ai;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.ai.AiService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.AttachedFile;

/**
 * 
 */
@Command(dataFile = "ai/Chat.json")
public class ChatCommand extends SlashCommandBase {

	private AiService aiService;
	
	/**
	 * @param <T>
	 * @param clazz
	 * @param xmlServ
	 * @param les
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	public <T> ChatCommand(ApplicationContext context, AiService aiService)
			throws IOException, IllegalArgumentException {
		super(ChatCommand.class, context);
		this.aiService = aiService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		event.deferReply(true).queue();
		String prompt = event.getOption("prompt").getAsString();
		
		if(aiService.containsProhibitedContent(prompt)) {
			event.getHook().sendMessage("https://c.tenor.com/CAEyorifT40AAAAd/tenor.gif").setEphemeral(true).queue();
			return;
		}
		
		String response = aiService.chat(prompt);
		
		if(response == null) {
			event.getHook().sendMessage(les.getLocalizedExpression("error.chat.no-response", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		String[] split = response.split("</think>");
		
		if(split.length < 1) {
			event.getHook().sendMessage(les.getLocalizedExpression("error.chat.no-response", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		String answer = "";
		String thoughts = "";
		
		if(split.length == 1) {
			answer = split[0];
		}
		else if(split.length > 1) {
			answer = split[1];
			thoughts = split[0].substring("<think>".length());
		}

		answer = answer.strip();
		if(answer.isEmpty()) {
			event.getHook().sendMessage(les.getLocalizedExpression("error.chat.no-response", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		for(int start = 0; start < answer.length(); start += Message.MAX_CONTENT_LENGTH) {
			int end = Math.min(answer.length(), start + Message.MAX_CONTENT_LENGTH);
			event.getHook().sendMessage(answer.substring(start, end)).setEphemeral(true).queue();
		}
		
		if(!thoughts.isBlank()) {
			AttachedFile thoughtsFile = AttachedFile.fromData(thoughts.getBytes(), "thoughts.txt");
			event.getHook().editOriginalAttachments(thoughtsFile).queue();
		}
	}
}
