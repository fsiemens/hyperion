package de.fabiansiemens.hyperion.persistence.guild.settings;

import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import de.fabiansiemens.hyperion.persistence.guild.GuildDataEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class GuildSettingsEntity {

	public GuildSettingsEntity(
			String locale,
			boolean sendCritMessage,
			boolean sendCritImage,
			boolean allowCritCustomImage,
			boolean allowCritCustomMessage,
			Long rollChannelId,
			Long musicChannelId,
			Long commandChannelId,
			String guildDefaultCritSuccessMessage,
			String guildDefaultCritFailMessage
			) {
		
	}
	
	@Id
	@Column(name = "SETTINGS_ID")
	private Long guildId;

	@OneToOne
	@PrimaryKeyJoinColumn(name = "SETTINGS_ID", referencedColumnName = "GUILD_ID")
	private GuildDataEntity guildData;
	
	private String locale = DiscordLocale.GERMAN.getLocale();
	
	private boolean sendCritMessage;
	
	private boolean sendCritImage;
	
	private boolean allowCritCustomImage;
	
	private boolean allowCritCustomMessage;
	
	private Long rollChannelId;
	
	private Long musicChannelId;
	
	private Long commandChannelId;
	
	private String guildDefaultCritSuccessMessage;
	
	private String guildDefaultCritFailMessage;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private FileEntity guildDefaultCritSuccessImage;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private FileEntity guildDefaultCritFailImage;
}
