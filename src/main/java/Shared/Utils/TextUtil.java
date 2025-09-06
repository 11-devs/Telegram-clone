package Shared.Utils;

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
}

