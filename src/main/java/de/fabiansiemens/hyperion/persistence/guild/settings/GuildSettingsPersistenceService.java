package de.fabiansiemens.hyperion.persistence.guild.settings;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;

@Service
public class GuildSettingsPersistenceService extends CrudPersistenceService<GuildSettingsEntity, Long, GuildSettingsRepository> {
	
	public GuildSettingsPersistenceService(GuildSettingsRepository repository) {
		super(repository);
	}

	public GuildSettingsEntity createDefault() {
		return new GuildSettingsEntity();
	}
}
