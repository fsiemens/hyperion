package de.fabiansiemens.hyperion.core.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.resource.beans.container.internal.NoSuchBeanException;
import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.annotations.JdaListener;
import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommand;
import de.fabiansiemens.hyperion.core.commands.slash.SuperCommand;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

@Slf4j
@JdaListener
public class CommandRegistrationService {
	private final ApplicationContext context;
	@Getter
	private final Map<String, ICommand> commands;
	
	public CommandRegistrationService(ApplicationContext context) {
		this.context = context;
		this.commands = new ConcurrentHashMap<>();
	}
	
	@PostConstruct
	public void registerCommands() {
		context.getBeansWithAnnotation(Command.class).forEach((name, instance) -> {
			if(!(instance instanceof ICommand)) {
				log.warn("Bean {} does not implement ICommand and should not be annotated with @Command.");
				return;
			}
			
			ICommand command = (ICommand) instance;
			commands.put(command.getName(), command);
			log.debug("Registered Command '{}'", command.getName());
		});
		
		context.getBeansWithAnnotation(Subcommand.class).forEach((name, instance) -> {
			if(!(instance instanceof SlashCommand)) {
				log.warn("Bean {} does not implement SlashCommand and should not be annotated with @Subcommand.");
				return;
			}
			
			Class<? extends SuperCommand> parentClazz = instance.getClass().getAnnotation(Subcommand.class).parent();
			String group = instance.getClass().getAnnotation(Subcommand.class).group();
			SlashCommand command = (SlashCommand) instance;
			try {
				SuperCommand parent = context.getBean(parentClazz);
				parent.addSubcommand(group, command);
				log.debug("Registered SubCommand '{}' on parent '{}'", command.getName(), parent.getName());
			}
			catch (NoSuchBeanException e) {
				log.warn("Command {} references a SuperCommand Bean that does not exist", command.getName());
				log.warn("Caused by: ", e);
			}
		});
		log.info("Successfully registered {} Commands.", commands.size());
	}
	
	@SubscribeEvent
	public void onGuildReadyEvent(GuildReadyEvent event) {
		event.getGuild().updateCommands().addCommands(getCommandData()).complete();
	}
	
	public List<CommandData> getCommandData(){
		List<CommandData> commandData = new LinkedList<>();
		for(ICommand command : this.commands.values()) {
			if(command instanceof SlashCommand slashCommand) {
				SlashCommandData slashCmd = slashCommand.getSlashCommandData();
				if(slashCmd != null)
					commandData.add(slashCmd);
			}
			
			//TODO hier context commands etc. hinzufügen
		}
		
		return commandData;
	}
}
