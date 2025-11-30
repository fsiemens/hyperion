package de.fabiansiemens.hyperion.core.ui;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import io.micrometer.common.lang.Nullable;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.modals.Modal;

@Service
@RequiredArgsConstructor
public class UiProviderService {

	private final ApplicationContext context;

	@Value("${ui.buttons.root}")
	private String buttonsRoot;
	
	@Value("${ui.selects.root}")
	private String selectRoot;
	
	@Value("${ui.modals.root}")
	private String modalRoot;
	
//	@Value("${ui.embeds.root}")
//	private String embedRoot;
	
	@Value("${ui.panels.root}")
	private String panelsRoot;
	
	
	public Button buttonOf(@NonNull String dataPath, @Nullable Guild guild) throws UiParseException {
		return buttonOf(dataPath, guild, Arguments.empty(), true);
	}
	
	public Button buttonOf(@NonNull String dataPath, @Nullable Guild guild, boolean enabled) throws UiParseException {
		return buttonOf(dataPath, guild, Arguments.empty(), enabled);
	}
	
	public Button buttonOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Arguments args) throws UiParseException {
		return buttonOf(dataPath, guild, args, true);
	}
	
	public Button buttonOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Arguments args, boolean enabled) throws UiParseException {
		UiImportService importService = context.getBean(UiImportService.class);
		Button button = importService.importButton(buttonsRoot + dataPath, args, guild);
		
		if(enabled)
			return button.asEnabled();
		return button.asDisabled();
	}
	
	
	public Modal modalOf(@NonNull String dataPath, @Nullable Guild guild) throws UiParseException {
		return modalOf(dataPath, guild, Arguments.empty());
	}
	
	public Modal modalOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Arguments args) throws UiParseException {
		UiImportService importService = context.getBean(UiImportService.class);
		return importService.importModal(modalRoot + dataPath, args, guild);
	}
	
	
	public StringSelectMenu stringSelectOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Collection<SelectOption> options, SelectOption... defaultOptions) throws UiParseException {
		return stringSelectOf(dataPath, guild, Arguments.empty(), options, defaultOptions);
	}
	
	public StringSelectMenu stringSelectOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Arguments args, @NonNull Collection<SelectOption> options, SelectOption... defaultOptions) throws UiParseException {
		UiImportService importService = context.getBean(UiImportService.class);
		return importService.importStringSelect(selectRoot + dataPath, args, guild, options, defaultOptions);
	}
	
	
	public Container panelOf(@NonNull String dataPath, @Nullable Guild guild) throws UiParseException {
		return panelOf(dataPath, guild, Arguments.empty()); 
	}
	
	public Container panelOf(@NonNull String dataPath, @Nullable Guild guild, @NonNull Arguments args) throws UiParseException {
		UiImportService importService = context.getBean(UiImportService.class);
		return importService.importPanel(panelsRoot + dataPath, args, guild);
	}
}
