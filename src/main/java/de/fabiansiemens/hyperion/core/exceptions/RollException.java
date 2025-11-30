package de.fabiansiemens.hyperion.core.exceptions;

import de.fabiansiemens.hyperion.core.util.Arguments;
import jakarta.annotation.Nullable;
import lombok.Getter;

public class RollException extends Exception {

	/**  */
	private static final long serialVersionUID = 2458824734974273454L;
	@Getter
	@Nullable
	private Arguments args;
	
	public RollException(String message, @Nullable Arguments args) {
		super(message);
		this.args = args;
	}
}
