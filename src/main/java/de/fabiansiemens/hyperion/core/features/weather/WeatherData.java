package de.fabiansiemens.hyperion.core.features.weather;

import de.fabiansiemens.hyperion.core.features.weather.components.Biome;
import de.fabiansiemens.hyperion.core.features.weather.components.Climate;
import de.fabiansiemens.hyperion.core.features.weather.components.CloudCover;
import de.fabiansiemens.hyperion.core.features.weather.components.Hemisphere;
import de.fabiansiemens.hyperion.core.features.weather.components.MoonPhase;
import de.fabiansiemens.hyperion.core.features.weather.components.Pressure;
import de.fabiansiemens.hyperion.core.features.weather.components.Season;
import de.fabiansiemens.hyperion.core.features.weather.components.Tide;
import de.fabiansiemens.hyperion.core.features.weather.components.WindData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {

	//PRIMARY FACTORS:
	//Season, Climate Zone, Hemisphere, Biome, 
	
	private Season season;
	
	private Climate climate;
	
	private Hemisphere hemisphere;
	
	private Biome biome;
	
	private MoonPhase moonPhase;
	
	//SECONDARY FACTORS:
	//--> These depend on the PRIMARY Factors
	// Sunlight, Pressure Areas, Humidity
	
	private Double sunlight;
	
	private Pressure airPressure;
	
	private Double humidity;
	
	private Tide tide;
	
	//TERTIARY FACTORS:
	//--> These depend on the SECONDARY Factors
	// Wind, Temperature, Cover, Precipitation
	
	private WindData wind;
	
	private Double temperature;
	
	private CloudCover cover;
	
	private Double precipitation;
}
