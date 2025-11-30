package de.fabiansiemens.hyperion.commands.calculate;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.calculate.CalculateService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Subcommand(dataFile = "calculate/MML.json", parent = CalculateCommand.class, group = "")
public class CalculateMMLCommand extends SlashCommandBase {

	private final CalculateService calcServ;
	
	public <T> CalculateMMLCommand(ApplicationContext context, CalculateService calcServ)
			throws IOException, IllegalArgumentException {
		super(CalculateMMLCommand.class, context);
		this.calcServ = calcServ;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		final int charlvl = event.getOption("level").getAsInt();
		final int maxTechUsed = event.getOption("max_tech_used").getAsInt();
		final int mml = calcServ.calculateMML(charlvl, maxTechUsed);
		Arguments args = Arguments.of("mml", String.valueOf(mml))
				.put("maxTechUsed", String.valueOf(maxTechUsed))
				.put("charLvl", String.valueOf(charlvl));
		event.reply(les.getLocalizedExpression("success.calculate.mml", event.getGuild(), args)).setEphemeral(true).queue();
	}

}
