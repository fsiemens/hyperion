package de.fabiansiemens.hyperion.core.util.asciitable;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableColumn {

	private String title;
	private DataType type;
	private int width;
	private boolean flexible;
}
