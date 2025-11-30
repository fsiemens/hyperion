package de.fabiansiemens.hyperion.ui.buttons.inventory;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Button;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.ui.buttons.ButtonBase;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Button(dataFile = "inventory/PageBackButton.xml")
public class InventoryPageBackButton extends ButtonBase {

	public <T> InventoryPageBackButton(ApplicationContext context)
			throws IOException, IllegalArgumentException, UiParseException {
		super(InventoryPageBackButton.class, context);
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		// TODO Implement Page Back Button
	}
}
