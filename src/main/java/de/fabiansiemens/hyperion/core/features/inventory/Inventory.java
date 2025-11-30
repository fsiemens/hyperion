package de.fabiansiemens.hyperion.core.features.inventory;

import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryEntity;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryFolderEntity;
import de.fabiansiemens.hyperion.persistence.user.UserDataEntity;
import io.micrometer.common.lang.NonNull;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Inventory {

	private InventoryEntity entity;

	public Long getId() {
		return this.entity.getId();
	}
	
	public void setId(@NonNull Long id) {
		this.entity.setId(id);
	}
	
	public UserData getUserData() {
		return new UserData(this.entity.getUserData());
	}
	
	public void setUserData(@NonNull UserDataEntity userData) {
		this.entity.setUserData(userData);
	}
	
	@Nullable
	public InventoryFolder getRootFolder() {
		return new InventoryFolder(this.entity.getRootFolder());
	}
	
	public void setRootFolder(@NonNull InventoryFolderEntity rootFolder) {
		this.entity.setRootFolder(rootFolder);
	}
}
