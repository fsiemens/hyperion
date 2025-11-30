package de.fabiansiemens.hyperion.core.features.inventory;

import de.fabiansiemens.hyperion.persistence.inventory.InventoryElementEntity;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class InventoryElement {

	private InventoryElementEntity entity;
	
	public Long getId() {
		return this.entity.getId();
	}
	
	public String getName() {
		return this.entity.getName();
	}
	
	public void setName(String name) {
		this.entity.setName(name);
	}
	
	public String getDescription() {
		return this.entity.getDescription();
	}
	
	public void setDescription(String description) {
		this.entity.setDescription(description);
	}
	
	@Nullable
	public InventoryFolder getParentFolder() {
		if(this.entity.getParentFolder() == null)
			return null;
		
		return new InventoryFolder(this.entity.getParentFolder());
	}
	
	public void setParentFolder(InventoryFolder parentFolder) {
		this.entity.setParentFolder(parentFolder.getEntity());
	}
}
