package de.fabiansiemens.hyperion.persistence.user;

import de.fabiansiemens.hyperion.persistence.inventory.InventoryEntity;
import de.fabiansiemens.hyperion.persistence.user.settings.UserSettingsEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class UserDataEntity {
	
	@Id
	@Column(name = "USER_ID")
	private Long id;
	
	@OneToOne(mappedBy = "userData", cascade = CascadeType.ALL)
	private UserSettingsEntity settings;
	
	//TODO Playlists
	
	@OneToOne(mappedBy = "userData", cascade = CascadeType.ALL)
	private InventoryEntity inventory;
}
