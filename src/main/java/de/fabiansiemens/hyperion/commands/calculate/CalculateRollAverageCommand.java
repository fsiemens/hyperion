package de.fabiansiemens.hyperion.commands.calculate;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.roll.RollService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "calculate/RollAverage.json", parent = CalculateCommand.class, group = "")
public class CalculateRollAverageCommand extends SlashCommandBase {

	private final RollService rollServ;
	
	public <T> CalculateRollAverageCommand(ApplicationContext context, RollService rollServ)
			throws IOException, IllegalArgumentException {
		super(CalculateRollAverageCommand.class, context);
		this.rollServ = rollServ;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		final String roll = event.getOption("roll").getAsString();
		try {
			double average = rollServ.calculateAverage(roll);
			Arguments args = Arguments.of("rollString", roll)
				.put("average", String.valueOf(average));
			
			event.reply(les.getLocalizedExpression("success.calculate.average", event.getGuild(), args)).setEphemeral(true).queue();
		} catch (RollException e) {
			event.reply(les.getLocalizedExpression(e.getLocalizedMessage(), event.getGuild())).setEphemeral(true).queue();
		}
	}

}
