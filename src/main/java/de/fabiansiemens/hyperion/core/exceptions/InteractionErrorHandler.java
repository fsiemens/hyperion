package de.fabiansiemens.hyperion.core.exceptions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback;

/**
 * Central error boundary for Discord interactions and legacy commands.<br>
 * Guarantees that every interaction receives a response, even if a handler throws an unexpected exception.
 * <ul>
 * <li>{@link UiParseException} &rarr; {@code error.common.ui}</li>
 * <li>Exception messages that look like a locale key ({@code error.*}, {@code warn.*}) &rarr; localized message</li>
 * <li>Everything else &rarr; {@code error.common.unexpected} with a short error ID, full stack trace in the log</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InteractionErrorHandler {

	private static final Pattern LOCALE_KEY = Pattern.compile("^(error|warn)\\.[A-Za-z0-9._-]+$");
	private static final String UI_ERROR_KEY = "error.common.ui";
	private static final String UNEXPECTED_KEY = "error.common.unexpected";
	private static final String FALLBACK_TEXT = "An unexpected error occurred.";

	private final LocalizedExpressionService les;

	/**
	 * Answers an interaction with a message describing the given error.
	 * Uses the interaction hook if the interaction was already acknowledged.
	 */
	public void handle(IReplyCallback event, Throwable error) {
		String text = resolveMessage(unwrap(error), event.getGuild());
		send(event, text);
	}

	/**
	 * Sends an error message for a failed legacy command into the channel of the triggering message.
	 */
	public void handle(MessageReceivedEvent event, Throwable error) {
		Guild guild = event.isFromGuild() ? event.getGuild() : null;
		String text = resolveMessage(unwrap(error), guild);
		event.getChannel().sendMessage(text)
			.queue(null, failure -> log.warn("Could not send error message for legacy command", failure));
	}

	/**
	 * Answers an interaction ephemerally with the localized expression of the given key.
	 */
	public void reply(IReplyCallback event, String localeKey) {
		String text;
		try {
			text = les.getLocalizedExpression(localeKey, event.getGuild());
		} catch (Exception e) {
			log.warn("Could not resolve locale key '{}'", localeKey, e);
			text = FALLBACK_TEXT;
		}
		send(event, text);
	}

	private void send(IReplyCallback event, String text) {
		if(event.isAcknowledged()) {
			event.getHook().sendMessage(text).setEphemeral(true)
				.queue(null, failure -> log.warn("Could not send error message via interaction hook", failure));
			return;
		}

		event.reply(text).setEphemeral(true)
			.queue(null, failure -> log.warn("Could not reply with error message", failure));
	}

	private String resolveMessage(Throwable cause, @Nullable Guild guild) {
		try {
			if(cause instanceof UiParseException) {
				log.warn("UI could not be parsed", cause);
				return les.getLocalizedExpression(UI_ERROR_KEY, guild);
			}

			String message = cause.getMessage();
			if(message != null && LOCALE_KEY.matcher(message).matches()) {
				log.debug("Handled error with locale key '{}'", message);
				return les.getLocalizedExpression(message, guild);
			}

			String errorId = UUID.randomUUID().toString().substring(0, 8);
			log.error("Unexpected error [{}]", errorId, cause);
			return les.getLocalizedExpression(UNEXPECTED_KEY, guild, Arguments.of("errorId", errorId));
		}
		catch (Exception e) {
			log.error("Error while resolving error message", e);
			return FALLBACK_TEXT;
		}
	}

	private Throwable unwrap(Throwable error) {
		Throwable current = error;
		while(current.getCause() != null
				&& (current instanceof InvocationTargetException || current instanceof UndeclaredThrowableException)) {
			current = current.getCause();
		}
		return current;
	}
}
