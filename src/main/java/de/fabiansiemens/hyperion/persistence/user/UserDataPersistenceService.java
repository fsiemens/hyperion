package de.fabiansiemens.hyperion.persistence.user;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;
import de.fabiansiemens.hyperion.persistence.inventory.InventoryEntity;
import de.fabiansiemens.hyperion.persistence.user.settings.UserSettingsEntity;
import de.fabiansiemens.hyperion.persistence.user.settings.UserSettingsPersistenceService;

@Service
public class UserDataPersistenceService extends CrudPersistenceService<UserDataEntity, Long, UserDataRepository> {

	private final UserSettingsPersistenceService settingsPersistenceService;
	
	public UserDataPersistenceService(UserDataRepository repos, UserSettingsPersistenceService settingsPersistenceService) {
		super(repos);
		this.settingsPersistenceService = settingsPersistenceService;
	}

	public UserDataEntity create(Long id) {
		return create(id, settingsPersistenceService.createDefault());
	}
	
	public UserDataEntity create(Long id, UserSettingsEntity settings) {
		InventoryEntity inventory = new InventoryEntity();
		UserDataEntity entity = new UserDataEntity(id, settings, inventory);
		settings.setUserData(entity);
		settings.setId(id);
		inventory.setUserData(entity);
		return repository.save(entity);
	}
}
