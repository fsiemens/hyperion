package de.fabiansiemens.hyperion.core.user.settings;

import java.util.Optional;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.persistence.user.settings.UserSettingsPersistenceService;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.User;

@Service
@AllArgsConstructor
public class UserSettingsService {
	
	private final UserSettingsPersistenceService persistenceService;

	public UserSettings getDefault() {
		return new UserSettings(persistenceService.createDefault());
	}
	
	public Optional<UserSettings> findByUser(User user) {
		return persistenceService.findById(user.getIdLong()).map(UserSettings::new);
	}
	
	public UserSettings update(UserSettings settings) {
		return new UserSettings(persistenceService.update(settings.getEntity()));
	}
}
