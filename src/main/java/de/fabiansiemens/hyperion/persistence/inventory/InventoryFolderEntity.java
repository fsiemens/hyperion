package de.fabiansiemens.hyperion.persistence.inventory;

import java.util.LinkedList;
import java.util.List;

import io.micrometer.common.lang.NonNull;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryFolderEntity extends InventoryElementEntity {
	
	@OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<InventoryFolderEntity> subFolders;
	
	@OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<InventoryItemEntity> items;

	public static InventoryFolderEntity getNewRoot(@NonNull String name, @NonNull String description) {
		InventoryFolderEntity root = new InventoryFolderEntity(name, description);
		return root;
	}
	
	private InventoryFolderEntity(@NonNull String name, @NonNull String description) {
		super(null, name, description, null);
		this.subFolders = new LinkedList<>();
		this.items = new LinkedList<>();
	}
	
	public InventoryFolderEntity(@NonNull String name, @NonNull String description, @NonNull InventoryFolderEntity parent) {
		super(null, name, description, parent);
		this.subFolders = new LinkedList<>();
		this.items = new LinkedList<>();
	}
}
