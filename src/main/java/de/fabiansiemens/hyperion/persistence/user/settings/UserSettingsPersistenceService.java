package de.fabiansiemens.hyperion.persistence.user.settings;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.CrudPersistenceService;

@Service
public class UserSettingsPersistenceService extends CrudPersistenceService<UserSettingsEntity, Long, UserSettingsRepository> {

	public UserSettingsPersistenceService(UserSettingsRepository repos) {
		super(repos);
	}
	
	public UserSettingsEntity createDefault() {
		return new UserSettingsEntity();
	}
}
