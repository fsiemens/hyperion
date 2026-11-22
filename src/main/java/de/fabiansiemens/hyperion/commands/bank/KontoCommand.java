package de.fabiansiemens.hyperion.commands.bank;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.interactions.bank.AccountSelectInteractionChain;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "bank/Konto.json")
public class KontoCommand extends SlashCommandBase {

	private AccountSelectInteractionChain accountInteractionChain;
	
	public KontoCommand(
			ApplicationContext context,
			AccountSelectInteractionChain accountInteractionChain
			) throws IOException, IllegalArgumentException {
		super(KontoCommand.class, context);
		this.accountInteractionChain = accountInteractionChain;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		try {
			accountInteractionChain.onKontoCommandInteraction(event);
		} catch (UiParseException e) {
			// TODO Fix Exception handling
			e.printStackTrace();
		}
	}
}
