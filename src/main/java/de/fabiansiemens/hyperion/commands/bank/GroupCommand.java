package de.fabiansiemens.hyperion.commands.bank;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.interactions.bank.CommonAccountInteractionChain;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "bank/Group.json")
public class GroupCommand extends SlashCommandBase {
	
	public GroupCommand(ApplicationContext context
						) throws IOException, IllegalArgumentException {
		super(GroupCommand.class, context);
	}
	
	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		CommonAccountInteractionChain interactionChain = context.getBean(CommonAccountInteractionChain.class);
		try {
			interactionChain.onGroupCommandInteraction(event);
		} catch (UiParseException e) {
			// TODO Fix Exception handling
			e.printStackTrace();
		}
	}

}
