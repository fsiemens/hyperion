package de.fabiansiemens.hyperion.commands.games;

import java.io.IOException;

import org.springframework.context.ApplicationContext;

import de.fabiansiemens.hyperion.core.annotations.Command;
import de.fabiansiemens.hyperion.core.commands.slash.SlashCommandBase;
import de.fabiansiemens.hyperion.core.exceptions.UiParseException;
import de.fabiansiemens.hyperion.core.features.weather.WeatherService;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Command(dataFile = "games/Weather.json")
public class WeatherCommand extends SlashCommandBase {

	private final WeatherService weatherService;
	
	public <T> WeatherCommand(ApplicationContext context, WeatherService weatherService)
			throws IOException, IllegalArgumentException {
		super(WeatherCommand.class, context);
		this.weatherService = weatherService;
	}

	@Override
	public void performSlashCommand(SlashCommandInteractionEvent event) throws UiParseException {
		event.replyEmbeds(weatherService.createRandomWeatherEmbed()).queue();
	}

}
