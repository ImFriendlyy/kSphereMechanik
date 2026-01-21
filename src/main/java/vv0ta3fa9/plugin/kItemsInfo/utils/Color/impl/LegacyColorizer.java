package vv0ta3fa9.plugin.kItemsInfo.utils.Color.impl;

import org.bukkit.ChatColor;
import vv0ta3fa9.plugin.kItemsInfo.KItemsInfo;
import vv0ta3fa9.plugin.kItemsInfo.utils.Color.Colorizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LegacyColorizer implements Colorizer {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public LegacyColorizer(KItemsInfo plugin) {
        // Параметр оставлен для совместимости
    }

    @Override
    public String colorize(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        // Обработка HEX цветов
        final Matcher matcher = HEX_PATTERN.matcher(message);
        final StringBuilder builder = new StringBuilder(message.length() + 32);
        while (matcher.find()) {
            char[] group = matcher.group(1).toCharArray();
            matcher.appendReplacement(builder,
                    ChatColor.COLOR_CHAR + "x" +
                            ChatColor.COLOR_CHAR + group[0] +
                            ChatColor.COLOR_CHAR + group[1] +
                            ChatColor.COLOR_CHAR + group[2] +
                            ChatColor.COLOR_CHAR + group[3] +
                            ChatColor.COLOR_CHAR + group[4] +
                            ChatColor.COLOR_CHAR + group[5]);
        }
        message = matcher.appendTail(builder).toString();
        
        // Обработка обычных цветовых кодов
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}

