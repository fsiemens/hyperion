package de.fabiansiemens.hyperion.core.exceptions;

/**
 * Expected failure of a draw action. The message is a locale key ("error.draw.*"),
 * which the InteractionErrorHandler resolves and sends ephemerally.
 */
public class DrawException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public DrawException(String localeKey) {
		super(localeKey);
	}
}
