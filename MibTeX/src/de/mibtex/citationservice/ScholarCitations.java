/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.mibtex.Levenshtein;

/**
 * A class to read the number of citations from Google Scholar.
 * 
 * @author Thomas Thuem, Christopher Sontag
 */
public class ScholarCitations {
    
    private final static String SCHOLAR_URL = "http://scholar.google.com/scholar?q=";
    private static Pattern citationsPattern = Pattern
            .compile("<div class=\"gs_r\">.*?<h3 class=\"gs_rt\">(.*?)<\\/h3>.*?Cited by (\\d*).*?Save<\\/a>"); // OLD Pattern: "Cited by \\s*(\\d+)"
    private static float levenshteinParameter = 0.7f; // This factor describes how much a title is allowed to change
                                                      // (Standart: Max. 70% of the original title)
    
    // public static int getYearlyCitations(String title, int publicationYear) {
    // int citations = getCitations(title);
    // if (citations < 0 || publicationYear < 1900)
    // return citations;
    // int currentYear = Calendar.getInstance().get(Calendar.YEAR);
    // double passedYears = currentYear - publicationYear + 1;
    // if (passedYears == 0)
    // return citations;
    // int average = (int) Math.round(citations / passedYears);
    // return average;
    // }
    
    public static int getCitations(String title) throws MalformedURLException, IOException {
        String url = SCHOLAR_URL + title.replace(" ", "%20");
        String html = toString(connect(new URL(url)));
        Matcher matcher = citationsPattern.matcher(html.replace("\n", ""));
        
        // Find minimal Distance between titles found and the original title
        int bestElementCitations = 0;
        int bestElementDistance = 99999;
        
        while (matcher.find()) {
            String titleOutline = matcher.group(1);
            int citations = Integer.parseInt(matcher.group(2));
            
            // Remove HTML tags and annotations like [PDF], etc.
            String titleFound = titleOutline.replaceAll("<.*?>", "").replaceAll("\\[.*\\]", "")
                    .replaceAll("%20", " ").trim();
            int elementDistance = Levenshtein.getDistance(titleFound, title);
            
            if (elementDistance < title.length() * levenshteinParameter) {
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
        }
        if (bestElementCitations > 0)
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
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
    
}
