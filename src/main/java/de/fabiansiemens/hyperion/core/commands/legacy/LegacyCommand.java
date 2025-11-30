package de.fabiansiemens.hyperion.core.commands.legacy;

import de.fabiansiemens.hyperion.core.commands.ICommand;
import de.fabiansiemens.hyperion.core.events.LegacyCommandEvent;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;

public interface LegacyCommand extends ICommand {
	public boolean shouldDeleteInitMessage();
	public void performLegacyCommand(LegacyCommandEvent event) throws UiParseException;
}
