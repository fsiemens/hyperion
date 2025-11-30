package de.fabiansiemens.hyperion.ui.buttons.inventory;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Button;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.ui.buttons.ButtonBase;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Button(dataFile = "inventory/EditElementButton.xml")
public class InventoryEditElementButton extends ButtonBase {

	public <T> InventoryEditElementButton(ApplicationContext context)
			throws IOException, IllegalArgumentException, UiParseException {
		super(InventoryEditElementButton.class, context);
	}

	@Override
	public void onButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		// TODO Implement Edit Element Button
	}
}
