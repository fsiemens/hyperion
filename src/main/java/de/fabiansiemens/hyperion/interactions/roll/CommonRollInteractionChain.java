package de.fabiansiemens.hyperion.interactions.roll;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Primary;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.events.LegacyCommandEvent;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.roll.RollService;
import de.fabiansiemens.hyperion.core.features.roll.dto.RollPresetDto;
import de.fabiansiemens.hyperion.core.features.roll.v2.RollCacheEntry;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.guild.GuildService;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.interactions.InteractionChainBase;
import de.fabiansiemens.hyperion.ui.roll.RollDetailsPanel;
import io.micrometer.common.lang.Nullable;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;

@Primary
@InteractionChain
public class CommonRollInteractionChain extends InteractionChainBase {
	
//	private static final String CLOSE_DETAILS_BUTTON_PATH = "roll/CloseDetails.xml";
	private static final String ROLL_DETAIL_BUTTON_PATH = "roll/RollDetails.xml";
	private static final String ROLL_AGAIN_BUTTON_PATH = "roll/RollAgain.xml";
	
	@Value("${roll.emoji.success}")
	public String successEmoji;
	
	@Value("${roll.emoji.fail}")
	public String failEmoji;
	
	protected final GuildService guildService;
	protected final UserService userService;
	protected final RollService rollService;
	protected final RollDetailsPanel detailsPanel;
	
	public CommonRollInteractionChain(ApplicationContext context) {
		super(context);
		this.guildService = context.getBean(GuildService.class);
		this.userService = context.getBean(UserService.class);
		this.rollService = context.getBean(RollService.class);
		this.detailsPanel = context.getBean(RollDetailsPanel.class);
	}
	
	public void onRollLegacyCommandInteraction(LegacyCommandEvent event) throws UiParseException {
		Guild guild = (event.isFromGuild() ? event.getGuild() : null);
		ExecutionNode result = rollService.evaluate(event.getPrompt());
		String reply = getReply(result, event.getGuild(), event.getMessage().getAuthor());
		
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, guild);
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, guild);
		
		Message response = event.getChannel().sendMessage(reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		rollService.save(event.getPrompt(), result, event.getAuthor(), response);
	}

	public void onRollSlashCommandInteraction(SlashCommandInteractionEvent event) throws UiParseException {
		String prompt = event.getOption("prompt", null, mapping -> mapping.getAsString());
		
		if(prompt == null || prompt.isBlank()) {
			RollRepositoryInteractionChain repoInteractionChain = context.getBean(RollRepositoryInteractionChain.class);
			repoInteractionChain.openRepository(event);
			return;
		}
		
		//if prompt option is set
		ExecutionNode result = rollService.evaluate(prompt);
		String reply = getReply(result, event.getGuild(), event.getUser());
		
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, event.getGuild());
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild());
		
		InteractionHook hook = event.reply(reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		rollService.save(prompt, result, event.getUser(), hook.retrieveOriginal().complete());	
	}

	@InteractionCallback(id = "b.roll.again")
	public void onRollAgainButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		Optional<RollCacheEntry> rollResult = rollService.loadByMessageId(event.getMessageIdLong());
		
		if(rollResult.isEmpty()) {
			event.reply(les.getLocalizedExpression("error.roll-detail-button.timeout", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		String rollPrompt = rollResult.get().getNode().getPrompt();
		ExecutionNode result = rollService.evaluate(rollPrompt);
		String reply = getReply(result, event.getGuild(), event.getUser());
		Button rollDetailButton = uiProvider.buttonOf(ROLL_DETAIL_BUTTON_PATH, event.getGuild(), args);
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild(), args);
		
		InteractionHook hook = event.reply(reply)
			.addComponents(ActionRow.of(rollDetailButton, rollAgainButton))
			.complete();
		
		rollService.save(rollPrompt, result, event.getUser(), hook.retrieveOriginal().complete());	
	}
	
	@InteractionCallback(id = "b.roll.details")
	public void onRollDetailsButtonInteraction(ButtonInteractionEvent event, Arguments args) throws UiParseException {
		Message message = event.getMessage();
		Optional<RollCacheEntry> result = rollService.loadByMessageId(message.getIdLong());
		
		if(result.isEmpty()) {
			event.reply(les.getLocalizedExpression("error.roll-detail-button.timeout", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild(), args);
		//Button closeDetailsButton = uiProvider.buttonOf(CLOSE_DETAILS_BUTTON_PATH, event.getGuild(), args);
		Container container = detailsPanel.build(event.getGuild(), result.get(), 0);
		event.editMessage("").setComponents(container, ActionRow.of(rollAgainButton)).useComponentsV2().queue();
	}
	
	@InteractionCallback(id = "ss.roll.details")
	public void onRollDetailsSelectInteraction(StringSelectInteractionEvent event, Arguments args) throws UiParseException {
		Message message = event.getMessage();
		Optional<RollCacheEntry> result = rollService.loadByMessageId(message.getIdLong());
		
		if(result.isEmpty()) {
			event.reply(les.getLocalizedExpression("error.roll-detail-button.timeout", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		Button rollAgainButton = uiProvider.buttonOf(ROLL_AGAIN_BUTTON_PATH, event.getGuild(), args);
		//Button closeDetailsButton = uiProvider.buttonOf(CLOSE_DETAILS_BUTTON_PATH, event.getGuild(), args);
		try {
			int index = Integer.parseInt(event.getValues().getFirst());
			Container container = detailsPanel.build(event.getGuild(), result.get(), index);
			event.editMessage("").setComponents(container, ActionRow.of(rollAgainButton)).useComponentsV2().queue();
		}
		catch (NumberFormatException e) {
			event.reply(les.getLocalizedExpression("error.common.interaction", event.getGuild(), Arguments.of("message", e.getMessage()))).setEphemeral(true).queue();
		}
		
	}
	
	public String getReply(ExecutionNode result, @Nullable Guild guild, User user) {
		Arguments args = prepareRollArgs(result, Arguments.empty(), user);
		
		String reply;
//		String label = null; //TODO re-implement Roll Labels
//		if(label == null || label.isBlank())
			if(result.hasCheck())
				reply = les.getLocalizedExpression("success.roll.response-with-check", guild, args);
			else
				reply = les.getLocalizedExpression("success.roll.base-response", guild, args);
//		else
//			if(result.hasCheck())
//				reply = les.getLocalizedExpression("success.roll.response-with-check-and-label", guild, args);
//			else
//				reply = les.getLocalizedExpression("success.roll.response-with-label", guild, args);
		
		return reply;
	}
	
	private Arguments prepareRollArgs(ExecutionNode result, Arguments args, User user) {
		args.put("mention", user.getAsMention())
			.put("result", "" + result.getResolvedValue())
			.put("label", null)
			.put("prompt", result.getPrompt());
			
		if(result.successCount() > Math.floorDiv(result.getChecks().size(), 2))
			args.put("check", "@{.roll.succeeds}@");
		else if(result.successCount() < Math.floorDiv(result.getChecks().size(), 2))
			args.put("check", "@{.roll.fails}@");
		else
			args.put("check", "@{.roll.draws}@");
		
		if(!result.hasCrits()) {
			args.put("crit", "");
		}
		else {
			final long sucCount = result.getCrits().stream().filter(crit -> crit.isCritSuccess()).count();
			final long failCount = result.getCrits().stream().filter(crit -> !crit.isCritSuccess()).count();
			
			if(sucCount > 0 && failCount > 0)
				args.put("crit", successEmoji + " " + failEmoji);
			else if(sucCount > 0)
				args.put("crit", successEmoji + " ");
			else
				args.put("crit", failEmoji + " ");
		}
			
		return args;
	}
}
