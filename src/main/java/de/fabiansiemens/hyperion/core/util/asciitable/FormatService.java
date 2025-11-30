package de.fabiansiemens.hyperion.core.util.asciitable;

import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class FormatService {
	
	private static final String[] SUFFIXES = {"", "k", "M", "G", "T", "P", "E"}; // Tausender-Suffixe

    public String formatNumber(double number, int maxLength) {
        // Wenn die Zahl < 1000 ist, formatiere sie ohne Nachkommastelle
        if (number < 1000) {
            return (number == (int) number) ? String.format("%.0f", number) : String.valueOf(number);
        }

        int exponent = (int) (Math.log10(number) / 3); // Bestimme Tausender-Schritt
        exponent = Math.min(exponent, SUFFIXES.length - 1); // Begrenzung

        double shortened = number / Math.pow(1000, exponent);
        String suffix = SUFFIXES[exponent];

        String result = String.format("~%.1f%s", shortened, suffix); // Standard: eine Nachkommastelle

        if (result.length() > maxLength) {
            result = String.format("~%.0f%s", (double) Math.round(shortened), suffix); // Ohne Nachkommastelle
        }

        return result;
    }
    
    public String shorten(String string, int length) {
    	if(string.length() <= length)
    		return string;
 
    	return string.substring(0, length -2) + "..";
    }
    
    public String exactLength(String string, int length) {
    	if(string.length() == length)
    		return string;
    	
    	if(string.length() > length)
    		return shorten(string, length);
    	
    	while(string.length() < length) {
			string += " ";
		}
    
    	return string;
    }
	
	public String formatColumn(String content, TableColumn column) {
		if(content.length() > column.getWidth()) {
			try {
				content = formatNumber(Double.parseDouble(content), column.getWidth());
			}
			catch (Exception e) {
				content = shorten(content, column.getWidth());
			}
		}
		
		return exactLength(content, column.getWidth());
	}
	
	public String formatTable(TableColumn[] columns, String[][] rows, int maxWidth) {
		StringBuilder builder = new StringBuilder();
		
		int targetWidth = columns.length -1;
		double amountOfFlexColumns = 0;
		for(TableColumn column : columns) {
			targetWidth += column.getWidth();
			if(column.isFlexible())
				amountOfFlexColumns++;
		}
		
		if(targetWidth > maxWidth && amountOfFlexColumns <= 0)
			throw new IllegalArgumentException("Can not create a Ascii Table based on the given configuration. Target width exceeds the maximum width, without flexible columns.");

		int margin = (int) Math.ceil((double)(targetWidth - maxWidth) / amountOfFlexColumns);
		if(margin > 0)
			for(TableColumn column : columns) {
				if(column.isFlexible())
					column.setWidth(column.getWidth() - margin);
			}
		
		for(int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			builder.append(formatColumn(column.getTitle(), column));
			if(i + 1 < columns.length)
				builder.append("|");
		}
		builder.append("\n");
		for(int i = 0; i < columns.length; i++) {
			TableColumn column = columns[i];
			for(int j = 0; j < column.getWidth(); j++) {
				builder.append("-");
			}
			if(i + 1 < columns.length)
				builder.append("+");
		}
		builder.append("\n");
		
		for(String[] data : rows) {
			for(int i = 0; i < columns.length; i++) {
				String field = data[i];
				builder.append(formatColumn(field, columns[i]));
				if(i + 1 < columns.length)
					builder.append("|");
			}
			builder.append("\n");
		}
		
		return builder.toString();
	}
}
