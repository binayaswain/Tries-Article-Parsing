import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadWrite {

    private static final Logger LOG = Logger.getLogger(ReadWrite.class.getCanonicalName());

    private static final String LINE_FEED = System.lineSeparator();

    private ReadWrite() {
        // Private constructor to prevent object creation.
    }

    public static void readFile(TriesArticleEngine engine) {
        try (BufferedReader reader = Files.newBufferedReader(new File("companies.dat").toPath())) {
            String line = reader.readLine();
            while (line != null) {
                engine.creatTries(line.trim());
                line = reader.readLine();
            }
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error in reading file", e);
        }

        engine.creatSuffixLinks();
    }

    public static void readArticle(TriesArticleEngine engine) {
        StringBuilder artcle = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            System.out.println("Start Entering the article. Enter only \".\" in a new line to stop.");

            String line = reader.readLine();
            while (!".".equals(line)) {
                if (line == null || line.isEmpty()) {
                    continue;
                }

                artcle.append(line).append(" ");
                line = reader.readLine();
            }

            artcle.append(line);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Error in reading the article", e);
        }

        engine.parseArticle(artcle.toString());
    }

    public static void generateOutput(TriesArticleEngine engine) {
        String percentage = " %";
        Map<String, Integer> hitCountMap = engine.getHitCountMap();

        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] { "Company", "Hit Count", "Relevace" });

        Long wordCount = engine.getWordCount();
        String wordCountString = String.valueOf(wordCount);
        Integer companyCount = 0;

        int maxCompanyColumnWidth = 13;
        int maxHitColumnWidth = wordCountString.length() + 11;
        int maxRelevanceColumnWidth = 10;

        for (Entry<String, Integer> entrySet : hitCountMap.entrySet()) {
            Formatter fmt = new Formatter();
            String companyName = entrySet.getKey();
            String hitCount = String.valueOf(entrySet.getValue());
            String relevance = fmt.format("%.6g", (double) entrySet.getValue() / wordCount * 100).toString();

            maxCompanyColumnWidth = Math.max(maxCompanyColumnWidth, companyName.length() + 2);
            maxHitColumnWidth = Math.max(maxHitColumnWidth, hitCount.length() + 2);
            maxRelevanceColumnWidth = Math.max(maxRelevanceColumnWidth, relevance.length() + 2);

            rows.add(new String[] { companyName, hitCount, relevance + percentage });
            companyCount += entrySet.getValue();
            fmt.close();
        }
        Formatter fmt = new Formatter();
        String totalRelevane = fmt.format("%.6g", (double) companyCount / wordCount * 100).toString();
        fmt.close();
        rows.add(new String[] { "Total", String.valueOf(companyCount), totalRelevane + percentage });

        StringBuilder rowSeparatorBuilder = new StringBuilder();
        int maxRowLength = maxCompanyColumnWidth + maxHitColumnWidth + maxRelevanceColumnWidth + 4;
        for (int i = 0; i < maxRowLength; i++) {
            rowSeparatorBuilder.append('-');
        }

        String rowSeparator = rowSeparatorBuilder.toString();

        String format = new StringBuilder("|%1$-").append(maxCompanyColumnWidth).append(".")
                .append(maxCompanyColumnWidth).append("s").append("|%2$").append(maxHitColumnWidth).append(".")
                .append(maxHitColumnWidth).append("s").append("|%3$").append(maxRelevanceColumnWidth).append(".")
                .append(maxRelevanceColumnWidth).append("s|").append(LINE_FEED).append(rowSeparator).append(LINE_FEED)
                .toString();

        System.out.println(LINE_FEED + rowSeparator);

        for (String[] columns : rows) {
            System.out.format(format, columns[0], columns[1], columns[2]);
        }

        int middle = maxHitColumnWidth / 2;
        int totalColumnWidth = maxCompanyColumnWidth + middle;
        int wordCountColumnWidth = maxHitColumnWidth - middle + maxRelevanceColumnWidth + 1;

        String totalRowFormatter = new StringBuilder("|%1$-").append(totalColumnWidth).append(".")
                .append(totalColumnWidth).append("s").append("|%2$").append(wordCountColumnWidth).append(".")
                .append(wordCountColumnWidth).append("s|").append(LINE_FEED).append(rowSeparator).append(LINE_FEED)
                .toString();

        System.out.format(totalRowFormatter, "Total Words", wordCountString);
    }

}
