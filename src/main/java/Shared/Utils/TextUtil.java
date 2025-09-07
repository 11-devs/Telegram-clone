package Shared.Utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class TextUtil {
    public static String stripFormattingForPreview(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String result = text;

        // 1. Spoilers: ||content|| -> Spoiler
        result = result.replaceAll("\\|\\|.*?\\|\\|", "Spoiler");

        // 2. Bold: **content** -> content
        result = result.replaceAll("\\*\\*(.*?)\\*\\*", "$1");

        // 3. Italic: __content__ -> content
        result = result.replaceAll("__(.*?)__", "$1");

        // 4. Underline: ++content++ -> content
        result = result.replaceAll("\\+\\+(.*?)\\+\\+", "$1");

        return result;
    }
    public static String stripFormattingForCopying(String rawText) {
        if (rawText == null || rawText.isEmpty()) {
            return "";
        }
        // This will remove the markers but keep the content inside them.
        return rawText
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("__(.*?)__", "$1")
                .replaceAll("\\+\\+(.*?)\\+\\+", "$1")
                .replaceAll("\\|\\|(.*?)\\|\\|", "$1");
    }
    public static String formatLastSeen(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isBlank()) {
            return "last seen a long time ago";
        }
        try {
            LocalDateTime lastSeenTime = LocalDateTime.parse(isoTimestamp);
            LocalDateTime now = LocalDateTime.now();

            if (lastSeenTime.toLocalDate().isEqual(now.toLocalDate())) {
                return "last seen today at " + lastSeenTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else if (lastSeenTime.toLocalDate().isEqual(now.toLocalDate().minusDays(1))) {
                return "last seen yesterday at " + lastSeenTime.format(DateTimeFormatter.ofPattern("HH:mm"));
            } else {
                return "last seen on " + lastSeenTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
            }
        } catch (Exception e) {
            System.err.println("Could not parse last seen timestamp: " + isoTimestamp);
            return "last seen a long time ago";
        }
    }
}

