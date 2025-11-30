package de.fabiansiemens.hyperion.core.events;

import java.util.Collection;

import de.fabiansiemens.hyperion.core.commands.legacy.LegacyCommand;
import lombok.Getter;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessagePollData;

public class LegacyCommandEvent extends MessageReceivedEvent {
	
	@Getter
	private LegacyCommand command;
	@Getter
	private String prompt;
	
	public LegacyCommandEvent(MessageReceivedEvent event, String prompt, LegacyCommand command) {
		super(event.getJDA(), event.getResponseNumber(), event.getMessage());
		this.command = command;
		this.prompt = prompt;
	}
	
	public MessageCreateAction reply(String content) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessage(content);
		return getMessage().reply(content);
	}
	
	public MessageCreateAction reply(MessageCreateData message) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessage(message);
		return getMessage().reply(message);
	}
	
	public MessageCreateAction replyComponents(Collection<? extends MessageTopLevelComponent> components) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessageComponents(components);
		return getMessage().replyComponents(components);
	}
	
	public MessageCreateAction replyComponents(MessageTopLevelComponent component, MessageTopLevelComponent... other) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessageComponents(component, other);
		return getMessage().replyComponents(component, other);
	}
	
	public MessageCreateAction replyEmbeds(Collection<? extends MessageEmbed> embeds) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessageEmbeds(embeds);
		return getMessage().replyEmbeds(embeds);
	}
	
	public MessageCreateAction replyEmbeds(MessageEmbed embed, MessageEmbed... other) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessageEmbeds(embed, other);
		return getMessage().replyEmbeds(embed, other);
	}
	
	public MessageCreateAction replyFiles(Collection<? extends FileUpload> files) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendFiles(files);
		return getMessage().replyFiles(files);
	}
	
	public MessageCreateAction replyFiles(FileUpload... files) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendFiles(files);
		return getMessage().replyFiles(files);
	}
	
	public MessageCreateAction replyFormat(String format, Object... args) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessageFormat(format, args);
		return getMessage().replyFormat(format, args);
	}
	
	public MessageCreateAction replyPoll(MessagePollData poll) {
		if(command.shouldDeleteInitMessage())
			return getChannel().sendMessagePoll(poll);
		return getMessage().replyPoll(poll);
	}
	
	public String getName() {
		return command.getName();
	}
}
