package de.fabiansiemens.hyperion.persistence.guild.settings;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuildSettingsRepository extends JpaRepository<GuildSettingsEntity, Long> {
	
}
