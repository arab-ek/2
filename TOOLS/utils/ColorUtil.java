package dev.arab.TOOLS.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    // Wzorzec dla kolorów HEX, np. &#FFFFFF
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String fixColor(String text) {
        if (text == null || text.isEmpty()) return text;

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer(text.length() + 4 * 8);

        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.COLOR_CHAR + "x"
                    + ChatColor.COLOR_CHAR + group.charAt(0) + ChatColor.COLOR_CHAR + group.charAt(1)
                    + ChatColor.COLOR_CHAR + group.charAt(2) + ChatColor.COLOR_CHAR + group.charAt(3)
                    + ChatColor.COLOR_CHAR + group.charAt(4) + ChatColor.COLOR_CHAR + group.charAt(5)
            );
        }

        String hexTłumaczony = matcher.appendTail(buffer).toString();
        // Na koniec zamieniamy standardowe kolory Bukkitowe (np. &a na §a)
        return ChatColor.translateAlternateColorCodes('&', hexTłumaczony);
    }
}