package de.fabiansiemens.hyperion.commands.calculate;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Subcommand;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.RollException;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.calculate.CalculateService;
import de.fabiansiemens.hyperion.core.util.Arguments;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
@Subcommand(dataFile = "calculate/Health.json", parent = CalculateCommand.class, group = "")
public class CalculateHealthCommand extends SlashCommandBase {
	
	private final CalculateService calcServ;

	public <T> CalculateHealthCommand(ApplicationContext context, CalculateService calcServ)
			throws IOException, IllegalArgumentException {
		super(CalculateHealthCommand.class, context);
		this.calcServ = calcServ;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		final String charClass = event.getOption("class").getAsString();
		final int charLvl = event.getOption("level").getAsInt();
		final int conMod = event.getOption("constitution").getAsInt();
		final boolean isTough = event.getOption("tough") != null ? event.getOption("tough").getAsBoolean() : false;
		final boolean isHillDwarf = event.getOption("hilldwarf") != null ? event.getOption("hilldwarf").getAsBoolean() : false;
		
		int hitDie = 0;
		switch(charClass) {
		case "barbarian": hitDie = 12; break;
		case "fighter":
		case "ranger":
		case "paladin": hitDie = 10; break;
		case "sorcerer":
		case "wizard": hitDie = 6; break;
		default: hitDie = 8; break;
		}
		
		try {
			int health = calcServ.calculateHealth(charLvl, hitDie, conMod, isTough, isHillDwarf);
			Arguments args = Arguments.of("class", charClass)
					.put("hitDie", String.valueOf(hitDie))
					.put("level", String.valueOf(charLvl))
					.put("con", String.valueOf(conMod))
					.put("health", String.valueOf(health));
			event.reply(les.getLocalizedExpression("success.calculate.health", event.getGuild(), args)).setEphemeral(true).queue();
		} catch (RollException e) {
			event.reply(les.getLocalizedExpression(e.getLocalizedMessage(), event.getGuild())).setEphemeral(true).queue();
		}
	}

}
