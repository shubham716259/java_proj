package com.notes.helper;

public class HtmlUtil {

    // escapes basic html special characters so users cannot inject script tags
    public static String escape(String input) {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '&':  sb.append("&amp;"); break;
                case '<':  sb.append("&lt;"); break;
                case '>':  sb.append("&gt;"); break;
                case '"':  sb.append("&quot;"); break;
                case '\'': sb.append("&#39;"); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    // replaces newlines with <br> for viewing notes
    public static String formatContent(String input) {
        if (input == null) return "";
        String escaped = escape(input);
        return escaped.replace("\r\n", "<br>").replace("\n", "<br>");
    }

    // wraps matching keyword with <mark> tags using case-insensitive search
    public static String highlight(String text, String keyword) {
        if (text == null || keyword == null || keyword.trim().isEmpty()) {
            return escape(text);
        }

        String lowerText = text.toLowerCase();
        String lowerKey = keyword.trim().toLowerCase();

        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        int foundIndex = lowerText.indexOf(lowerKey, lastIndex);

        while (foundIndex != -1) {
            // append escaped part before the match
            sb.append(escape(text.substring(lastIndex, foundIndex)));
            // append highlighted match
            sb.append("<mark class=\"match\">");
            sb.append(escape(text.substring(foundIndex, foundIndex + lowerKey.length())));
            sb.append("</mark>");

            lastIndex = foundIndex + lowerKey.length();
            foundIndex = lowerText.indexOf(lowerKey, lastIndex);
        }

        sb.append(escape(text.substring(lastIndex)));
        return sb.toString();
    }
}
