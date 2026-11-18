package com.notes.helper;

import com.notes.model.Note;
import com.notes.model.Tag;

import java.util.*;

public class SuggestionHelper {

    // strip common words so suggestions are not just 'the' and 'is'
    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            "a", "about", "all", "also", "an", "and", "are", "as", "at", "be", "because",
            "been", "but", "by", "can", "do", "for", "from", "has", "have", "if", "in",
            "is", "it", "its", "just", "more", "my", "no", "not", "of", "on", "or", "so",
            "than", "that", "the", "their", "them", "then", "there", "these", "they",
            "this", "to", "was", "we", "were", "what", "when", "which", "will", "with"
    ));

    // pulls the 5 most frequent meaningful words out of note content
    public static List<String> getKeywords(Note note) {
        if (note == null) return Collections.emptyList();

        String fullText = ((note.getTitle() != null ? note.getTitle() : "") + " " +
                           (note.getContent() != null ? note.getContent() : "")).toLowerCase();

        String[] words = fullText.split("[^a-zA-Z0-9_-]+");
        Map<String, Integer> counts = new HashMap<>();

        for (String w : words) {
            String word = w.trim();
            if (word.length() > 2 && !STOPWORDS.contains(word)) {
                counts.put(word, counts.getOrDefault(word, 0) + 1);
            }
        }

        // sort map by frequency
        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> topWords = new ArrayList<>();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            topWords.add(list.get(i).getKey());
        }
        return topWords;
    }

    // finds other notes that share the top keywords or have overlapping tags
    public static List<Note> getRelatedNotes(Note current, List<Note> allNotes, List<String> keywords) {
        if (current == null || allNotes == null) return Collections.emptyList();

        Map<Note, Integer> scoreMap = new HashMap<>();

        // build set of current note's tag names for quick overlap check
        Set<String> currentTagNames = new HashSet<>();
        if (current.getTags() != null) {
            for (Tag t : current.getTags()) {
                currentTagNames.add(t.getName().toLowerCase());
            }
        }

        for (Note other : allNotes) {
            if (other.getId() == current.getId()) continue;

            int matchCount = 0;
            String text = ((other.getTitle() != null ? other.getTitle() : "") + " " +
                           (other.getContent() != null ? other.getContent() : "")).toLowerCase();

            // keyword matches
            for (String kw : keywords) {
                if (text.contains(kw)) {
                    matchCount += 1;
                }
            }

            // tag overlap using HashSet
            if (other.getTags() != null) {
                for (Tag t : other.getTags()) {
                    if (currentTagNames.contains(t.getName().toLowerCase())) {
                        matchCount += 2;
                    }
                }
            }

            if (matchCount > 0) {
                scoreMap.put(other, matchCount);
            }
        }

        List<Map.Entry<Note, Integer>> sorted = new ArrayList<>(scoreMap.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        List<Note> results = new ArrayList<>();
        for (int i = 0; i < Math.min(3, sorted.size()); i++) {
            results.add(sorted.get(i).getKey());
        }
        return results;
    }

    // finds related tags using HashSet intersection
    public static Set<String> getRelatedTags(Note note, List<Note> relatedNotes) {
        Set<String> relatedTags = new HashSet<>();
        if (note == null || relatedNotes == null) return relatedTags;

        Set<String> currentTags = new HashSet<>();
        if (note.getTags() != null) {
            for (Tag t : note.getTags()) {
                currentTags.add(t.getName());
            }
        }

        for (Note rel : relatedNotes) {
            if (rel.getTags() != null) {
                for (Tag t : rel.getTags()) {
                    if (!currentTags.contains(t.getName())) {
                        relatedTags.add(t.getName());
                    }
                }
            }
        }
        return relatedTags;
    }
}
