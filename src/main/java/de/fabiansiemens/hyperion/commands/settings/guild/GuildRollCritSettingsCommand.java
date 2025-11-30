package de.fabiansiemens.hyperion.commands.settings.guild;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.guild.GuildData;
import de.fabiansiemens.hyperion.core.guild.GuildService;
import de.fabiansiemens.hyperion.core.guild.settings.GuildSettings;
import de.fabiansiemens.hyperion.core.util.ImageCompressionService;
import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

@Slf4j
@Subcommand(dataFile = "settings/guild/Crit.json", parent = GuildSettingsCommand.class, group = "roll")
public class GuildRollCritSettingsCommand extends SlashCommandBase {

	@Value("${roll.image.max-size:500}")
	private int maxAttachmentSize;
	
	private final GuildService guildService;
	private final ImageCompressionService imageCompressionService;
	
	public <T> GuildRollCritSettingsCommand(
				ApplicationContext context,
				GuildService guildService,
				ImageCompressionService imageCompressionService
			) throws IOException, IllegalArgumentException {
		super(GuildRollCritSettingsCommand.class, context);
		this.guildService = guildService;
		this.imageCompressionService = imageCompressionService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		Optional<Boolean> optSendMessage = Optional.ofNullable(event.getOption("send-message")).map(mapping -> mapping.getAsBoolean());
		Optional<Boolean> optSendImage = Optional.ofNullable(event.getOption("send-image")).map(mapping -> mapping.getAsBoolean());
		Optional<Boolean> optAllowCustomMessage = Optional.ofNullable(event.getOption("allow-custom-message")).map(mapping -> mapping.getAsBoolean());
		Optional<Boolean> optAllowCustomImage = Optional.ofNullable(event.getOption("allow-custom-image")).map(mapping -> mapping.getAsBoolean());
		Optional<String> optDefaultSucMessage = Optional.ofNullable(event.getOption("default-success-message")).map(mapping -> mapping.getAsString());
		Optional<Attachment> optDefaultSucImage = Optional.ofNullable(event.getOption("default-success-image")).map(mapping -> mapping.getAsAttachment());
		Optional<String> optDefaultFailMessage = Optional.ofNullable(event.getOption("default-fail-message")).map(mapping -> mapping.getAsString());
		Optional<Attachment> optDefaultFailImage = Optional.ofNullable(event.getOption("default-fail-image")).map(mapping -> mapping.getAsAttachment());
		
		event.deferReply(true).queue();
		
		GuildData guildData = guildService.find(event.getGuild()).orElse(guildService.create(event.getGuild()));
		guildData = guildService.update(guildData);
		GuildSettings settings = guildData.getSettings();
		
		if(event.getOptions().size() <= 0) {
			StringBuilder builder = new StringBuilder();
			builder
			.append("\n> **send-message:** " + String.valueOf(settings.isSendCritMessage()))
			.append("\n> **send-image:** " + String.valueOf(settings.isSendCritImage()))
			.append("\n> **allow-custom-message:** " + String.valueOf(settings.isAllowCritCustomMessage()))
			.append("\n> **allow-custom-image:** " + String.valueOf(settings.isAllowCritCustomImage()))
			.append("\n> **default-success-message:** " + (settings.getGuildDefaultCritSuccessMessage() == null ? "--" : settings.getGuildDefaultCritSuccessMessage()))
			.append("\n> **default-success-image:** " + (settings.getGuildDefaultCritSuccessImage() == null ? "--" : "(see attachment)"))
			.append("\n> **default-fail-message:** " + (settings.getGuildDefaultCritFailMessage() == null ? "--" : settings.getGuildDefaultCritFailMessage()))
			.append("\n> **default-fail-image:** " + (settings.getGuildDefaultCritFailImage() == null ? "--" : "(see attachment)"));
			
			List<FileUpload> attachments = new LinkedList<>();
			if(settings.getGuildDefaultCritSuccessImage() != null) 
				attachments.add(FileUpload.fromData(settings.getGuildDefaultCritSuccessImage().getData(), "crit-success-image." + settings.getGuildDefaultCritSuccessImage().getExtension()));
			
			if(settings.getGuildDefaultCritFailImage() != null) 
				attachments.add(FileUpload.fromData(settings.getGuildDefaultCritFailImage().getData(), "crit-fail-image." +settings.getGuildDefaultCritFailImage().getExtension()));
			
			event.getHook().sendMessage(les.getLocalizedExpression("success.settings.overview", event.getGuild()) + builder.toString()).addFiles(attachments).queue();
			return;
		}
		
		if(optSendMessage.isPresent())
			settings.setSendCritMessage(optSendMessage.get());
		
		if(optSendImage.isPresent())
			settings.setSendCritImage(optSendImage.get());
		
		if(optAllowCustomMessage.isPresent())
			settings.setAllowCritCustomMessage(optAllowCustomMessage.get());
		
		if(optAllowCustomImage.isPresent())
			settings.setAllowCritCustomImage(optAllowCustomImage.get());
		
		if(optDefaultSucMessage.isPresent()) {
			String messageContent = optDefaultSucMessage.get();
			
			if(messageContent.isBlank()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-message.blank", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			settings.setGuildDefaultCritSuccessMessage(messageContent);
		}
		
		if(optDefaultSucImage.isPresent()) {
			Attachment attachmentContent = optDefaultSucImage.get();
			if(!attachmentContent.isImage()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.type", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			InputStream stream = attachmentContent.getProxy().download().completeOnTimeout(InputStream.nullInputStream(), 3, TimeUnit.MINUTES).join();
			byte[] data;
			try {
				data = stream.readAllBytes();
				if(data.length <= 0) {
					event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.failed", event.getGuild())).setEphemeral(true).queue();
					return;
				}
					
				data = imageCompressionService.compressImage(data, attachmentContent.getFileExtension(), maxAttachmentSize, maxAttachmentSize);
				settings.setGuildDefaultCritSuccessImage(new FileEntity(data, attachmentContent.getFileExtension()));
				
			} catch (Exception e) {
				log.warn("File download failed: ", e.getLocalizedMessage());
				log.warn("Caused by: ", e);
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.failed", event.getGuild())).setEphemeral(true).queue();
				return;
			}
		}
		
		if(optDefaultFailMessage.isPresent()) {
			String messageContent = optDefaultFailMessage.get();
			
			if(messageContent.isBlank()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-message.blank", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			settings.setGuildDefaultCritFailMessage(messageContent);
		}
		
		if(optDefaultFailImage.isPresent()) {
			Attachment attachmentContent = optDefaultFailImage.get();
			if(!attachmentContent.isImage()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.type", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			InputStream stream = attachmentContent.getProxy().download().completeOnTimeout(InputStream.nullInputStream(), 3, TimeUnit.MINUTES).join();
			byte[] data;
			try {
				data = stream.readAllBytes();
				if(data.length <= 0) {
					event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.failed", event.getGuild())).setEphemeral(true).queue();
					return;
				}
					
				data = imageCompressionService.compressImage(data, attachmentContent.getFileExtension(), maxAttachmentSize, maxAttachmentSize);
				settings.setGuildDefaultCritFailImage(new FileEntity(data, attachmentContent.getFileExtension()));
				
			} catch (Exception e) {
				log.warn("File download failed: ", e.getLocalizedMessage());
				log.warn("Caused by: ", e);
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.failed", event.getGuild())).setEphemeral(true).queue();
				return;
			}
		}
		
		guildData.setSettings(settings);
		guildService.update(guildData);
		event.getHook().sendMessage(les.getLocalizedExpression("success.settings.saved", event.getGuild())).setEphemeral(true).queue();
	}

}
