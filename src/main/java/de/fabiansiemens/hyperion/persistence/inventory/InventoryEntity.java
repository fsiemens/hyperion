package de.fabiansiemens.hyperion.persistence.inventory;

import de.fabiansiemens.hyperion.persistence.user.UserDataEntity;
import io.micrometer.common.lang.NonNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;

@Data
@Entity
public class InventoryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@Column(name = "INVENTORY_ID")
	private Long id;
	
	@OneToOne
	@PrimaryKeyJoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
	private UserDataEntity userData;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "ELEMENT_ID", nullable = false, unique = true)
	private InventoryFolderEntity rootFolder;
	
	public InventoryEntity() {
		this.rootFolder = InventoryFolderEntity.getNewRoot("root", "This is your personal Inventory!");
	}
	
	public InventoryEntity(@NonNull UserDataEntity userData) {
		this.rootFolder = InventoryFolderEntity.getNewRoot("root", "This is your personal Inventory!");
		this.userData = userData;
	}

//	private InventorySettingsEntity inventorySettings;
}
