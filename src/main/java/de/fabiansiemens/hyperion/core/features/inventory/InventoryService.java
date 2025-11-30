package de.fabiansiemens.hyperion.core.features.inventory;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.user.UserData;
import de.fabiansiemens.hyperion.core.user.UserService;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryPersistenceService;
import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.User;

@Service
@AllArgsConstructor
public class InventoryService {

	private InventoryPersistenceService persistenceService;
	private UserService userService;
	
	public Inventory createInventory(UserData userData) {
		return new Inventory(persistenceService.createEmpty(userData.getEntity()));
	}
	
	public Inventory find(@NonNull User user) {
		Optional<UserData> userData = this.userService.findByUser(user);
	
		if(userData.isPresent()) {
			if(userData.get().getInventory() != null)
				return userData.get().getInventory();
			
			userData.get().setInventory(createInventory(userData.get()));
			UserData persistedUserData = userService.update(userData.get());
			return persistedUserData.getInventory();
		}
		
		UserData persistedUserData = userService.create(user);
		return persistedUserData.getInventory();
	}
	
	public Inventory update(@NonNull Inventory inventory) {
		return new Inventory(persistenceService.update(inventory.getEntity()));
	}
}
