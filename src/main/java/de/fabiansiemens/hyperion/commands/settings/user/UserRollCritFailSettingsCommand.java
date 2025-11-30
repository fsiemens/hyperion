package de.fabiansiemens.hyperion.commands.settings.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.core.util.ImageCompressionService;
import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

@Slf4j
@Subcommand(dataFile = "settings/user/CritFail.json", parent = UserSettingsCommand.class, group = "roll")
public class UserRollCritFailSettingsCommand extends SlashCommandBase {

	@Value("${roll.image.max-size:500}")
	private int maxAttachmentSize;
	
	private final UserService userService;
	private final ImageCompressionService imageCompressionService;
	
	public UserRollCritFailSettingsCommand(
				UserService userService, 
				ApplicationContext context,
				ImageCompressionService imageCompressionService
			) throws IOException, IllegalArgumentException {
		super(UserRollCritFailSettingsCommand.class, context);
		this.userService = userService;
		this.imageCompressionService = imageCompressionService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		Optional<String> message = Optional.ofNullable(event.getOption("message")).map(mapping -> mapping.getAsString());
		Optional<Attachment> attachment = Optional.ofNullable(event.getOption("image")).map(mapping -> mapping.getAsAttachment());
		UserData userData = userService.findByUser(event.getUser()).orElse(userService.getDefault(event.getUser()));
		UserSettings settings = userData.getSettings();
		event.deferReply(true).queue();
		
		if(!message.isPresent() && ! attachment.isPresent()) {
			if(settings.getCritFailFile() == null) {
				event.getHook().sendMessage(les.getLocalizedExpression("success.settings.overview", event.getGuild())
						+ "\n> **message:** " + (settings.getCritFailMessage() == null ? "--" : settings.getCritFailMessage()) 
						+ "\n> **image:** --").setEphemeral(true).queue();
				return;
			}
			
			event.getHook().sendMessage(les.getLocalizedExpression("success.settings.overview", event.getGuild())
					+ "\n> **message:** " + (settings.getCritFailMessage() == null ? "--" : settings.getCritFailMessage()) 
					+ "\n> **image:**")
			.addFiles(FileUpload.fromData(settings.getCritFailFile().getData(), "crit-success-image." + settings.getCritFailFile().getExtension()))
			.setEphemeral(true)
			.queue();
			return;
		}
		
		if(message.isPresent()) {
			String messageContent = message.get();
			
			if(messageContent.isBlank()) {
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-message.blank", event.getGuild())).setEphemeral(true).queue();
				return;
			}
			
			messageContent = messageContent.replaceAll("@\\{name\\}@", event.getUser().getAsMention());
			
			settings.setCritFailMessage(messageContent);
			userData.setSettings(settings);
			userService.update(userData);
		}
		
		if(attachment.isPresent()) {
			Attachment attachmentContent = attachment.get();
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
				settings.setCritFailFile(new FileEntity(data, attachmentContent.getFileExtension()));
				userData.setSettings(settings);
				userService.update(userData);
				
			} catch (Exception e) {
				log.warn("File download failed: ", e.getLocalizedMessage());
				log.warn("Caused by: ", e);
				event.getHook().sendMessage(les.getLocalizedExpression("error.settings.crit-image.failed", event.getGuild())).setEphemeral(true).queue();
				return;
			}
		}
		event.getHook().sendMessage(les.getLocalizedExpression("success.settings.saved", event.getGuild())).setEphemeral(true).queue();
	}

}
