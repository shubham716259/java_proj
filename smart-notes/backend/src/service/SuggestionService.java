package service;

import model.Note;

import java.util.*;

/**
 * Service for keyword extraction and related-note recommendations.
 * Uses HashMap for keyword frequency counting and HashSet for stopwords.
 */
public class SuggestionService {

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "a", "about", "above", "after", "again", "against", "all", "am", "an", "and",
            "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being",
            "below", "between", "both", "but", "by", "can", "can't", "cannot", "could",
            "did", "do", "does", "doing", "don't", "down", "during", "each", "few", "for",
            "from", "further", "had", "has", "have", "having", "he", "her", "here", "hers",
            "herself", "him", "himself", "his", "how", "i", "if", "in", "into", "is", "isn't",
            "it", "its", "itself", "just", "me", "more", "most", "my", "myself", "no", "nor",
            "not", "now", "of", "off", "on", "once", "only", "or", "other", "ought", "our",
            "ours", "ourselves", "out", "over", "own", "same", "she", "should", "so", "some",
            "such", "than", "that", "the", "their", "theirs", "them", "themselves", "then",
            "there", "these", "they", "this", "those", "through", "to", "too", "under", "until",
            "up", "very", "was", "we", "were", "what", "when", "where", "which", "while",
            "who", "whom", "why", "with", "would", "you", "your", "yours", "yourself", "yourselves"
    ));

    /**
     * Extracts top 5 distinctive keywords from text using HashMap frequency counting.
     */
    public List<String> extractKeywords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // Clean punctuation, split into words
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ");
        String[] words = cleaned.split("\\s+");

        // Count frequency with HashMap<String, Integer>
        Map<String, Integer> freqMap = new HashMap<>();
        for (String w : words) {
            String trimmed = w.trim();
            if (trimmed.length() > 2 && !STOP_WORDS.contains(trimmed)) {
                freqMap.put(trimmed, freqMap.getOrDefault(trimmed, 0) + 1);
            }
        }

        // Sort by frequency descending
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(freqMap.entrySet());
        entries.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        // Return top 5 keywords
        List<String> topKeywords = new ArrayList<>();
        for (int i = 0; i < Math.min(5, entries.size()); i++) {
            topKeywords.add(entries.get(i).getKey());
        }
        return topKeywords;
    }

    /**
     * Recommends other notes that share the extracted keywords.
     */
    public List<Note> getRelatedNotes(Note currentNote, List<Note> allNotes, List<String> keywords) {
        if (keywords.isEmpty() || allNotes == null || allNotes.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> kwSet = new HashSet<>(keywords);
        Map<Note, Integer> matchScores = new HashMap<>();

        for (Note candidate : allNotes) {
            if (candidate.getId() == currentNote.getId()) {
                continue; // Skip the current note itself
            }

            String candidateText = (candidate.getTitle() + " " + candidate.getContent()).toLowerCase();
            int score = 0;
            for (String kw : kwSet) {
                if (candidateText.contains(kw)) {
                    score++;
                }
            }

            if (score > 0) {
                matchScores.put(candidate, score);
            }
        }

        // Sort candidates by match score descending
        List<Map.Entry<Note, Integer>> sorted = new ArrayList<>(matchScores.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        List<Note> related = new ArrayList<>();
        for (int i = 0; i < Math.min(4, sorted.size()); i++) {
            related.add(sorted.get(i).getKey());
        }
        return related;
    }
}
