/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class to read the number of citations from Google Scholar.
 * 
 * @author Thomas Thuem, Christopher Sontag
 */
public class ScholarCitations {
    
    public final static String SCHOLAR_URL = "http://scholar.google.com/scholar?q=";
    
    public final static Pattern citationsPattern = Pattern.compile("Cited by \\s*(\\d+)");
    
    public static int getYearlyCitations(String title, int publicationYear) {
        int citations = getCitations(title);
        if (citations < 0 || publicationYear < 1900)
            return citations;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        double passedYears = currentYear - publicationYear + 1;
        if (passedYears == 0)
            return citations;
        int average = (int) Math.round(citations / passedYears);
        return average;
    }
    
    public static int getCitations(String title) {
        try {
            String url = SCHOLAR_URL + title;
            String html = toString(connect(new URL(url)));
            Matcher matcher = citationsPattern.matcher(html);
            if (matcher.find())
                return Integer.parseInt(matcher.group(1));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public static InputStream connect(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        String myCookie = "GSP=ID=bc97fd2103a97010:IN=88119b4bc736c413+eda666da4771d016:CF=4";
        connection.setRequestProperty("Cookie", myCookie);
        connection.setRequestProperty("User-Agent",
                "Mozilla/6.0 (Windows NT 5.1; en-US; rv:x.x.x) Gecko/20041109 Firefox/x.x");
        return connection.getInputStream();
    }
    
    public static String toString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }
    
}
