package de.fabiansiemens.hyperion.commands.calculate;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.calculate.CalculateService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "calculate/MTL.json", parent = CalculateCommand.class, group = "")
public class CalculateMTLCommand extends SlashCommandBase {
	
	private final CalculateService calcServ;

	public <T> CalculateMTLCommand(ApplicationContext context, CalculateService calcServ)
			throws IOException, IllegalArgumentException {
		super(CalculateMTLCommand.class, context);
		this.calcServ = calcServ;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		final int charlvl = event.getOption("level").getAsInt();
		final int mtl = calcServ.calculateMTL(charlvl);
		Arguments args = Arguments.of("mtl", String.valueOf(mtl))
				.put("charLvl", String.valueOf(charlvl));
		event.reply(les.getLocalizedExpression("success.calculate.mtl", event.getGuild(), args)).setEphemeral(true).queue();
	}
}
