package de.fabiansiemens.hyperion.ui.buttons.inventory;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Button;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.ui.buttons.ButtonBase;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

@Button(dataFile = "inventory/FolderBackButton.xml")
public class InventoryFolderBackButton extends ButtonBase {

	public <T> InventoryFolderBackButton(ApplicationContext context)
			throws IOException, IllegalArgumentException, UiParseException {
		super(InventoryFolderBackButton.class, context);
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event, Arguments args) {
		// TODO Implement Folder Back Button
	}
}
