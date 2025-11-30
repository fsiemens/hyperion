package de.fabiansiemens.hyperion.core.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import de.fabiansiemens.hyperion.core.user.settings.UserSettings;
import de.fabiansiemens.hyperion.persistence.user.UserDataPersistenceService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.User;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
	
	private final UserDataPersistenceService persistenceService;
	
	public UserData create(User user) {
		return new UserData(persistenceService.create(user.getIdLong()));
	}

	public UserData create(User user, UserSettings settings) {
		return new UserData(persistenceService.create(user.getIdLong(), settings.getEntity()));
	}
	
	public UserData getDefault(User user) {
		return create(user);
	}
	
	public boolean exists(User user) {
		return persistenceService.exists(user.getIdLong());
	}
	
	public Optional<UserData> findByUser(User user){
		return persistenceService.findById(user.getIdLong()).map(UserData::new);
	}
	
	public List<UserData> findAll() {
		return persistenceService.findAll()
				.stream()
				.map(UserData::new)
				.collect(Collectors.toList());
	}
	
	public UserData update(UserData userData) {
		return new UserData(persistenceService.update(userData.getEntity()));
	}
	
	public void delete(UserData userData) {
		persistenceService.delete(userData.getEntity());
	}
}
