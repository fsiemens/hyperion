package de.fabiansiemens.hyperion.core.features.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryFolderView {
	
	private InventoryFolder folder;
	private int page;
	private int length;
}
