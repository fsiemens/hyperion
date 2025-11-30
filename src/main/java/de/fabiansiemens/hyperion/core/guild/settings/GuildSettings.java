package de.fabiansiemens.hyperion.core.guild.settings;

import de.fabiansiemens.hyperion.core.guild.GuildData;
import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import de.fabiansiemens.hyperion.persistence.guild.settings.GuildSettingsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.interactions.DiscordLocale;

@Data
@AllArgsConstructor
public class GuildSettings {

	private final GuildSettingsEntity entity;

	public Long getGuildId() {
		return entity.getGuildId();
	}
	
	public GuildData getGuildData() {
		return new GuildData(entity.getGuildData());
	}
	
	public DiscordLocale getLocale() {
		return DiscordLocale.from(entity.getLocale());
	}
	
	public boolean isSendCritMessage() {
		return entity.isSendCritMessage();
	}
	
	public boolean isSendCritImage() {
		return entity.isSendCritImage();
	}
	
	public boolean isAllowCritCustomImage() {
		return entity.isAllowCritCustomImage();
	}
	
	public boolean isAllowCritCustomMessage() {
		return entity.isAllowCritCustomMessage();
	}
	
	public Long getRollChannelId() {
		return entity.getRollChannelId();
	}
	
	public Long getMusicChannelId() {
		return entity.getMusicChannelId();
	}
	
	public Long getCommandChannelId() {
		return entity.getCommandChannelId();
	}
	
	public String getGuildDefaultCritSuccessMessage() {
		return entity.getGuildDefaultCritSuccessMessage();
	}
	
	public String getGuildDefaultCritFailMessage() {
		return entity.getGuildDefaultCritFailMessage();
	}
	
	public FileEntity getGuildDefaultCritSuccessImage() {
		return entity.getGuildDefaultCritSuccessImage();
	}
	
	public FileEntity getGuildDefaultCritFailImage() {
		return entity.getGuildDefaultCritFailImage();
	}
	
	public void setGuildId(Long guildId) {
		this.entity.setGuildId(guildId);
	}
	
	public void setGuildData(GuildData guildData) {
		this.entity.setGuildData(guildData.getEntity());
	}
	
	public void setLocale(DiscordLocale locale) {
		this.entity.setLocale(locale.getLocale());
	}
	
	public void setSendCritMessage(boolean sendCritMessage) {
		this.entity.setSendCritMessage(sendCritMessage);
	}
	
	public void setSendCritImage(boolean sendCritImage) {
		this.entity.setSendCritImage(sendCritImage);
	}
	
	public void setAllowCritCustomMessage(boolean AllowCritCustomMessage) {
		this.entity.setAllowCritCustomMessage(AllowCritCustomMessage);
	}
	
	public void setAllowCritCustomImage(boolean AllowCritCustomImage) {
		this.entity.setAllowCritCustomImage(AllowCritCustomImage);
	}
	
	public void setRollChannelId(Long RollChannelId) {
		this.entity.setRollChannelId(RollChannelId);
	}
	
	public void setMusicChannelId(Long MusicChannelId) {
		this.entity.setMusicChannelId(MusicChannelId);
	}
	
	public void setCommandChannelId(Long CommandChannelId) {
		this.entity.setCommandChannelId(CommandChannelId);
	}
	
	public void setGuildDefaultCritSuccessMessage(String guildDefaultCritSuccessMessage) {
		this.entity.setGuildDefaultCritSuccessMessage(guildDefaultCritSuccessMessage);
	}
	
	public void setGuildDefaultCritFailMessage(String guildDefaultCritFailMessage) {
		this.entity.setGuildDefaultCritFailMessage(guildDefaultCritFailMessage);
	}

	public void setGuildDefaultCritSuccessImage(FileEntity image) {
		this.entity.setGuildDefaultCritSuccessImage(image);
	}
	
	public void setGuildDefaultCritFailImage(FileEntity image) {
		this.entity.setGuildDefaultCritFailImage(image);
	}
}
