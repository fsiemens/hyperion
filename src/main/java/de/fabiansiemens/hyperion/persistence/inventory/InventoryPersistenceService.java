package de.fabiansiemens.hyperion.persistence.inventory;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;
import de.fabiansiemens.hyperion.persistence.user.UserDataEntity;

@Service
public class InventoryPersistenceService extends CrudPersistenceService<InventoryEntity, Long, InventoryRepository> {

	public InventoryPersistenceService(InventoryRepository repos) {
		super(repos);
	}

	public InventoryEntity createEmpty(UserDataEntity userData) {
		return new InventoryEntity(userData);
	}

	
}
