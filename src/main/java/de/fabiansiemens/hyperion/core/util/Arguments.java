package de.fabiansiemens.hyperion.core.util;

import java.util.HashMap;
import java.util.Map;

import de.fabiansiemens.hyperion.core.exceptions.ArgumentTypeException;
import io.micrometer.common.lang.Nullable;
import lombok.NonNull;

public class Arguments {

	public static class Entry {
		private final Object value;
		
		public Entry(Object value) {
			this.value = value;
		}
		
		public String asString() {
			if(this.value == null)
				return null;
			
			if (this.value instanceof String valString)
		        return valString;
		    if (this.value instanceof Number valNumber)
		        return String.valueOf(valNumber);
		    if (this.value instanceof Boolean valBool)
		        return String.valueOf(valBool);

		    return null;
		}
		
		public long asLong() throws ArgumentTypeException {
			if(this.value instanceof Long valLong)
				return valLong;
			
			try {
				return Long.parseLong(asString());
			}
			catch(NumberFormatException e) {
				throw new ArgumentTypeException();
			}
		}
		
		public int asInteger() throws ArgumentTypeException {
			if(this.value instanceof Integer valInt)
				return valInt;
			
			try {
				return Integer.parseInt(asString());
			}
			catch(NumberFormatException e) {
				throw new ArgumentTypeException();
			}
		}
		
		public double asDouble() throws ArgumentTypeException {
			if(this.value instanceof Double valDouble)
				return valDouble;
			
			try {
				return Double.parseDouble(asString());
			}
			catch(NumberFormatException e) {
				throw new ArgumentTypeException();
			}
		}
		
		public boolean asBoolean() throws ArgumentTypeException {
			if(this.value == null)
				return false;
			
			if(this.value instanceof Boolean valBool)
				return valBool;
			
			try {
				return asDouble() != 0;
			} catch (ArgumentTypeException e) {}
			
			try {
				return asLong() != 0;
			} catch (ArgumentTypeException e) {}
			
			try {
				return asString().equalsIgnoreCase("true");
			} catch(Exception e) {}
			
			throw new ArgumentTypeException();
		}
		
		public Object asObject() {
			return this.value;
		}
		
		public <T> T as(Class<T> clazz) throws ArgumentTypeException {
			if(clazz.isInstance(this.value)) {
				return clazz.cast(this.value);
			}
			
			throw new ArgumentTypeException();
		}
	}
	
	private final Map<String, Object> argumentMap;
	
	private Arguments() {
		this.argumentMap = new HashMap<>();
	}
	
	public static Arguments empty() {
		return new Arguments();
	}
	
	public static Arguments of(@NonNull String key, @Nullable Object value) {
		Arguments args = new Arguments();
		args.put(key, value);
		return args;
	}
	
	public Arguments put(@NonNull String key, @Nullable Object value) {
		this.argumentMap.put(key, value);
		return this;
	}

	public Map<String, Object> getMap() {
		return argumentMap;
	}
	
	public Entry getOrDefault(String key, Object defaultValue) {
		return new Entry(this.argumentMap.getOrDefault(key, defaultValue));
	}

	public Entry get(String key) {
		return new Entry(this.argumentMap.get(key));
	}

	public String dump() {
		StringBuilder builder = new StringBuilder();
		getMap().forEach((key, value) -> builder.append(key).append(": ").append(new Entry(value).asString()).append("; "));
		return builder.toString();
	}
}
