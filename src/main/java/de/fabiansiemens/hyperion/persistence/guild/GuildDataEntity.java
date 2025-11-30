package de.fabiansiemens.hyperion.persistence.guild;

import de.fabiansiemens.hyperion.persistence.guild.settings.GuildSettingsEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GuildDataEntity {

	@Id
	@Column(name = "GUILD_ID")
	private Long id;

	@OneToOne(mappedBy = "guildData", cascade = CascadeType.ALL)
	private GuildSettingsEntity settings;
	//Date joinDate;
	//TODO more (Mitglieder Statistiken, Aktivste Nutzer, Onlinezeiten, Anz. Chatnachrichten, etc...)
}
