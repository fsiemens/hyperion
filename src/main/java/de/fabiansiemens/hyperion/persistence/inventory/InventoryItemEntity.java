package de.fabiansiemens.hyperion.persistence.inventory;

import de.fabiansiemens.hyperion.persistence.file.FileEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryItemEntity extends InventoryElementEntity {

	@Column(nullable = false)
	private int quantity;
	
	@Column(nullable = false)
	private double price;
	
	@Column(nullable = false)
	private boolean requiresAttunement;
	
	@Column(nullable = false)
	private boolean attuned;
	
	@OneToOne(cascade = CascadeType.ALL)
	@Nullable
	private FileEntity image;
	
	public InventoryItemEntity(String name, String description,
			InventoryFolderEntity parentFolder, int quantity, double price, boolean requiresAttunement, boolean attuned, FileEntity image) {
		super(null, name, description, parentFolder);
		this.quantity = quantity;
		this.price = price;
		this.requiresAttunement = requiresAttunement;
		this.attuned = attuned;
		this.image = image;
	}

}
