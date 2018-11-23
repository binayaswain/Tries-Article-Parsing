
public class Application {

    public static void main(String[] args) {
        String stopWordRegex = "(?i)a|(?i)an|(?i)the|(?i)and|(?i)or|(?i)but";

        String validPunctuations = ",.;:-!?";

        TriesArticleEngine engine = new TriesArticleEngine(stopWordRegex, validPunctuations);

        ReadWrite.readFile(engine);

        ReadWrite.readArticle(engine);

        ReadWrite.generateOutput(engine);
    }
}
