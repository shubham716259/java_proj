package service;

import model.Note;
import model.Tag;

import java.sql.Timestamp;
import java.util.*;

/**
 * Service for in-memory keyword search, relevance scoring, and sorting.
 * Uses Comparator for sorting and HashMap for occurrence scoring.
 */
public class SearchService {

    public static class SearchResult {
        private final Note note;
        private final int score;

        public SearchResult(Note note, int score) {
            this.note = note;
            this.score = score;
        }

        public Note getNote() {
            return note;
        }

        public int getScore() {
            return score;
        }
    }

    /**
     * Filters, searches, scores, and sorts notes.
     *
     * @param notes list of all notes
     * @param query keyword search query
     * @param folderId optional folder filter (-1 for all)
     * @param tagId optional tag filter (-1 for all)
     * @param sortBy "date" or "title"
     * @param order "asc" or "desc"
     * @return filtered and sorted list of notes
     */
    public List<Note> searchAndFilter(List<Note> notes, String query, int folderId, int tagId,
                                      String sortBy, String order) {
        List<Note> filtered = new ArrayList<>();

        // 1. Filter by folder and tag
        for (Note n : notes) {
            if (folderId > 0) {
                if (n.getFolder() == null || n.getFolder().getId() != folderId) {
                    continue;
                }
            }
            if (tagId > 0) {
                boolean hasTag = false;
                if (n.getTags() != null) {
                    for (Tag t : n.getTags()) {
                        if (t.getId() == tagId) {
                            hasTag = true;
                            break;
                        }
                    }
                }
                if (!hasTag) {
                    continue;
                }
            }
            filtered.add(n);
        }

        // 2. If search query is provided, calculate relevance scores
        if (query != null && !query.trim().isEmpty()) {
            String lowerQuery = query.trim().toLowerCase();
            String[] terms = lowerQuery.split("\\s+");
            List<SearchResult> scored = new ArrayList<>();

            for (Note n : filtered) {
                int score = calculateScore(n, terms);
                if (score > 0) {
                    scored.add(new SearchResult(n, score));
                }
            }

            // Sort by pinned first, then by score descending, then by updated_at descending
            scored.sort((a, b) -> {
                if (a.getNote().isPinned() != b.getNote().isPinned()) {
                    return b.getNote().isPinned() ? 1 : -1;
                }
                if (a.getScore() != b.getScore()) {
                    return Integer.compare(b.getScore(), a.getScore());
                }
                if (a.getNote().getUpdatedAt() != null && b.getNote().getUpdatedAt() != null) {
                    return b.getNote().getUpdatedAt().compareTo(a.getNote().getUpdatedAt());
                }
                return 0;
            });

            List<Note> result = new ArrayList<>();
            for (SearchResult sr : scored) {
                result.add(sr.getNote());
            }
            return result;
        }

        // 3. Otherwise, sort by requested criteria (pinned notes always first)
        sortNotes(filtered, sortBy, order);
        return filtered;
    }

    /**
     * Calculates relevance score:
     * - Title match = 3 points per term
     * - Content match = 1 point per occurrence of term
     */
    private int calculateScore(Note note, String[] terms) {
        String title = note.getTitle() != null ? note.getTitle().toLowerCase() : "";
        String content = note.getContent() != null ? note.getContent().toLowerCase() : "";
        int totalScore = 0;

        for (String term : terms) {
            if (term.isEmpty()) continue;

            // Title match: 3 points per occurrence
            int titleCount = countOccurrences(title, term);
            totalScore += (titleCount * 3);

            // Content match: 1 point per occurrence
            int contentCount = countOccurrences(content, term);
            totalScore += (contentCount * 1);
        }

        return totalScore;
    }

    private int countOccurrences(String text, String term) {
        if (text.isEmpty() || term.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(term, idx)) != -1) {
            count++;
            idx += term.length();
        }
        return count;
    }

    /**
     * Sorts notes using Java Comparator.
     * Pinned notes always appear on top.
     */
    public void sortNotes(List<Note> notes, String sortBy, String order) {
        boolean isAsc = "asc".equalsIgnoreCase(order);
        boolean byTitle = "title".equalsIgnoreCase(sortBy);

        Comparator<Note> comparator = (a, b) -> {
            // Pinned notes always on top
            if (a.isPinned() != b.isPinned()) {
                return b.isPinned() ? 1 : -1;
            }

            if (byTitle) {
                String t1 = a.getTitle() != null ? a.getTitle() : "";
                String t2 = b.getTitle() != null ? b.getTitle() : "";
                int cmp = String.CASE_INSENSITIVE_ORDER.compare(t1, t2);
                return isAsc ? cmp : -cmp;
            } else {
                // Default: by updated_at
                Timestamp d1 = a.getUpdatedAt();
                Timestamp d2 = b.getUpdatedAt();
                if (d1 == null && d2 == null) return 0;
                if (d1 == null) return 1;
                if (d2 == null) return -1;
                int cmp = d1.compareTo(d2);
                return isAsc ? cmp : -cmp;
            }
        };

        notes.sort(comparator);
    }

    /**
     * Extracts related searches based on common keywords found in search results.
     */
    public List<String> getRelatedSearches(List<Note> results, String originalQuery) {
        if (results == null || results.isEmpty()) return Collections.emptyList();

        Set<String> queryWords = new HashSet<>();
        if (originalQuery != null) {
            for (String w : originalQuery.toLowerCase().split("\\s+")) {
                queryWords.add(w.trim());
            }
        }

        Map<String, Integer> freq = new HashMap<>();
        SuggestionService sugg = new SuggestionService();
        for (Note n : results) {
            List<String> keywords = sugg.extractKeywords(n.getContent());
            for (String kw : keywords) {
                if (!queryWords.contains(kw)) {
                    freq.put(kw, freq.getOrDefault(kw, 0) + 1);
                }
            }
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(freq.entrySet());
        list.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<String> related = new ArrayList<>();
        for (int i = 0; i < Math.min(5, list.size()); i++) {
            related.add(list.get(i).getKey());
        }
        return related;
    }
}
