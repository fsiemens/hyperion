package de.fabiansiemens.hyperion.persistence.guild;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;
import de.fabiansiemens.hyperion.persistence.guild.settings.GuildSettingsEntity;

@Service
public class GuildDataPersistenceService extends CrudPersistenceService<GuildDataEntity, Long, GuildDataRepository>{

	public GuildDataPersistenceService(GuildDataRepository repos) {
		super(repos);
	}
	
	public GuildDataEntity create(Long guildId, GuildSettingsEntity settings) {
		GuildDataEntity entity = new GuildDataEntity(guildId, settings);
		settings.setGuildId(guildId);
		settings.setGuildData(entity);
		return repository.save(entity);
	}
}
