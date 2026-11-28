package de.fabiansiemens.hyperion.interactions.draw;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.DrawException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.draw.DrawResult;
import de.fabiansiemens.hyperion.core.features.draw.DrawService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.core.util.TextUtil;
import de.fabiansiemens.hyperion.interactions.InteractionChainBase;
import de.fabiansiemens.hyperion.persistence.draw.DrawEntity;
import de.fabiansiemens.hyperion.persistence.draw.DrawPrizeEntity;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.MessageTopLevelComponent;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.MentionType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;

/**
 * Public draw panel (/draw) and the owner's ephemeral settings panel.
 */
@Slf4j
@InteractionChain
public class DrawInteractionChain extends InteractionChainBase {

	private static final String PANEL_PATH = "draw/DrawPanel.xml";
	private static final String SETTINGS_PANEL_PATH = "draw/DrawSettingsPanel.xml";
	private static final String PRIZE_SELECT_PATH = "draw/DrawPrizeSelect.xml";
	private static final String PRIZE_CREATE_MODAL_PATH = "draw/DrawPrizeCreate.xml";
	private static final String PRIZE_EDIT_MODAL_PATH = "draw/DrawPrizeEdit.xml";
	private static final String PRIZE_REMOVE_MODAL_PATH = "draw/DrawPrizeRemove.xml";
	private static final String DEFAULT_PRIZE_MODAL_PATH = "draw/DrawDefaultPrize.xml";
	private static final String MAX_MODAL_PATH = "draw/DrawMaxParticipants.xml";
	private static final String PARTICIPANT_REMOVE_MODAL_PATH = "draw/DrawParticipantRemove.xml";

	/** Components V2 messages allow 4000 characters of text in total; leave room for the fixed texts. */
	private static final int PANEL_TEXT_BUDGET = 3300;
	private static final int MIN_PARTICIPANT_BUDGET = 500;
	private static final int SUMMARY_LENGTH = 90;

	private final DrawService drawService;

	public DrawInteractionChain(ApplicationContext context) {
		super(context);
		this.drawService = context.getBean(DrawService.class);
	}

	// ===== Command =====

	public void onDrawCommand(SlashCommandInteractionEvent event) throws UiParseException {
		Guild guild = event.getGuild();
		if(guild == null)
			throw new DrawException("error.draw.guild-only");

		DrawEntity draw = drawService.create(guild.getIdLong(), event.getChannelIdLong(), event.getUser().getIdLong());
		Container panel = buildPanel(draw, guild);

		event.replyComponents(panel).useComponentsV2()
			.flatMap(InteractionHook::retrieveOriginal)
			.queue(
				message -> drawService.attachMessage(draw.getId(), message.getIdLong()),
				failure -> log.warn("Could not send or resolve panel of draw {}", draw.getId(), failure));
	}

	// ===== Public panel =====

	@InteractionCallback(id = "b.draw.join")
	public void onJoinButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		long drawId = drawId(args);
		ensureMessage(drawId, event.getMessageIdLong());
		DrawEntity draw = drawService.join(drawId, event.getUser().getIdLong());
		event.editComponents(buildPanel(draw, event.getGuild())).useComponentsV2().queue();
	}

	@InteractionCallback(id = "b.draw.leave")
	public void onLeaveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		long drawId = drawId(args);
		ensureMessage(drawId, event.getMessageIdLong());
		DrawEntity draw = drawService.leave(drawId, event.getUser().getIdLong());
		event.editComponents(buildPanel(draw, event.getGuild())).useComponentsV2().queue();
	}

	@InteractionCallback(id = "b.draw.my-prize")
	public void onMyPrizeButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException {
		DrawResult result = drawService.getResult(drawId(args))
				.orElseThrow(() -> new DrawException("error.draw.no-result"));
		DrawResult.Prize prize = result.assignments().get(event.getUser().getIdLong());
		if(prize == null)
			throw new DrawException("error.draw.not-in-round");

		Arguments textArgs = Arguments.of("round", result.round())
				.put("prize", prize.name())
				.put("message", nonNull(prize.message()));
		String key = prize.message() == null ? ".draw.my-prize" : ".draw.my-prize-with-message";
		event.reply(les.getLocalizedExpression(key, event.getGuild(), textArgs)).setEphemeral(true).queue();
	}

	@InteractionCallback(id = "b.draw.settings")
	public void onSettingsButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		long drawId = drawId(args);
		ensureMessage(drawId, event.getMessageIdLong());
		DrawEntity draw = drawService.get(drawId);
		drawService.requireOwner(draw, event.getUser().getIdLong());
		event.replyComponents(buildSettings(draw, event.getGuild())).useComponentsV2().setEphemeral(true).queue();
	}

	@InteractionCallback(id = "b.draw.start")
	public void onStartButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		long drawId = drawId(args);
		long userId = event.getUser().getIdLong();
		Guild guild = event.getGuild();
		ensureMessage(drawId, event.getMessageIdLong());
		drawService.requireOwner(drawService.get(drawId), userId);

		// Sending DMs takes longer than the 3 seconds Discord allows for an answer
		event.deferReply(true).queue();

		String defaultPrizeName = les.getLocalizedExpression(".draw.default-prize", guild);
		DrawResult result = drawService.draw(drawId, userId, defaultPrizeName);
		DrawEntity draw = drawService.get(drawId);

		event.getMessage().editMessageComponents(buildPanel(draw, guild)).useComponentsV2()
			.queue(null, failure -> log.warn("Could not update panel of draw {}", drawId, failure));

		sendResults(event, draw, result);
	}

	@InteractionCallback(id = "b.draw.reveal")
	public void onRevealButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		long drawId = drawId(args);
		drawService.reveal(drawId, event.getUser().getIdLong());
		DrawEntity draw = drawService.get(drawId);
		event.editComponents(buildPanel(draw, event.getGuild())).useComponentsV2().queue();
	}

	// ===== Settings: prizes =====

	@InteractionCallback(id = "b.draw.prize.add")
	public void onPrizeAddButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		event.replyModal(uiProvider.modalOf(PRIZE_CREATE_MODAL_PATH, event.getGuild(), Arguments.of("draw", draw.getId()))).queue();
	}

	@InteractionCallback(id = "m.draw.prize.create")
	public void onPrizeCreateModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = drawService.addPrize(drawId(args), event.getUser().getIdLong(),
				value(event, "name"), parseCount(value(event, "count")), value(event, "message"));
		refreshSettings(event, draw);
	}

	@InteractionCallback(id = "ss.draw.prize.edit")
	public void onPrizeEditSelectInteraction(StringSelectInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		DrawPrizeEntity prize = drawService.findPrize(draw, parseId(event.getValues().getFirst()));

		Arguments modalArgs = Arguments.of("draw", draw.getId())
				.put("prize", prize.getId())
				.put("name", prize.getName())
				.put("count", String.valueOf(prize.getCount()))
				.put("message", nonNull(prize.getMessage()));
		event.replyModal(uiProvider.modalOf(PRIZE_EDIT_MODAL_PATH, event.getGuild(), modalArgs)).queue();
	}

	@InteractionCallback(id = "m.draw.prize.edit")
	public void onPrizeEditModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = drawService.updatePrize(drawId(args), event.getUser().getIdLong(), args.get("prize").asLong(),
				value(event, "name"), parseCount(value(event, "count")), value(event, "message"));
		refreshSettings(event, draw);
	}

	@InteractionCallback(id = "b.draw.prize.remove")
	public void onPrizeRemoveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		if(draw.getPrizes().isEmpty())
			throw new DrawException("error.draw.prize-not-found");

		Arguments modalArgs = Arguments.of("draw", draw.getId()).put("prizes", prizeOptions(draw));
		event.replyModal(uiProvider.modalOf(PRIZE_REMOVE_MODAL_PATH, event.getGuild(), modalArgs)).queue();
	}

	@InteractionCallback(id = "m.draw.prize.remove")
	public void onPrizeRemoveModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		ModalMapping selection = event.getValue("prize");
		if(selection == null || selection.getAsStringList().isEmpty())
			throw new DrawException("error.draw.prize-not-found");

		DrawEntity draw = drawService.removePrize(drawId(args), event.getUser().getIdLong(), parseId(selection.getAsStringList().getFirst()));
		refreshSettings(event, draw);
	}

	// ===== Settings: default prize, limit, participants =====

	@InteractionCallback(id = "b.draw.default-prize")
	public void onDefaultPrizeButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		Arguments modalArgs = Arguments.of("draw", draw.getId())
				.put("name", nonNull(draw.getDefaultPrizeName()))
				.put("message", nonNull(draw.getDefaultPrizeMessage()));
		event.replyModal(uiProvider.modalOf(DEFAULT_PRIZE_MODAL_PATH, event.getGuild(), modalArgs)).queue();
	}

	@InteractionCallback(id = "m.draw.default-prize")
	public void onDefaultPrizeModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = drawService.setDefaultPrize(drawId(args), event.getUser().getIdLong(),
				value(event, "name"), value(event, "message"));
		refreshSettings(event, draw);
	}

	@InteractionCallback(id = "b.draw.max")
	public void onMaxButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		Arguments modalArgs = Arguments.of("draw", draw.getId())
				.put("max", draw.getMaxParticipants() == null ? "" : String.valueOf(draw.getMaxParticipants()));
		event.replyModal(uiProvider.modalOf(MAX_MODAL_PATH, event.getGuild(), modalArgs)).queue();
	}

	@InteractionCallback(id = "m.draw.max")
	public void onMaxModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		String raw = value(event, "max");
		Integer max = null;
		if(raw != null && !raw.isBlank()) {
			try {
				max = Integer.parseInt(raw.strip());
			} catch(NumberFormatException e) {
				throw new DrawException("error.draw.invalid-max");
			}
		}
		DrawEntity draw = drawService.setMaxParticipants(drawId(args), event.getUser().getIdLong(), max);
		refreshSettings(event, draw);
	}

	@InteractionCallback(id = "b.draw.participant.remove")
	public void onParticipantRemoveButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		DrawEntity draw = ownedDraw(args, event.getUser().getIdLong());
		event.replyModal(uiProvider.modalOf(PARTICIPANT_REMOVE_MODAL_PATH, event.getGuild(), Arguments.of("draw", draw.getId()))).queue();
	}

	@InteractionCallback(id = "m.draw.participant.remove")
	public void onParticipantRemoveModalInteraction(ModalInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		ModalMapping selection = event.getValue("users");
		List<Long> userIds = selection == null ? List.of()
				: selection.getAsStringList().stream().map(this::parseId).toList();

		long drawId = drawId(args);
		int removed = drawService.removeParticipants(drawId, event.getUser().getIdLong(), userIds);
		refreshSettings(event, drawService.get(drawId));
		event.getHook().sendMessage(les.getLocalizedExpression("success.draw.participants-removed", event.getGuild(), Arguments.of("count", removed)))
			.setEphemeral(true).queue();
	}

	// ===== Draw results =====

	private void sendResults(ButtonInteractionEvent event, DrawEntity draw, DrawResult result) {
		Guild guild = event.getGuild();
		String jumpUrl = Message.JUMP_URL.formatted(draw.getGuildId(), draw.getChannelId(), event.getMessageIdLong());

		List<CompletableFuture<Long>> deliveries = new ArrayList<>();
		for(Map.Entry<Long, DrawResult.Prize> entry : result.assignments().entrySet()) {
			long userId = entry.getKey();
			String text = dmText(guild, result, entry.getValue(), jumpUrl);
			deliveries.add(event.getJDA().openPrivateChannelById(userId)
				.flatMap(channel -> channel.sendMessage(text))
				.submit()
				.handle((message, failure) -> {
					if(failure == null)
						return null;
					log.debug("Could not send draw result to user {}", userId, failure);
					return userId;
				}));
		}

		CompletableFuture.allOf(deliveries.toArray(CompletableFuture[]::new)).thenRun(() -> {
			List<Long> failed = deliveries.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList();
			String failedMentions = failed.stream().map(this::mention).collect(Collectors.joining(" "));
			Arguments summaryArgs = Arguments.of("round", result.round())
					.put("delivered", deliveries.size() - failed.size())
					.put("total", deliveries.size())
					.put("failed", failedMentions);

			if(!failed.isEmpty()) {
				event.getChannel().sendMessage(les.getLocalizedExpression(".draw.dm-failed", guild, summaryArgs))
					.setAllowedMentions(EnumSet.of(MentionType.USER))
					.queue(null, failure -> log.warn("Could not ping unreachable participants of draw {}", draw.getId(), failure));
			}

			String key = failed.isEmpty() ? "success.draw.drawn" : "success.draw.drawn-with-failures";
			event.getHook().sendMessage(les.getLocalizedExpression(key, guild, summaryArgs)).setEphemeral(true).queue();
		});
	}

	private String dmText(Guild guild, DrawResult result, DrawResult.Prize prize, String jumpUrl) {
		Arguments args = Arguments.of("round", result.round())
				.put("prize", prize.name())
				.put("message", nonNull(prize.message()))
				.put("guild", guild == null ? "" : guild.getName())
				.put("link", jumpUrl);
		String key = prize.message() == null ? ".draw.dm" : ".draw.dm-with-message";
		return les.getLocalizedExpression(key, guild, args);
	}

	// ===== Rendering =====

	private Container buildPanel(DrawEntity draw, @Nullable Guild guild) throws UiParseException {
		Optional<DrawResult> result = drawService.getResult(draw.getId());
		boolean revealed = result.map(DrawResult::revealed).orElse(false);

		String prizeList = prizeList(draw);
		int participantBudget = Math.max(MIN_PARTICIPANT_BUDGET, PANEL_TEXT_BUDGET - prizeList.length());

		Arguments args = Arguments.of("draw", draw.getId())
				.put("owner", mention(draw.getOwnerId()))
				.put("participantCount", draw.getParticipants().size())
				.put("maxParticipants", maxText(draw))
				.put("isFull", drawService.isFull(draw))
				.put("participantList", participantList(draw, revealed ? result.get() : null, participantBudget))
				.put("prizeList", prizeList)
				.put("round", draw.getRound())
				.put("lastDraw", draw.getLastDrawAt() == null ? "" : "<t:" + draw.getLastDrawAt().getEpochSecond() + ":R>")
				.put("hasResult", result.isPresent())
				.put("revealed", revealed);
		return uiProvider.panelOf(PANEL_PATH, guild, args);
	}

	private List<MessageTopLevelComponent> buildSettings(DrawEntity draw, @Nullable Guild guild) throws UiParseException {
		Arguments args = Arguments.of("draw", draw.getId())
				.put("prizeCount", draw.getPrizes().size())
				.put("participantCount", draw.getParticipants().size())
				.put("prizeDetails", prizeDetails(draw))
				.put("defaultPrizeDetails", defaultPrizeDetails(draw))
				.put("maxParticipants", maxText(draw))
				.put("prizes", prizeOptions(draw));

		List<MessageTopLevelComponent> components = new ArrayList<>();
		components.add(uiProvider.panelOf(SETTINGS_PANEL_PATH, guild, args));
		if(!draw.getPrizes().isEmpty()) {
			StringSelectMenu select = uiProvider.stringSelectOf(PRIZE_SELECT_PATH, guild, args, new LinkedList<>());
			components.add(ActionRow.of(select));
		}
		return components;
	}

	private void refreshSettings(ModalInteractionEvent event, DrawEntity draw) throws UiParseException {
		Guild guild = event.getGuild();
		event.editComponents(buildSettings(draw, guild)).useComponentsV2().queue();
		updatePublicPanel(draw, guild);
	}

	private void updatePublicPanel(DrawEntity draw, @Nullable Guild guild) throws UiParseException {
		if(guild == null || draw.getMessageId() == null)
			return;

		GuildMessageChannel channel = guild.getChannelById(GuildMessageChannel.class, draw.getChannelId());
		if(channel == null) {
			log.debug("Channel of draw {} not found", draw.getId());
			return;
		}

		channel.editMessageComponentsById(draw.getMessageId(), buildPanel(draw, guild)).useComponentsV2()
			.queue(null, failure -> log.debug("Could not update panel of draw {}", draw.getId(), failure));
	}

	/** Lists participants until the character budget is used up. */
	private String participantList(DrawEntity draw, @Nullable DrawResult revealedResult, int budget) {
		List<Long> participants = draw.getParticipants();
		if(participants.isEmpty())
			return "*@{.draw.panel.no-participants}@*";

		StringBuilder sb = new StringBuilder();
		int listed = 0;
		for(long userId : participants) {
			StringBuilder line = new StringBuilder();
			line.append("`").append(listed + 1).append(".` ").append(mention(userId));
			if(revealedResult != null) {
				DrawResult.Prize prize = revealedResult.assignments().get(userId);
				line.append(" – ").append(prize == null ? "@{.draw.panel.no-prize-this-round}@" : "**" + prize.name() + "**");
			}

			if(sb.length() + line.length() + 1 > budget)
				break;
			if(listed > 0)
				sb.append("\n");
			sb.append(line);
			listed++;
		}

		if(participants.size() > listed)
			sb.append("\n*@{.draw.panel.more-participants}@ (").append(participants.size() - listed).append(")*");
		return sb.toString();
	}

	private String prizeList(DrawEntity draw) {
		StringBuilder sb = new StringBuilder();
		for(DrawPrizeEntity prize : draw.getPrizes())
			sb.append("> `").append(prize.getCount()).append("×` **").append(prize.getName()).append("**\n");

		String restLabel = draw.getPrizes().isEmpty() ? "@{.draw.panel.prize-all}@" : "@{.draw.panel.prize-rest}@";
		sb.append("> `").append(restLabel).append("` **").append(defaultPrizeName(draw)).append("**");
		return sb.toString();
	}

	private String prizeDetails(DrawEntity draw) {
		if(draw.getPrizes().isEmpty())
			return "*@{.draw.settings.no-prizes}@*";

		StringBuilder sb = new StringBuilder();
		for(DrawPrizeEntity prize : draw.getPrizes()) {
			if(!sb.isEmpty())
				sb.append("\n");
			sb.append("> `").append(prize.getCount()).append("×` **").append(prize.getName()).append("**");
			if(prize.getMessage() != null)
				sb.append("\n-# ").append(TextUtil.truncate(oneLine(prize.getMessage()), SUMMARY_LENGTH));
		}
		return sb.toString();
	}

	private String defaultPrizeDetails(DrawEntity draw) {
		String details = "> **" + defaultPrizeName(draw) + "**";
		if(draw.getDefaultPrizeMessage() != null)
			details += "\n-# " + TextUtil.truncate(oneLine(draw.getDefaultPrizeMessage()), SUMMARY_LENGTH);
		return details;
	}

	private Arguments[] prizeOptions(DrawEntity draw) {
		return draw.getPrizes().stream()
				.map(prize -> Arguments.of("id", prize.getId())
						.put("name", prize.getName())
						.put("summary", TextUtil.truncate(prize.getCount() + "×"
								+ (prize.getMessage() == null ? "" : " – " + oneLine(prize.getMessage())), SUMMARY_LENGTH)))
				.toArray(Arguments[]::new);
	}

	private String defaultPrizeName(DrawEntity draw) {
		return draw.getDefaultPrizeName() != null ? draw.getDefaultPrizeName() : "@{.draw.default-prize}@";
	}

	private String maxText(DrawEntity draw) {
		return draw.getMaxParticipants() == null ? "@{.draw.unlimited}@" : String.valueOf(draw.getMaxParticipants());
	}

	// ===== Helpers =====

	private DrawEntity ownedDraw(Arguments args, long userId) throws ArgumentTypeException {
		DrawEntity draw = drawService.get(drawId(args));
		drawService.requireOwner(draw, userId);
		return draw;
	}

	/** Stores the panel message id if attaching it after /draw failed. */
	private void ensureMessage(long drawId, long messageId) {
		DrawEntity draw = drawService.get(drawId);
		if(draw.getMessageId() == null)
			drawService.attachMessage(drawId, messageId);
	}

	private long drawId(Arguments args) throws ArgumentTypeException {
		return args.get("draw").asLong();
	}

	private long parseId(String raw) {
		try {
			return Long.parseLong(raw);
		} catch(NumberFormatException e) {
			throw new DrawException("error.draw.prize-not-found");
		}
	}

	private int parseCount(@Nullable String raw) {
		try {
			return Integer.parseInt(raw == null ? "" : raw.strip());
		} catch(NumberFormatException e) {
			throw new DrawException("error.draw.invalid-count");
		}
	}

	private static @Nullable String value(ModalInteractionEvent event, String id) {
		ModalMapping mapping = event.getValue(id);
		return mapping == null ? null : mapping.getAsString();
	}

	private String mention(long userId) {
		return "<@" + userId + ">";
	}

	private static String nonNull(@Nullable String value) {
		return value == null ? "" : value;
	}

	private static String oneLine(String value) {
		return value.replaceAll("\\s+", " ");
	}
}
