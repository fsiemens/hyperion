package de.fabiansiemens.hyperion.core.exceptions;

public class UiParseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5500947099455019483L;

	public UiParseException() {
	}

	public UiParseException(String message) {
		super(message);
	}

	public UiParseException(Throwable cause) {
		super(cause);
	}

	public UiParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public UiParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
