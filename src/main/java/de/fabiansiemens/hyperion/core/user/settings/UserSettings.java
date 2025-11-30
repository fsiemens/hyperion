package de.fabiansiemens.hyperion.core.user.settings;

import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import de.fabiansiemens.hyperion.persistence.safeguard.SafeguardEntity;
import de.fabiansiemens.hyperion.persistence.user.settings.UserSettingsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserSettings {

	private UserSettingsEntity entity;
	
	public Long getId() {
		return entity.getId();
	}
	
	/**
	 * Returns a copy of the {@link UserData} of this {@link UserSettingsEntity}
	 * @return copy of {@link UserData}
	 */
	public UserData getUserData() {
		return new UserData(entity.getUserData());
	}

	public boolean isUseCritSettings() {
		return entity.isUseCritSettings();
	}
	
	public void setUseCritSettings(boolean useCritSettings) {
		this.entity.setUseCritSettings(useCritSettings);
	}
	
	public String getCritSuccessMessage() {
		return entity.getCritSuccessMessage();
	}
	
	public void setCritSuccessMessage(String message) {
		this.entity.setCritSuccessMessage(message);
	}
	
	public String getCritFailMessage() {
		return entity.getCritFailMessage();
	}
	
	public void setCritFailMessage(String message) {
		this.entity.setCritFailMessage(message);
	}
	
	public FileEntity getCritSuccessFile() {
		return entity.getCritSuccessFile();
	}
	
	public void setCritSuccessFile(FileEntity fileEntity) {
		this.entity.setCritSuccessFile(fileEntity);
	}
	
	public FileEntity getCritFailFile() {
		return entity.getCritFailFile();
	}
	
	public void setCritFailFile(FileEntity fileEntity) {
		this.entity.setCritFailFile(fileEntity);
	}
	
	public SafeguardEntity getSafeguard() {
		return entity.getSafeguard();
	}
	
	public void setSafeguard(SafeguardEntity safeguard) {
		entity.setSafeguard(safeguard);
	}
}
