package de.fabiansiemens.hyperion.core.guild;

import de.fabiansiemens.hyperion.core.guild.settings.GuildSettings;
import de.fabiansiemens.hyperion.persistence.guild.GuildDataEntity;
import lombok.Data;

@Data
public class GuildData {

	private final GuildDataEntity entity;
	
	public Long getId() {
		return entity.getId();
	}
	
	public void setId(Long id) {
		this.entity.setId(id);
	}

	public GuildSettings getSettings() {
		return new GuildSettings(entity.getSettings());
	}

	public void setSettings(GuildSettings settings) {
		this.entity.setSettings(settings.getEntity());
	}
}
