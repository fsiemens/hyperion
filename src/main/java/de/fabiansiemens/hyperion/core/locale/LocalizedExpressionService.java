package de.fabiansiemens.hyperion.core.locale;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.guild.GuildData;
import de.fabiansiemens.hyperion.core.guild.GuildService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import de.fabiansiemens.hyperion.core.util.TextUtil;
import jakarta.annotation.Nullable;
import lombok.NonNull;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Service
public class LocalizedExpressionService {
	
	@Value("${locale.path:src/main/resources/locale}")
	private String localePath;
	
	@Value("${locale.base-name:language}")
	private String localeBaseName;
	
	@Value("${locale.max-depth:10}")
	private int maxDepth;
	
	@Value("${locale.expression-placeholder.open:@}")
	private String expressionPlaceholderOpen;
	
	@Value("${locale.expression-placeholder.close:@}")
	private String expressionPlaceholderClose;
	
	@Value("${locale.parameter.open}")
	private String paramOpen;
	
	@Value("${locale.parameter.close}")
	private String paramClose;
	
	private final GuildService guildService;
	
	public LocalizedExpressionService(@NonNull final GuildService guildService) {
		this.guildService = guildService;
	}
	
	public String getLocalizedExpression(String id, @Nullable Guild guild) {
		return getLocalizedExpression(id, guild, Arguments.empty());
	}
	
	public String getLocalizedExpression(String id, @Nullable Guild guild, Arguments args) {
		return getLocalizedExpression(id, guild, args, 0);
	}
	
	public String getLocalizedExpression(String id, @Nullable Guild guild, Arguments args, int depth) {
		if(guild == null)
			return getDefaultExpression(id, args);
		
		DiscordLocale locale = DiscordLocale.ENGLISH_US;
		Optional<GuildData> data = guildService.find(guild); 
		if(data.isPresent()) {
			locale = data.get().getSettings().getLocale();
		}
		
		if(locale.equals(DiscordLocale.ENGLISH_US))
			return getDefaultExpression(id, args);
		
		File file = new File(localePath);
		URL[] urls;
		try {
			urls = new URL[]{file.toURI().toURL()};
		} catch (MalformedURLException e) {
			return expressionPlaceholderOpen + id + expressionPlaceholderClose + " - " + e.getLocalizedMessage();	//TODO Locale Errorhandling?
		}
		ClassLoader loader = new URLClassLoader(urls);
		ResourceBundle bundle = ResourceBundle.getBundle(localeBaseName, locale.toLocale(), loader);
		
		if(!bundle.containsKey(id))
			return getDefaultExpression(id, args);
		
		return finalizeExpression(id, bundle.getString(id), guild, args, depth);
	}
	
	public String getDefaultExpression(String id) {
		return getDefaultExpression(id, Arguments.empty());
	}
	
	public String getDefaultExpression(String id, Arguments args) {
		return getDefaultExpression(id, args, 0);
	}
	
	public String getDefaultExpression(String id, Arguments args, int depth) {
		File file = new File(localePath);
		URL[] urls;
		try {
			urls = new URL[]{file.toURI().toURL()};
		} catch (MalformedURLException e) {
			return expressionPlaceholderOpen + id + expressionPlaceholderClose + " - " + e.getLocalizedMessage();	//TODO Locale Errorhandling?
		}
		ClassLoader loader = new URLClassLoader(urls);
		ResourceBundle bundle = ResourceBundle.getBundle(localeBaseName, Locale.ROOT, loader);
			
		if(!bundle.containsKey(id))
			return expressionPlaceholderOpen + id + expressionPlaceholderClose;	//TODO Locale Errorhandling?
		
		return finalizeExpression(id, bundle.getString(id), null, args, depth);
	}
	
	private String finalizeExpression(String id, String content, Guild guild, Arguments args, int depth) {
		StringBuilder expression = new StringBuilder(TextUtil.getMessagePrefixEmoji(id.split("\\.")[0]));
		replacePlaceholders(expression, content, guild, args, depth);	
		return expression.toString();
	}
	
	public String replacePlaceholders(final String input, @Nullable final Guild guild) {
		return replacePlaceholders(input, guild, null);
	}
	
	public String replacePlaceholders(final String input, @Nullable final Guild guild, @Nullable Arguments args) {
		if(args == null)
			args = Arguments.empty();
		
		if(input == null)
			return "";
		
		StringBuilder result = new StringBuilder();
		return replacePlaceholders(result, input, guild, args, 0).toString();
	}
	
	private StringBuilder replacePlaceholders(@NonNull final StringBuilder result, @NonNull final String input, final Guild guild, @NonNull final Arguments args, int depth) {
		String parametersResolvedInput = resolveParameters(input, args);
		String fullyResolvedInput = resolveRecursiveExpressions(parametersResolvedInput, guild, args, depth);
        return result.append(fullyResolvedInput);
	}
	
	private String resolveParameters(@NonNull final String input, @NonNull final Arguments args) {
		int startIndex = 0;
		StringBuilder result = new StringBuilder();
		while (startIndex < input.length()) {
            int openBraceIndex = input.indexOf(paramOpen, startIndex);
            if (openBraceIndex == -1) {
                result.append(input.substring(startIndex));
                break;
            }

            int closeBraceIndex = input.indexOf(paramClose, openBraceIndex + paramOpen.length());
            if (closeBraceIndex == -1) {
                result.append(input.substring(startIndex));
                break;
            }

            result.append(input, startIndex, openBraceIndex);

            String placeholderName = input.substring(openBraceIndex + paramOpen.length(), closeBraceIndex);
            String replacement = args.getOrDefault(placeholderName, paramOpen + placeholderName + paramClose).asString();

            result.append(replacement);
            startIndex = closeBraceIndex + paramClose.length();
        }
		return result.toString();
	}
	
	private String resolveRecursiveExpressions(@NonNull final String input, @Nullable final Guild guild, @NonNull final Arguments args, int depth) {
		int startIndex = 0;
		StringBuilder result = new StringBuilder();
		if(depth < this.maxDepth)
			while(startIndex < input.length()) {
				int openBraceIndex = input.indexOf(expressionPlaceholderOpen, startIndex);
				if (openBraceIndex == -1) {
					result.append(input.substring(startIndex));
	                break;
	            }
				
				int closeBraceIndex = input.indexOf(expressionPlaceholderClose, openBraceIndex + expressionPlaceholderClose.length());
	            if (closeBraceIndex == -1) {
	            	result.append(input.substring(startIndex));
	                break;
	            }
	            
	            result.append(input, startIndex, openBraceIndex);
	            
	            String placeholderName = input.substring(openBraceIndex + expressionPlaceholderOpen.length(), closeBraceIndex);
	            String replacement = getLocalizedExpression(placeholderName, guild, args, depth + 1);
	
	            result.append(replacement);
	            startIndex = closeBraceIndex + expressionPlaceholderClose.length();
			}
		
		return result.toString();
	}
}
