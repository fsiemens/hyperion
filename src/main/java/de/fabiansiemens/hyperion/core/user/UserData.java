package de.fabiansiemens.hyperion.core.user;

import de.fabiansiemens.hyperion.core.features.inventory.Inventory;
import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.persistence.user.UserDataEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserData {

	private UserDataEntity entity;
	
	public Long getId() {
		return entity.getId();
	}
	
	public Inventory getInventory() {
		return new Inventory(entity.getInventory());
	}
	
	public void setInventory(Inventory inventory) {
		this.entity.setInventory(inventory.getEntity());
	}
	
	public UserSettings getSettings() {
		return new UserSettings(entity.getSettings());
	}
	
	public void setSettings(UserSettings settings) {
		this.entity.setSettings(settings.getEntity());
	}
}
