/* MibTeX - Minimalistic tool to manage your references with BibTeX
 *
 * Distributed under BSD 3-Clause License, available at Github
 *
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import de.mibtex.Levenshtein;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to read the number of citations from Google Scholar.
 *
 * @author Thomas Thuem, Christopher Sontag
 */
public class ScholarCitations {

    private final static String SCHOLAR_URL = "http://scholar.google.com/scholar?q=";
    private static Pattern entryPattern = Pattern.compile("<div class=\"gs_r gs_or gs_scl\" data-cid=\".*?\" data-did=\".*?\" data-lid=\".*?\" data-rp=\".*?\">(.*?)<\\/svg><\\/a><\\/div><\\/div><\\/div>");
    private static Pattern citationsPattern = Pattern
            .compile("<h3 class=\"gs_rt\">.*?<a *?href=\".*?\" data-clk=\".*?\">(.*?)<\\/a><\\/h3>.*?<a href=\".*?\">Cited by (\\d*)<\\/a>.*?<a href=\".*?\">Related articles<\\/a>");
    private static Pattern titlePattern = Pattern
            .compile("<h3 class=\"gs_rt\">.*?<a href=\".*?\" data-clk=\".*?\">(.*?)<\\/a><\\/h3>");

    private static float levenshteinParameter = 10; // This factor describes how much a title is allowed to change (Standard: 10%)

    public static int getCitations(String title) throws IOException {
        String url = SCHOLAR_URL + title.replace(" ", "%20");
        String html = toString(connect(new URL(url)));
        Matcher entryMatcher = entryPattern.matcher(html.replace("\n", ""));

        // Find minimal Distance between titles found and the original title
        int bestElementCitations = -1;
        int bestElementDistance = 99999;

        while (entryMatcher.find()) {
            Matcher citationsMatcher = citationsPattern.matcher(entryMatcher.group());
            if (citationsMatcher.find()) {
                String titleOutline = citationsMatcher.group(1);
                int citations = Integer.parseInt(citationsMatcher.group(2));

                // Remove HTML tags and annotations like [PDF], etc.
                String titleFound = titleOutline.replaceAll("<.*?>", "").replaceAll("\\[.*\\]", "")
                        .replaceAll("%20", " ").replaceAll("&#39;", "'").trim();

                int elementDistance = Levenshtein.getDistance(titleFound.toLowerCase(), title.toLowerCase());

                if (elementDistance < (title.length() / 100.0f * levenshteinParameter)) {
                    if (elementDistance < bestElementDistance) {
                        bestElementCitations = citations;
                        bestElementDistance = elementDistance;
                    } else if (elementDistance == bestElementDistance) {
                        if (citations > bestElementCitations) {
                            bestElementCitations = citations;
                            bestElementDistance = elementDistance;
                        }
                    }
                }
            } else {
                Matcher titleMatcher = titlePattern.matcher(entryMatcher.group());
                if (titleMatcher.find()) {
                    String titleOutline = titleMatcher.group(1);
                    String titleFound = titleOutline.replaceAll("<.*?>", "").replaceAll("\\[.*\\]", "")
                            .replaceAll("%20", " ").replaceAll("&#39;", "'").trim();

                    int elementDistance = Levenshtein.getDistance(titleFound.toLowerCase(), title.toLowerCase());

                    if (elementDistance < (title.length() / 100.0f * levenshteinParameter) && elementDistance < bestElementDistance) {
                        bestElementCitations = 0;
                        bestElementDistance = elementDistance;
                    }
                }
            }
        }

        if (bestElementCitations >= 0)
            return bestElementCitations;
        return CitationEntry.NOT_FOUND;
    }

    private static InputStream connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String myCookie = "GSP=ID=bc97fd2103a97010:IN=88119b4bc736c413+eda666da4771d016:CF=4";
        connection.setRequestProperty("Cookie", myCookie);
        connection.setRequestProperty("User-Agent",
                "Mozilla/6.0 (Windows NT 5.1; en-US; rv:x.x.x) Gecko/20041109 Firefox/x.x");
        return connection.getInputStream();
    }

    private static String toString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
