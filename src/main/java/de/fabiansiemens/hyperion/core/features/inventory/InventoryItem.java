package de.fabiansiemens.hyperion.core.features.inventory;

import de.fabiansiemens.hyperion.persistence.inventory.InventoryItemEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryItem extends InventoryElement {

	private InventoryItemEntity entity;
	
	public InventoryItem(InventoryItemEntity entity) {
		super(entity);
		this.entity = entity;
	}

	public int getQuantity() {
		return this.entity.getQuantity();
	}
	
	public void setQuantity(int quantity) {
		this.entity.setQuantity(quantity);
	}
	
	public double getPrice() {
		return this.entity.getPrice();
	}
	
	public void setPrice(double price) {
		this.entity.setPrice(price);
	}
	
	public boolean isRequiresAttunement() {
		return this.entity.isRequiresAttunement();
	}
	
	public void setRequiresAttunement(boolean requiresAttunement) {
		this.entity.setAttuned(requiresAttunement);
	}
	
	public boolean isAttuned() {
		return this.entity.isAttuned();
	}
	
	public void setAttuned(boolean attuned) {
		this.entity.setAttuned(attuned);
	}
}
