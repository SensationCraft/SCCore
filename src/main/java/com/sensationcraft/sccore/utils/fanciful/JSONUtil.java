package com.sensationcraft.sccore.utils.fanciful;

import org.bukkit.ChatColor;

/**
 * @author DarkSeraphim
 **/
public class JSONUtil {
	private static final StringBuilder JSON_BUILDER = new StringBuilder("{\"text\":\"\",\"extra\":[");

	private static final int RETAIN = "{\"text\":\"\",\"extra\":[".length();
	private static final StringBuilder STYLE = new StringBuilder();

	public static String toJSON(String message) {
		if (message == null || message.isEmpty())
			return null;
		if (JSONUtil.JSON_BUILDER.length() > JSONUtil.RETAIN)
			JSONUtil.JSON_BUILDER.delete(JSONUtil.RETAIN, JSONUtil.JSON_BUILDER.length());
		String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
		boolean first = true;
		String colour = null;
		String format = null;
		boolean ignoreFirst = !parts[0].isEmpty() && ChatColor.getByChar(parts[0].charAt(0)) != null;
		for (String part : parts) {
			// If it starts with a colour, just ignore the empty String
			// before it
			if (part.isEmpty()) {
				continue;
			}

			String newStyle = null;
			if (!ignoreFirst) {
				newStyle = JSONUtil.getStyle(part.charAt(0));
			} else {
				ignoreFirst = false;
			}

			if (newStyle != null) {
				part = part.substring(1);
				if (newStyle.startsWith("\"c"))
					colour = newStyle;
				else
					format = newStyle;
			}
			if (!part.isEmpty()) {
				if (first)
					first = false;
				else {
					JSONUtil.JSON_BUILDER.append(",");
				}
				JSONUtil.JSON_BUILDER.append("{");
				if (colour != null) {
					JSONUtil.JSON_BUILDER.append(colour);
					colour = null;
				}
				if (format != null) {
					JSONUtil.JSON_BUILDER.append(format);
					format = null;
				}
				JSONUtil.JSON_BUILDER.append(String.format("text:\"%s\"", part));
				JSONUtil.JSON_BUILDER.append("}");
			}
		}
		return JSONUtil.JSON_BUILDER.append("]}").toString();
	}

	private static String getStyle(char colour) {
		if (JSONUtil.STYLE.length() > 0)
			JSONUtil.STYLE.delete(0, JSONUtil.STYLE.length());
		switch (colour) {
		case 'k':
			return "\"obfuscated\": true,";
		case 'l':
			return "\"bold\": true,";
		case 'm':
			return "\"strikethrough\": true,";
		case 'n':
			return "\"underlined\": true,";
		case 'o':
			return "\"italic\": true,";
		case 'r':
			return "\"reset\": true,";
		default:
			break;
		}
		ChatColor cc = ChatColor.getByChar(colour);
		if (cc == null)
			return null;
		return JSONUtil.STYLE.append("\"color\":\"").append(cc.name().toLowerCase()).append("\",").toString();
	}
}