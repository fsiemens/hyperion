package de.fabiansiemens.hyperion.ui.buttons;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.ui.InteractionBase;
import io.micrometer.common.lang.Nullable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Data
@Deprecated
@EqualsAndHashCode(callSuper = true)
public abstract class ButtonBase extends InteractionBase {
	
	private final String id;
	private final String basePath;
	
	public <T> ButtonBase(Class<T> clazz, ApplicationContext context) throws IOException, IllegalArgumentException, UiParseException {
		super(clazz, de.fabiansiemens.hyperion.core.annotations.Button.class, context);
		this.basePath = context.getEnvironment().getProperty("ui.buttons.root");
		
		if(!super.getDataPath().isBlank())
			this.id = super.importService.previewButtonId(getDataPath());
		else
			this.id = super.id;
	}
	
	/**
	 * Wird ausgeführt, wenn der Button gedrückt wurde
	 * DEFAULT -> Defer Edit
	 * @param event - ButtonInteractionEvent
	 * @param args 
	 */
	public void onButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		event.deferEdit().queue();
	};
	
	public Button get(@Nullable Guild guild) throws UiParseException {
		return get(Arguments.empty(), guild, true);
	}
	
	public Button get(@Nullable Guild guild, boolean enabled) throws UiParseException {
		return get(Arguments.empty(), guild, enabled);
	}
	
	public Button get(@NonNull Arguments args, @Nullable Guild guild) throws UiParseException {
		return get(args, guild, true);
	}
	
	public Button get(@NonNull Arguments args, @Nullable Guild guild, boolean enabled) throws UiParseException {
		Button button = importService.importButton(getDataPath(), args, guild);
		
		if(enabled)
			return button.asEnabled();
		return button.asDisabled();
	}

	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public String getDataPath() {
		return this.basePath + super.getDataPath();
	}
}
