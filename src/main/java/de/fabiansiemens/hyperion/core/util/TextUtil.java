package de.fabiansiemens.hyperion.core.util;

public class TextUtil {

	public static String error() {
		return "❌ | ";
	}

	public static String success() {
		return "✅ | ";
	}
	
	public static String info() {
		return "💬 | ";
	}
	
	public static String warn() {
		return "⚠ | ";
	}

	public static String getMessagePrefixEmoji(String idPrefix) {
		switch(idPrefix) {
		case "error": return error();
		case "success": return success();
		case "info": return info();
		case "warn": return warn();
		default: return "";
		}
	}
}
