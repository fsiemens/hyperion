package de.fabiansiemens.hyperion.core.features.weather;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.features.weather.components.Biome;
import de.fabiansiemens.hyperion.core.features.weather.components.Climate;
import de.fabiansiemens.hyperion.core.features.weather.components.Season;
import de.fabiansiemens.hyperion.core.util.RandomService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

@Service
@AllArgsConstructor
public class WeatherService {
	
	private RandomService randomService;
	
	public WeatherData generateRandomWeather() {
		return generateRandomWeather(randomService.oneOutOf(Season.values()));
	}
	
	public WeatherData generateRandomWeather(Season season) {
		return generateRandomWeather(season, randomService.oneOutOf(Climate.values()));
	}
	
	public WeatherData generateRandomWeather(Climate climate) {
		return generateRandomWeather(climate, randomService.oneOutOf(Biome.values()));
	}
	
	public WeatherData generateRandomWeather(Biome biome) {
		return generateRandomWeather(randomService.oneOutOf(Season.values()), biome);
	}
	
	public WeatherData generateRandomWeather(Season season, Climate climate) {
		return generateRandomWeather(season, climate, randomService.oneOutOf(Biome.values()));
	}
	
	public WeatherData generateRandomWeather(Season season, Biome biome) {
		return generateRandomWeather(season, randomService.oneOutOf(Climate.values()), biome);
	}
	
	public WeatherData generateRandomWeather(Climate climate, Biome biome) {
		return generateRandomWeather(randomService.oneOutOf(Season.values()), climate, biome);
	}
	
	public WeatherData generateRandomWeather(Season season, Climate climate, Biome biome) {
		return new WeatherData();	//TODO
	}

	@Deprecated
	public MessageEmbed createRandomWeatherEmbed() {
		int IndicatorF = 0;
		int IndicatorN = 0;
		int IndicatorW = 0;
		int IndicatorTS = 0;
		int IndicatorAS = 0;

		boolean sonnig = false;
		boolean wolkig = false;

		// Temperatur
		// Windgeschwindigkeit
		// Niederschlag
		// Windrichtung
		// Special Conditions
			// 1 = Flaute (Low Winds + kein Regen + 25 % chance)
			// 2 = Nebel (Low Winds + Colder than Normal + 20 % chance)
			// 3 = Extremer Wind (Strong Winds + 50 % chance
			// 4 = Tropischer Sturm (Strong Winds + Heavy rain + warmer than normal + 25 % chance)
			// 5 = Arktischer Sturm (Strong Winds + Heavy rain + normal temp or colder than normal + 25 % chance)


			int temp = randomTemperature();
			int windVel = randomWindSpeed();
			int ns = randomPercipitation();
			int windDir = randomWindDirection();

			if((temp != 0) && (windVel != 0) && (ns != 0) && (windDir != 0)) {
				
			}
				EmbedBuilder eb = new EmbedBuilder();

				eb.setTitle("Random Weather");
				eb.setDescription("Nennt eine zufällige Wetterkonstellation auf dem Meer");
				eb.setColor(Color.cyan);

				switch(temp) {
				case 1: eb.addField("Temperatur 🌡:", "**Normale Temperatur** für Jahreszeit und Breitengrad", true);
				break;
				case 2:
					int z = (1+ randomService.outOf(5)) * 2;
					eb.addField("Temperatur 🌡:", z + "°C **kühler** als für Jahreszeit und Umgebung üblich", true);
				break;
				case 3:
					int x = (1+ randomService.outOf(5)) * 2;
					eb.addField("Temperatur 🌡:", x + "°C **wärmer** als für Jahreszeit und Umgebung üblich", true);
				break;
				}

				switch(windVel) {
				case 1: eb.addField("Windstärke 🌬:", "**Leichter Wind**, ideal zum Segeln", true);
				break;
				case 2: eb.addField("Windstärke 🌬:", "**Schwacher Wind**, langsameres Segeln", true);
				break;
				case 3: int waves = 1 + randomService.outOf(4); eb.addField("Windstärke 🌬:", "**Starker Wind**, 20 % schnelleres Segeln. "
						+ "Bei normaler Wetterlage ca. " + waves + " Meter höhere Wellen", true);
				break;
				}

				switch(ns) {
				case 1: if(randomService.outOf(2) == 1) {sonnig = true; eb.addField("Niederschlag 🌥", "**Kein Niederschlag**, sonnig 🌤", true);}
						else{ wolkig = true; eb.addField("Niederschlag 🌥", "**Kein Niederschlag**, wolkig ☁", true);} break;
				case 2: eb.addField("Niederschlag 🌥", "**Leichter Regen**", true); break;
				case 3: eb.addField("Niederschlag 🌥", "**Starker Regen**", true); break;
				}
				switch(windDir) {
				case 1: eb.setThumbnail("https://s12.directupload.net/images/200911/riedb798.png"); break;
				case 2: eb.setThumbnail("https://s12.directupload.net/images/200911/syn9pny8.png"); break;
				case 3: eb.setThumbnail("https://s12.directupload.net/images/200911/95j5626g.png"); break;
				case 4: eb.setThumbnail("https://s12.directupload.net/images/200911/dmxp9jrp.png"); break;
				case 5: eb.setThumbnail("https://s12.directupload.net/images/200911/zifuxkkd.png"); break;
				case 6: eb.setThumbnail("https://s12.directupload.net/images/200911/zm3ayob2.png"); break;
				case 7: eb.setThumbnail("https://s12.directupload.net/images/200911/t4qjcbw3.png"); break;
				case 8: eb.setThumbnail("https://s12.directupload.net/images/200911/grnumqhz.png"); break;
				}

				System.out.println("Temperatur: " + temp + "; \nWindgeschwindigkeit: " + windVel +
						"\nWindrichtung: " + windDir + "\nNiederschlag: " + ns);

				// 1 = Flaute (Low Winds + kein Regen + 25 % chance)
				int counter = 0;
				if(windVel == 2 && ns == 1) {
					System.out.println("Flaute: Bedingungen erfüllt");
					if(randomService.isTrue(50)) {
						++counter;
						IndicatorF = counter;

					}
				}
				// 2 = Nebel (Low Winds + Colder than Normal + 20 % chance)
				if(windVel == 2 && temp == 2) {
					System.out.println("Nebel: Bedingungen erfüllt");
					if(randomService.isTrue(50)) {
						++counter;
						IndicatorN = counter;

					}
				}
				// 3 = Extremer Wind (Strong Winds + 50 % chance)
				if(windVel == 3) {
					System.out.println("Extreme Winds: Bedingungen erfüllt");
					if(randomService.isTrue(50)) {
						++counter;
						IndicatorW = counter;

					}
				}
				// 4 = Tropischer Sturm (Strong Winds + Heavy rain + warmer than normal + 25 % chance)
				if(windVel == 3 && ns == 3 && temp == 3) {
					System.out.println("Tropensturm: Bedingungen erfüllt");
					if(randomService.isTrue(50)) {
						++counter;
						IndicatorTS = counter;

					}
				}
				// 5 = Arktischer Sturm (Strong Winds + Heavy rain + normal temp or colder than normal + 25 % chance)
				if(windVel == 3 && ns == 3 && temp < 3) {
					System.out.println("Arktischer Sturm: Bedingungen erfüllt");
					if(randomService.isTrue(50)) {
						++counter;
						IndicatorAS = counter;

					}
				}

				System.out.println("Flaute: " + IndicatorF + "\nNebel: " + IndicatorN + "\nExtreme Winds: " + IndicatorW +
						"\nTropensturm: " + IndicatorTS + "\nArktischer Sturm: " + IndicatorAS);

			// Wenn mindestens ein besonderes Wetterereignis vorliegt
				if(counter > 0) {
					int r = 1 + randomService.outOf(counter);
					System.out.println("Random Picked Indicator: " + r);
					if(r == IndicatorF) {
						//Flaute
						int time = 3 + randomService.outOf(3);
						eb.addField("Besondere Wetterlage: **Flaute**", "Bei Trockenheit und schwachem Wind entsteht auf dem Meer eine Windstille. "
								+ "Schiffe sind in der Zeit auf natürlichem Wege " + time + " Tage **Bewegungsunfähig**", false);
						eb.setImage("https://www.muralswallpaper.com/app/uploads/turquoise-sea-plain.jpg");
					}
					else if(r == IndicatorN) {
						//Nebel
						int sicht = 0;
						switch(randomService.outOf(5)) {
						case 0: sicht = 50; break;
						case 1: sicht = 100; break;
						case 2: sicht = 150; break;
						case 3: sicht = 250; break;
						case 4: sicht = 500; break;

						}
						eb.addField("Besondere Wetterlage: **Nebel**", "Bei leichtem Regen und schwachem Wind entsteht auf dem Meer ein Nebelfeld. "
								+ "Die Sichtweite fällt in diesem Fall auf unter " + sicht + " Meter. Gleichzeitig kann das Schiff nur noch 50 % max. Geschwindigkeit fahren", false);
						eb.setImage("https://s12.directupload.net/images/200911/geevk86d.png");
					}
					else if(r == IndicatorW) {
						int amp = 2 + randomService.outOf(4);

						eb.addField("Besondere Wetterlage: **Extreme Winde**", "Es tritt ein so extremer Wind auf, dass Schiffe gegen den Wind 50% langsamer und mit dem Wind 50% schneller fahren."
								+ " Zeitgleich entstehen Wellen mit einer Amplitude bis " + amp + " Metern höher als normal.", false);
						eb.setImage("https://www.sciencemag.org/sites/default/files/styles/article_main_large/public/wave_16x9.jpg?itok=i2LBLkZ3");
					}
					else if(r == IndicatorTS) {
						int min = 80 + randomService.outOf(20);
						int max = 100 + randomService.outOf(20);
						int amp = 3 + randomService.outOf(6);

						eb.addField("Besondere Wetterlage: **Tropensturm**", "Es tritt ein tropischer Gewittersturm auf. Windgeschwindigkeiten zwischen " + min + " und " + max + " km/h sind zu erwarten."
								+ " Zusätzlich besteht die Gefahr auf Blitzschlag. Wellen steigen auf eine Höhe von " + amp, false);
						eb.setImage("https://s12.directupload.net/images/200912/mvlmijbf.jpg");
					}
					else if(r == IndicatorAS) {
						int min = 70 + randomService.outOf(20);
						int max = 90 + randomService.outOf(20);
						int amp = 3 + randomService.outOf(6);
						eb.addField("Besondere Wetterlage: **Arktischer Sturm**", "Es tritt ein arktischer Sturm auf. Windgeschwindigkeiten zwischen " + min + " und " + max + " km/h sind zu erwarten."
								+ "Zusätzlich besteht die Gefahr spontan in einen Blizzard zu geraten. Wellen steigen auf eine Höhe von " + amp, false);
						eb.setImage("https://s12.directupload.net/images/200911/an6ymwfo.jpg");
					}

				}
			//  Wenn kein besonderes Wetterereignis vorliegt:
				else {
					if(sonnig || wolkig) {
						if(sonnig) {
							//Sonnig
							eb.addField("Besondere Wetterlage: **Keine**", "", false);
							eb.setImage("https://cdn.pixabay.com/photo/2020/05/28/17/51/ocean-5232052_960_720.jpg");
						}
						else {
							//wolkig
							eb.addField("Besondere Wetterlage: **Keine**", "", false);
							eb.setImage("https://img.fotocommunity.com/wolken-meer-2cadd0f0-9c6a-4a06-930f-36293abcb99c.jpg?width=1000");
						}
					}
					else if(ns == 2 || ns == 3) {
						// regen
						if(ns == 2) {
							//leichter Regen
							eb.addField("Besondere Wetterlage: **Keine**", "", false);
							eb.setImage("https://unwetteralarm.com/wp-content/uploads/2019/04/water-815271__340.jpg");
						}
						else {
							//starker Regen
							eb.addField("Besondere Wetterlage: **Keine**", "", false);
							eb.setImage("https://s12.directupload.net/images/200911/9bsaw7bp.jpg");
						}
					}

				}
				return eb.build();
	}

	private int randomTemperature() {
		Map<Integer, Double> temperatures = new HashMap<>();
		temperatures.put(1, 50.0);
		temperatures.put(2, 25.0);
		temperatures.put(3, 25.0);
		
		return randomService.oneOutOfWeighted(temperatures);
	}

	private int randomWindSpeed() {
		Map<Integer, Double> windspeed = new HashMap<>();
		windspeed.put(1, 50.0);
		windspeed.put(2, 25.0);
		windspeed.put(3, 25.0);
		
		return randomService.oneOutOfWeighted(windspeed);
	}

	private int randomPercipitation() {
		Map<Integer, Double> percipitation = new HashMap<>();
		percipitation.put(1, 50.0);
		percipitation.put(2, 25.0);
		percipitation.put(3, 25.0);
		
		return randomService.oneOutOfWeighted(percipitation);
	}

	private int randomWindDirection() {
		return randomService.outOf(8);
	}
	
}
