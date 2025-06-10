package org.vg.markusbro.core.service.plugins.llm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextFormatUtils {

    public static String formatText(String text) {
        text = formatBold(text);

        return text;
    }

    private static String formatBold(String text) {
        final Map<String, String> replacements = new LinkedHashMap<>();

        Pattern pattern = Pattern.compile("\\*\\*.*?\\*\\*");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String found = matcher.group();
            replacements.put(found, "<b>" + found.substring(2, found.length() - 2) + "</b>");
        }

        for (Map.Entry<String, String> replacement : replacements.entrySet()) {
            text = text.replace(replacement.getKey(), replacement.getValue());
        }

        return text;
    }
}