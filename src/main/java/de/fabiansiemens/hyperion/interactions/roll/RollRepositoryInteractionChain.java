package de.fabiansiemens.hyperion.interactions.roll;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import de.fabiansiemens.hyperion.core.annotations.InteractionCallback;
import de.fabiansiemens.hyperion.core.annotations.InteractionChain;
import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.roll.dto.RollPresetDto;
import de.fabiansiemens.hyperion.core.features.roll.v2.execution.ExecutionNode;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.modals.Modal;

@Slf4j
@InteractionChain
public class RollRepositoryInteractionChain extends CommonRollInteractionChain {

	private static final String ROLL_PRESET_CREATE_MODAL_PATH = "roll/RollPresetCreate.xml";
	private static final String ROLL_REPO_PANEL_PATH = "roll/RollRepositoryPanel.xml";
	private static final int MAX_ENTRIES_PER_PAGE = 1;
	
	public RollRepositoryInteractionChain(ApplicationContext context) {
		super(context);
	}
	
	@InteractionCallback(id = "b.roll.repo.new")
	public void onRepoNewPresetButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		Modal modal = uiProvider.modalOf(ROLL_PRESET_CREATE_MODAL_PATH, event.getGuild(), args);
		event.replyModal(modal).queue();
	}
	
	@InteractionCallback(id = "m.roll.repo.preset.create")
	public void onRepoNewPresetModalInteraction(ModalInteractionEvent event, Arguments args) throws UiParseException {
		event.deferEdit().queue();
		String name = event.getValue("name").getAsString();
		String prompt = event.getValue("prompt").getAsString();
		RollPresetDto dto = RollPresetDto.builder()
				.name(name)
				.prompt(prompt)
				.userId(event.getUser().getIdLong())
				.build();
		
		super.rollService.saveRollPreset(dto);
		displayRepository(event.getHook(), event.getUser(), event.getGuild(), 0, true);
	}

	@InteractionCallback(id = "b.roll.repo.page-back")
	public void onRepoPageBackButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		event.deferEdit().queue();
		int pageNr = args.get("page").asInteger();
		displayRepository(event.getHook(), event.getUser(), event.getGuild(), pageNr -1, true);
	}
	
	@InteractionCallback(id = "b.roll.repo.page-forward")
	public void onRepoPageForwardButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		event.deferEdit().queue();
		int pageNr = args.get("page").asInteger();
		displayRepository(event.getHook(), event.getUser(), event.getGuild(), pageNr +1, true);
	}
	
	public void openRepository(SlashCommandInteractionEvent event) throws UiParseException {
		event.deferReply(true).queue();
		try {
			displayRepository(event.getHook(), event.getUser(), event.getGuild(), 0, false);
		}
		catch (Exception e) {
			log.warn("Caught exception: ", e);
		}
	}
	
	@InteractionCallback(id = "b.roll.repo.run")
	public void onRepoRunButtonInteraction(ButtonInteractionEvent event, Arguments args) throws ArgumentTypeException, UiParseException {
		event.deferEdit().queue();
		long id = args.get("id").asLong();
		Optional<RollPresetDto> preset = super.rollService.findRollPresetById(id);
		
		if(preset.isEmpty()) {
			event.getHook().sendMessage(les.getLocalizedExpression("error.roll.not-found", event.getGuild())).setEphemeral(true).queue();
			return;
		}
		
		ExecutionNode result = rollService.evaluate(preset.get().getPrompt());
		String reply = getReply(result, event.getGuild(), event.getUser());
		Message response = event.getHook().sendMessage(reply).complete();
		rollService.save(preset.get().getPrompt(), result, event.getUser(), response);	
	}
	
	private void displayRepository(InteractionHook hook, User user, Guild guild, int page, boolean edit) throws UiParseException {
		Page<RollPresetDto> presets = rollService.findAllRollPresets(user, PageRequest.of(page, MAX_ENTRIES_PER_PAGE));
		Arguments args = prepareRepoArgs(Arguments.empty(), presets);
		Container rollPanel = uiProvider.panelOf(ROLL_REPO_PANEL_PATH, guild, args);
		if(edit)
			hook.editOriginalComponents(rollPanel).useComponentsV2().queue();
		else
			hook.sendMessageComponents(rollPanel).useComponentsV2().setEphemeral(true).queue();
		return;
	}
	
	private Arguments prepareRepoArgs(Arguments args, Page<RollPresetDto> presets) {
		
		List<Arguments> argList = presets.stream()
				.map(entry -> createPresetArguments(entry, presets.getNumber()))
				.collect(Collectors.toList());
		
		args.put("page", presets.getNumber())
		.put("currentPage", presets.getNumber() +1)
		.put("totalPages", presets.getTotalPages());
		
		return args.put("presets", argList.toArray(new Arguments[0]));
	}

	private Arguments createPresetArguments(RollPresetDto preset, int page) {
		Arguments arg = Arguments.of("id", preset.getId())
				.put("name", preset.getName())
				.put("prompt", preset.getPrompt())
				.put("page", page);
			
		return arg;
	}
	
}
