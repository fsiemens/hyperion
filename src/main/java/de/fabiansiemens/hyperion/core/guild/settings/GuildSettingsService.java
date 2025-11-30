package de.fabiansiemens.hyperion.core.guild.settings;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.guild.settings.GuildSettingsPersistenceService;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class GuildSettingsService {

	private final GuildSettingsPersistenceService persistenceService;

	public GuildSettings getDefault() {
		return new GuildSettings(persistenceService.createDefault());
	}
}
