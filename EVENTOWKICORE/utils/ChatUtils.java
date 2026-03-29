/* Decompiler 11ms, total 256ms, lines 33 */
package dev.arab.EVENTOWKICORE.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;

public class ChatUtils {
  private static final Pattern HEX_PATTERN = Pattern.compile("&#[a-fA-F0-9]{6}");

  public static String color(String text) {
    if (text != null && !text.isEmpty()) {
      Matcher matcher = HEX_PATTERN.matcher(text);
      StringBuilder sb = new StringBuilder(text.length());

      int last;
      for(last = 0; matcher.find(); last = matcher.end()) {
        sb.append(text, last, matcher.start());
        String color = text.substring(matcher.start() + 1, matcher.end());
        sb.append(ChatColor.of(color).toString());
      }

      sb.append(text, last, text.length());
      return org.bukkit.ChatColor.translateAlternateColorCodes('&', sb.toString());
    } else {
      return text;
    }
  }

  public static String stripColor(String text) {
    return text == null ? null : org.bukkit.ChatColor.stripColor(color(text));
  }
}