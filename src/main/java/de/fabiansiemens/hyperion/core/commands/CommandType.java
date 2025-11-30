package de.fabiansiemens.hyperion.core.commands;

public enum CommandType {
	/**
	 * LEGACY Commands can be run by sending a message starting with a command symbol (i.e. "!"), followed by a command prompt in the regular chat
	 */
	LEGACY,
	/**
	 * SLASH Command can be run by typing / in the chat bar and selecting a command from the provided list. <br>
	 * Slash Commands are native to Discord and the preferred way of using commands
	 */
	SLASH,
	/**
	 * UNIVERSAL Commands are available as both LEGACY and SLASH Commands
	 */
	UNIVERSAL
}
