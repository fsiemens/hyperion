package de.fabiansiemens.hyperion.interactions.roll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.events.LegacyCommandEvent;
import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.roll.RollBundle;
import de.fabiansiemens.hyperion.core.features.roll.RollCachingService;
import de.fabiansiemens.hyperion.core.features.roll.RollService;
import de.fabiansiemens.hyperion.core.guild.GuildData;
import de.fabiansiemens.hyperion.core.guild.GuildService;
import de.fabiansiemens.hyperion.core.guild.settings.GuildSettings;
import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.interactions.InteractionChainBase;
import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import io.micrometer.common.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.utils.FileUpload;

@InteractionChain
public class CommonRollInteractionChain extends InteractionChainBase {

	@Data
	@AllArgsConstructor
	private class JointRollBundleAndReply {
		private final RollBundle bundle;
		private final String reply;
	}
	
	private static final String ROLL_DETAIL_BUTTON_PATH = "roll/RollDetails.xml";
	private static final String ROLL_AGAIN_BUTTON_PATH = "roll/RollAgain.xml";
	private static final String ROLL_PANEL_PATH = "roll/RollPanel.xml";
	
	@Value("${roll.emoji.success}")
	public String successEmoji;
	
	@Value("${roll.emoji.fail}")
	public String failEmoji;
	
	protected final GuildService guildService;
	protected final UserService userService;
	protected final RollService rollService;
	protected final RollCachingService rollCachingService;
	
	public CommonRollInteractionChain(ApplicationContext context) {
		super(context);
		this.guildService = context.getBean(GuildService.class);
		this.userService = context.getBean(UserService.class);
		this.rollService = context.getBean(RollService.class);
		this.rollCachingService = context.getBean(RollCachingService.class);
	}
	
	public void onRollLegacyCommandInteraction(LegacyCommandEvent event) throws UiParseException {
		Guild guild = (event.isFromGuild() ? event.getGuild() : null);
		JointRollBundleAndReply result = getReply(event.getPrompt(), guild, event.getMessage().getAuthor());
		
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, guild);
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, guild);
		
		Message response = event.getChannel().sendMessage(result.reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		if(result.bundle.hasCritSuccess() || result.bundle.hasCritFail())
			sendCritNotification(event.getChannel(), result.bundle, event.getGuild(), event.getAuthor());
		
		rollCachingService.putRoll(response.getIdLong(), result.bundle);
	}

	public void onRollSlashCommandInteraction(SlashCommandInteractionEvent event) throws UiParseException {
		String prompt = event.getOption("prompt", null, mapping -> mapping.getAsString());
		
		if(prompt == null || prompt.isBlank()) {
			Container rollPanel = uiProvider.panelOf(ROLL_PANEL_PATH, event.getGuild());
			event.replyComponents(rollPanel).useComponentsV2().setEphemeral(true).queue();
			return;
		}
		
		//if prompt option is set
		JointRollBundleAndReply result = getReply(prompt, event.getGuild(), event.getUser());
		
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, event.getGuild());
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild());
		
		InteractionHook hook = event.reply(result.reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		if(result.bundle.hasCritSuccess() || result.bundle.hasCritFail())
			sendCritNotification(event.getChannel(), result.bundle, event.getGuild(), event.getUser());
		
		rollCachingService.putRoll(hook.retrieveOriginal().complete().getIdLong(), result.bundle);	
	}

	@InteractionCallback(id = "b.roll.again")
	public void onRollAgainButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		RollBundle bundle = rollCachingService.getRoll(event.getMessageIdLong());
		String rollPrompt;
		if(bundle == null) {
			rollPrompt = args.get("prompt").asString();
			if(rollPrompt == null || rollPrompt.isBlank()) {
				String content = event.getMessage().getContentRaw();
				content = content.substring(0, content.indexOf(")"));
				rollPrompt = content.substring(content.indexOf("(") +1);
			}
		}
		else {
			rollPrompt = bundle.getPrompt();
		}
		
		JointRollBundleAndReply result = getReply(rollPrompt, event.getGuild(), event.getUser());
		
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, event.getGuild(), args);
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild(), args);
		
		InteractionHook hook = event.reply(result.reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		if(result.bundle.hasCritSuccess() || result.bundle.hasCritFail())
			sendCritNotification(event.getChannel(), result.bundle, event.getGuild(), event.getUser());
		
		rollCachingService.putRoll(hook.retrieveOriginal().complete().getIdLong(), result.bundle);	
	}
	
	@InteractionCallback(id = "b.roll.details")
	public void onRollDetailsButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		Message message = event.getMessage();
		RollBundle rollBundle = rollCachingService.getRoll(message.getIdLong());
		
		if(rollBundle == null) {
			event.reply(les.getLocalizedExpression("error.roll-detail-button.timeout", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild(), args);
		event.editMessage(message.getContentRaw() + rollBundle.getDetailMessage(les, event.getGuild(), false))	//TODO change to panel
			.setComponents(ActionRow.of(rollAgainButton)).queue();
	}

	private JointRollBundleAndReply getReply(String prompt, @Nullable Guild guild, User user) throws UiParseException {
	
		RollBundle bundle;
		try {
			bundle = rollService.compileRoll(prompt);
		}
		catch(RollException e) {
			return new JointRollBundleAndReply(null, les.getLocalizedExpression(e.getLocalizedMessage(), guild, e.getArgs()));
		}
		
		Arguments args = prepareRollArgs(bundle, Arguments.empty(), user);
		
		String reply;
		String label = bundle.getRollLabel();
		if(label == null || label.isBlank())
			if(bundle.hasCheck())
				reply = les.getLocalizedExpression("success.roll.response-with-check", guild, args);
			else
				reply = les.getLocalizedExpression("success.roll.base-response", guild, args);
		else
			if(bundle.hasCheck())
				reply = les.getLocalizedExpression("success.roll.response-with-check-and-label", guild, args);
			else
				reply = les.getLocalizedExpression("success.roll.response-with-label", guild, args);
		
		return new JointRollBundleAndReply(bundle, reply);
	}
	
	private void sendCritNotification(MessageChannelUnion channel, RollBundle bundle, Guild guild, User user) {
		if(guild == null)
			return;
		
		GuildData guildData = guildService.find(guild).orElse(guildService.create(guild));
		guildService.update(guildData);
		
		UserData userData = userService.findByUser(user).orElse(userService.getDefault(user));
		userService.update(userData);
		
		GuildSettings guildSettings = guildData.getSettings();
		UserSettings userSettings = userData.getSettings();
		
		if(!guildSettings.isSendCritMessage() && !guildSettings.isSendCritImage())
			return;
		
		Arguments args = Arguments.of("channel", channel.getAsMention())
			.put("name", user.getAsMention())
			.put("guild", guild.getName())
			.put("result", String.valueOf(bundle.getTotalResult()))
			.put("label", String.valueOf(bundle.getRollLabel()))
			.put("prompt", String.valueOf(bundle.getPrompt()));
		
		String critSuccessMessageContent = "";
		String critFailMessageContent = "";
		
		if(guildSettings.isSendCritMessage()) {
			if(guildSettings.isAllowCritCustomMessage() && userSettings.isUseCritSettings()) {
				critSuccessMessageContent = les.replacePlaceholders(userSettings.getCritSuccessMessage(), guild, args);
				critFailMessageContent = les.replacePlaceholders(userSettings.getCritFailMessage(), guild, args);
			}
			else {
				critSuccessMessageContent = les.replacePlaceholders(guildSettings.getGuildDefaultCritSuccessMessage(), guild, args);
				critFailMessageContent = les.replacePlaceholders(guildSettings.getGuildDefaultCritFailMessage(), guild, args);
			}
		}
		
		FileEntity critSuccessImage = null;
		FileEntity critFailImage = null;
		
		if(guildSettings.isSendCritImage()) {
			if(guildSettings.isAllowCritCustomImage() && userSettings.isUseCritSettings()) {
				critSuccessImage = userSettings.getCritSuccessFile();
				critFailImage = userSettings.getCritFailFile();
			}
			else {
				critSuccessImage = guildSettings.getGuildDefaultCritSuccessImage();
				critFailImage = guildSettings.getGuildDefaultCritFailImage();
			}
		}
		
		if(bundle.hasCritSuccess()) {
			if(critSuccessImage != null)
				channel.sendMessage(critSuccessMessageContent)
						.addFiles(FileUpload.fromData(critSuccessImage.getData(), "crit-success-image." + critSuccessImage.getExtension()))
						.queue();

			else if(!critSuccessMessageContent.isBlank())
				channel.sendMessage(critSuccessMessageContent).queue();
		}
		
		if(bundle.hasCritFail()) {
			if(critFailImage != null)
				channel.sendMessage(critFailMessageContent)
						.addFiles(FileUpload.fromData(critFailImage.getData(), "crit-fail-image." + critFailImage.getExtension()))
						.queue();
			else if(!critFailMessageContent.isBlank())
				channel.sendMessage(critFailMessageContent).queue();
		}
	}
	
	private Arguments prepareRollArgs(RollBundle bundle, Arguments args, User user) {
		args.put("mention", user.getAsMention())
			.put("result", "" + bundle.getTotalResult())
			.put("label", bundle.getRollLabel())
			.put("prompt", bundle.getPrompt());
			
		if(bundle.isSuccess())
			args.put("check", "@{.roll.succeeds}@");
		else if(bundle.isFail())
			args.put("check", "@{.roll.fails}@");
		else
			args.put("check", "@{.roll.draws}@");
		
		if(bundle.hasCritSuccess() && bundle.hasCritFail())
			args.put("crit", successEmoji + " " + failEmoji);
		else if(bundle.hasCritSuccess())
			args.put("crit", successEmoji + " ");
		else if(bundle.hasCritFail())
			args.put("crit", failEmoji + " ");
		else
			args.put("crit", "");
		
		return args;
	}
}
