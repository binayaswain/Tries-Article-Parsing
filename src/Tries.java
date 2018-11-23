import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class Tries {

    private final Map<Character, Tries> suffixLinks;

    private Tries prefixLink;

    private String normalizedName;

    public Tries() {
        suffixLinks = new HashMap<>();
    }

    public void insert(char[] elements) {
        Tries root = this;
        Tries currentNode = this;

        StringBuilder defaultName = new StringBuilder();
        boolean append = true;

        for (char character : elements) {
            if (append && '\t' != character) {
                defaultName.append(character);
            }

            if (Character.isLetterOrDigit(character)) {
                currentNode = currentNode.suffixLinks.computeIfAbsent(character, k -> new Tries());
                continue;
            }

            if ('\t' == character) {
                append = false;
                currentNode.normalizedName = defaultName.toString();
                currentNode = root;
            }
        }

        currentNode.normalizedName = defaultName.toString();
    }

    public void createPrefixLinks() {
        createPrefixLink(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(Arrays.asList(this)));
    }

    private void createPrefixLink(Queue<Tries> previousNodes, Queue<Character> keys, Queue<Tries> bfsQueue) {
        Tries currentNode = bfsQueue.poll();

        if (currentNode == null) {
            return;
        }

        Tries previousNode = previousNodes.poll();
        Character currentKey = keys.poll();

        if (previousNode != null && currentKey != null) {
            currentNode.prefixLink = previousNode.findPrefixLink(currentKey);
        }

        if (suffixLinks.isEmpty()) {
            return;
        }

        for (Entry<Character, Tries> entrySet : currentNode.suffixLinks.entrySet()) {
            bfsQueue.add(entrySet.getValue());
            keys.add(entrySet.getKey());
            previousNodes.add(currentNode);
        }

        createPrefixLink(previousNodes, keys, bfsQueue);
    }

    public int generateHitCount(Map<String, Integer> hitMap, char[] input, String stopWordRegex,
            String validPunctuations) {
        Tries currentNode = this;

        String match = null;
        int wordCount = 0;
        boolean skipToNextWord = false;
        boolean isApostrophy = false;

        StringBuilder currentWord = new StringBuilder();
        AtomicInteger compoundWordCount = new AtomicInteger(0);

        for (char character : input) {
            if (Character.isLetterOrDigit(character)) {
                currentWord.append(character);
            }

            if (Character.isWhitespace(character) || isPunctuation(validPunctuations, character)) {
                match = currentNode.normalizedName != null ? currentNode.normalizedName : match;
                wordCount += countNewWord(stopWordRegex, currentWord.toString(), compoundWordCount, skipToNextWord);
                currentWord = new StringBuilder();
                skipToNextWord = false;
                isApostrophy = false;
                continue;
            }

            if (skipToNextWord) {
                continue;
            }

            if (!Character.isLetterOrDigit(character)) {
                isApostrophy = '\'' == character;
                continue;
            }

            if (currentNode.suffixLinks.containsKey(character)) {
                currentNode = currentNode.suffixLinks.get(character);
                continue;
            }

            if (isApostrophy) {
                isApostrophy = false;
                match = currentNode.normalizedName != null ? currentNode.normalizedName : match;
                compoundWordCount.getAndSet(0);
                currentNode = findPrefixLink(character);
                continue;
            }

            wordCount += processWordCount(compoundWordCount, match, currentNode.normalizedName);
            match = addToMap(hitMap, match);
            currentNode = findPrefixLink(character);
            skipToNextWord = currentNode.prefixLink == null;
        }

        match = currentNode.normalizedName != null ? currentNode.normalizedName : match;
        wordCount += processWordCount(compoundWordCount, match, currentNode.normalizedName);
        addToMap(hitMap, match);

        return wordCount;
    }

    private Tries findPrefixLink(char key) {
        if (suffixLinks.containsKey(key)) {
            return suffixLinks.get(key);
        }

        if (prefixLink == null) {
            return this;
        }

        return prefixLink.findPrefixLink(key);
    }

    private String addToMap(Map<String, Integer> hitMap, String key) {
        if (key != null) {
            hitMap.compute(key, (k, v) -> v == null ? 1 : ++v);
        }

        return null;
    }

    private int countNewWord(String stopWordRegex, String currentWord, AtomicInteger companyWordCount,
            boolean isSkipWord) {
        if (currentWord == null || currentWord.isEmpty() || currentWord.matches(stopWordRegex)) {
            return 0;
        }

        if (!isSkipWord) {
            companyWordCount.getAndIncrement();
        }

        return 1;

    }

    private int processWordCount(AtomicInteger companyWordCount, String previousMatch, String currentMatch) {
        int compoundWordCount = companyWordCount.getAndSet(0);

        if (previousMatch != null && currentMatch != null) {
            return -1 * compoundWordCount + 1;
        }

        return 0;
    }

    private boolean isPunctuation(String validPunctuations, char character) {
        return !Character.isLetterOrDigit(character) && validPunctuations.indexOf(character) > -1;
    }
}
