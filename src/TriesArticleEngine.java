import java.util.HashMap;
import java.util.Map;

public class TriesArticleEngine {

    private final Tries companyTries;

    private final Map<String, Integer> hitCountMap;

    private final String stopWordRegex;

    private final String validPunctuations;

    private long wordCount;

    public TriesArticleEngine(String stopWordRegex, String validPunctuations) {
        companyTries = new Tries();
        hitCountMap = new HashMap<>();
        this.stopWordRegex = stopWordRegex;
        this.validPunctuations = validPunctuations;
        wordCount = 0L;
    }

    public void creatTries(String inputLine) {
        companyTries.insert(inputLine.toCharArray());
    }

    public void creatSuffixLinks() {
        companyTries.createPrefixLinks();
    }

    public void parseArticle(String article) {
        wordCount += companyTries.generateHitCount(hitCountMap, article.trim().toCharArray(), stopWordRegex,
                validPunctuations);
    }

    public Map<String, Integer> getHitCountMap() {
        return hitCountMap;
    }

    public Long getWordCount() {
        return wordCount;
    }

}
