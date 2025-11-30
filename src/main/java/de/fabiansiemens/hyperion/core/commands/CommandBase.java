package de.fabiansiemens.hyperion.core.commands;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.data.CommandInfo;
import de.fabiansiemens.hyperion.core.locale.LocalizedExpressionService;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Slf4j
public abstract class CommandBase implements ICommand {
	
	@Getter
	private final CommandInfo commandInfo;
	
	@Getter
	private final String dataPath;
	
	@Getter
	protected final LocalizedExpressionService les;
	
	@Getter
	protected final ApplicationContext context;
	
	public <T> CommandBase(@NonNull Class<T> clazz, @NonNull ApplicationContext context) throws IOException, IllegalArgumentException {
		this.context = context;
		this.les = context.getBean(LocalizedExpressionService.class);
		CommandImportService importService = context.getBean(CommandImportService.class);
		
		Command annotation = clazz.getAnnotation(Command.class);
		if(annotation != null) {
			this.dataPath = annotation.dataFile();
			if(dataPath.isBlank())
				throw new IllegalArgumentException("Can't construct Command " + clazz.getName() + ". JSON file path must not be blank.");
			
			commandInfo = importService.importCommand(dataPath);
			log.debug("Constructed Command {}", getName());
			return;
		}
			
		Subcommand subcommandAnnotation = clazz.getAnnotation(Subcommand.class);
		if(subcommandAnnotation != null ) {
			this.dataPath = subcommandAnnotation.dataFile();
			if(dataPath.isBlank())
				throw new IllegalArgumentException("Can't construct Command " + clazz.getName() + ". JSON file path must not be blank.");
			
			commandInfo = importService.importCommand(dataPath);
			log.debug("Constructed Subcommand {} of Supercommand {}", getName(), subcommandAnnotation.parent());
			return;
		}
		
		throw new IllegalArgumentException("Can't construct Command " + clazz.getName() + ". Add a Command-Annotation and specify a valid JSON data file.");
	}
	
	@Override
	public String getName() {
		return commandInfo.getName();
	}

	@Override
	public String getDescription(DiscordLocale locale) {
		String description = commandInfo.getData().getDescriptionLocalizations().get(locale);
		if(description == null)
			description = commandInfo.getData().getDescriptionLocalizations().get(DiscordLocale.ENGLISH_UK);
		
		if(description == null)
			description = commandInfo.getData().getDescriptionLocalizations().toMap().values().toArray(new String[0])[0];
		return description;
	}

}
