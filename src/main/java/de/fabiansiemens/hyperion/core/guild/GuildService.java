package de.fabiansiemens.hyperion.core.guild;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.guild.settings.GuildSettings;
import de.fabiansiemens.hyperion.core.guild.settings.GuildSettingsService;
import de.fabiansiemens.hyperion.persistence.guild.GuildDataEntity;
import de.fabiansiemens.hyperion.persistence.guild.GuildDataPersistenceService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;

@Service
@AllArgsConstructor
public class GuildService {

	private final GuildSettingsService guildSettingsService;
	private final GuildDataPersistenceService persistenceService;
	
	public GuildData create(Guild guild) {
		return create(guild, guildSettingsService.getDefault());
	}
	
	public GuildData create(Guild guild, GuildSettings settings) {
		return new GuildData(persistenceService.create(guild.getIdLong(), settings.getEntity()));
	}
	
	public long count() {
		return persistenceService.count();
	}
	
	public boolean exists(Guild guild) {
		return persistenceService.exists(guild.getIdLong());
	}

	public Optional<GuildData> find(Guild guild) {
		Optional<GuildDataEntity> data = persistenceService.findById(guild.getIdLong());
		if(data.isPresent())
			return Optional.of(new GuildData(data.get()));
		return Optional.empty();
	}
	
	public List<GuildData> findAll(){
		return persistenceService.findAll()
				.stream()
				.map(GuildData::new)
				.collect(Collectors.toList());
	}
	
	public GuildData update(GuildData data) {
		return new GuildData(persistenceService.update(data.getEntity()));
	}
	
	public void delete(GuildData data) {
		persistenceService.delete(data.getEntity());
	}
}
