package com.notes.helper;

import com.notes.model.Note;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SearchHelper {

    public static class SearchResult {
        public Note note;
        public int score;

        public SearchResult(Note note, int score) {
            this.note = note;
            this.score = score;
        }
    }

    // counts how many times a substring occurs in text
    private static int countOccurrences(String text, String word) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(word, idx)) != -1) {
            count++;
            idx += word.length();
        }
        return count;
    }

    // computes score: 3 points for title match, 1 point per occurrence in content
    public static List<SearchResult> search(List<Note> allNotes, String query) {
        List<SearchResult> results = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            for (Note n : allNotes) {
                results.add(new SearchResult(n, 0));
            }
            return results;
        }

        String q = query.trim().toLowerCase();

        for (Note note : allNotes) {
            String title = (note.getTitle() != null) ? note.getTitle().toLowerCase() : "";
            String content = (note.getContent() != null) ? note.getContent().toLowerCase() : "";

            int score = 0;
            if (title.contains(q)) {
                score += 3 * countOccurrences(title, q);
            }
            if (content.contains(q)) {
                score += 1 * countOccurrences(content, q);
            }

            if (score > 0) {
                results.add(new SearchResult(note, score));
            }
        }

        // sort results: pinned notes first, then higher score, then newest
        results.sort((a, b) -> {
            if (a.note.isPinned() != b.note.isPinned()) {
                return a.note.isPinned() ? -1 : 1;
            }
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0) return cmp;
            if (a.note.getUpdatedAt() != null && b.note.getUpdatedAt() != null) {
                return b.note.getUpdatedAt().compareTo(a.note.getUpdatedAt());
            }
            return 0;
        });

        return results;
    }

    // sorts a list of notes by date or title, keeping pinned notes at the top
    public static void sortNotes(List<Note> notes, String sortBy) {
        Comparator<Note> comp;
        if ("title_asc".equalsIgnoreCase(sortBy)) {
            comp = (a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle());
        } else if ("title_desc".equalsIgnoreCase(sortBy)) {
            comp = (a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle());
        } else if ("date_asc".equalsIgnoreCase(sortBy)) {
            comp = Comparator.comparing(Note::getUpdatedAt);
        } else {
            // default is newest first
            comp = Comparator.comparing(Note::getUpdatedAt).reversed();
        }

        notes.sort((a, b) -> {
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;
            }
            return comp.compare(a, b);
        });
    }
}
