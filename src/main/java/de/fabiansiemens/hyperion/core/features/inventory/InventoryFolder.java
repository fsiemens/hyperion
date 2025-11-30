package de.fabiansiemens.hyperion.core.features.inventory;

import java.util.List;
import java.util.stream.Collectors;

import de.fabiansiemens.hyperion.persistence.inventory.InventoryFolderEntity;
import io.micrometer.common.lang.NonNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InventoryFolder extends InventoryElement {

	private InventoryFolderEntity entity;
	
	public InventoryFolder(@NonNull InventoryFolderEntity entity) {
		super(entity);
		this.entity = entity;
	}
	
	public List<InventoryFolder> getSubFolders(){
		return this.entity.getSubFolders()
				.stream()
				.map(InventoryFolder::new)
				.collect(Collectors.toList());
	}
	
	public void setSubFolders(List<InventoryFolder> subFolders) {
		this.entity.setSubFolders(
			subFolders.stream()
			.map(folder -> folder.getEntity())
			.collect(Collectors.toList())
		);
	}
	
	public List<InventoryItem> getItems(){
		return this.entity.getItems()
				.stream()
				.map(InventoryItem::new)
				.collect(Collectors.toList());
	}

	public void setItems(List<InventoryItem> items) {
		this.entity.setItems(
			items.stream()
			.map(folder -> folder.getEntity())
			.collect(Collectors.toList())
		);
	}
	
	public void addElement(InventoryElement element) {
		if(element instanceof InventoryFolder) {
			InventoryFolder folder = (InventoryFolder) element;
			this.entity.getSubFolders().add(folder.getEntity());
		}
		else {
			InventoryItem item = (InventoryItem) element;
			this.entity.getItems().add(item.getEntity());
		}
	}

	public String getBreadcrumbs() {
		if(this.getParentFolder() == null)
			return "~/";
		
		return this.getParentFolder().getBreadcrumbs() + this.getName() + "/";
	}

	public int getChildCount() {
		return this.getSubFolders().size() + this.getItems().size();
	}

	public double getValue() {
		double value = 0;
		for(InventoryItem item : this.getItems())
			value += item.getPrice() * item.getQuantity();
			
		for(InventoryFolder folder : this.getSubFolders())
			value += folder.getValue();
		
		return value;
	}
}
