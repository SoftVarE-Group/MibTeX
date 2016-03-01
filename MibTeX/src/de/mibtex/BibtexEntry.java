/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;

/**
 * A class storing a single BibTeX entry with several options for manipulation.
 * 
 * @author Thomas Thuem, Christopher Sontag
 */
public class BibtexEntry {
    
    private static final String UNKNOWN = "unknown";
    
    public static final Key KEY_TT_TAGS = new Key(BibtexViewer.TAGS);
    
    public BibTeXEntry entry = null;
    
    public String key = UNKNOWN;
    
    public String author = UNKNOWN;
    
    public List<String> authorList = new ArrayList<String>();
    
    public String title = UNKNOWN;
    
    public String venue = UNKNOWN;
    
    public String tags = UNKNOWN;
    
    public List<String> tagList = new ArrayList<String>();
    
    public int year = 0;
    
    public int citations = 0;
    
    public long lastUpdate = 0;
    
    
    public BibtexEntry(BibTeXEntry entry) {
        this.entry = entry;
        parseKey();
        parseAuthor();
        parseTitle();
        parseVenue();
        parseYear();
        parseTags();
    }
    
    public BibtexEntry(String key, String author, String title, String venue, String tags,
            int year, int citations) {
        this.key = key;
        this.author = author;
        this.title = title;
        this.venue = venue;
        this.tags = tags;
        this.year = year;
        this.citations = citations;
        parseAuthor();
        parseTags();
    }
    
    public int getCitations() {
        if (citations != 0)
            return citations;
        return citations;
    }
    
    public File getPDF() {
        String pdf = getLastname(authorList.get(0));
        if (authorList.size() == 2)
            pdf += " and " + getLastname(authorList.get(1));
        else if (authorList.size() > 2)
            pdf += " et al.";
        if (!venue.startsWith("("))
            pdf += " " + venue;
        if (year > 0)
            pdf += " " + year;
        pdf += " " + title;
        pdf += ".pdf";
        return new File(BibtexViewer.PDF_DIR + toURL(pdf));
    }
    
    public String getPDFPath() {
        String pdf = getLastname(authorList.get(0));
        if (authorList.size() == 2)
            pdf += " and " + getLastname(authorList.get(1));
        else if (authorList.size() > 2)
            pdf += " et al.";
        if (!venue.startsWith("("))
            pdf += " " + venue;
        if (year > 0)
            pdf += " " + year;
        pdf += " " + title;
        pdf += ".pdf";
        return BibtexViewer.PDF_DIR.replace("\\", "/") + toURL(pdf);
    }
    
    private String getLastname(String name) {
        return name.substring(name.lastIndexOf(" ") + 1);
    }
    
    void parseKey() {
        try {
            if (key.equals(UNKNOWN)) {
                key = entry.getKey().getValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void parseAuthor() {
        try {
            if (author.equals(UNKNOWN)) {
                try {
                    author = entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString();
                } catch (Exception e) {
                    author = entry.getField(BibTeXEntry.KEY_EDITOR).toUserString();
                }
            }
            author = replaceUmlauts(author);
            author = author.replace(" and ", "; ");
            while (author.contains(",")) {
                int comma = author.indexOf(",");
                int before = author.lastIndexOf("; ", comma) + 2;
                if (before < 2)
                    before = 0;
                int after = author.indexOf("; ", comma);
                if (after < 0)
                    after = author.length();
                author = author.substring(0, before) + author.substring(comma + 1, after) + " "
                        + author.substring(before, comma) + author.substring(after);
            }
            author = author.replace("; ", ", ");
            StringTokenizer tokenizer = new StringTokenizer(author, ",");
            while (tokenizer.hasMoreTokens())
                authorList.add(tokenizer.nextToken().trim());
        } catch (Exception e) {
            if (authorList.equals(UNKNOWN))
                authorList.add(author);
            e.printStackTrace();
        }
    }
    
    void parseTitle() {
        try {
            if (title.equals(UNKNOWN)) {
                title = entry.getField(BibTeXEntry.KEY_TITLE).toUserString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        title = replaceUmlauts(title);
    }
    
    void parseVenue() {
        if (venue.equals(UNKNOWN)) {
            venue = "(" + entry.getType().getValue() + ")";
            if (venue.equalsIgnoreCase("(incollection)"))
                return;
            try {
                venue = entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString();
            } catch (Exception e) {
            }
            try {
                venue = entry.getField(BibTeXEntry.KEY_JOURNAL).toUserString();
            } catch (Exception e) {
            }
        }
    }
    
    void parseYear() {
        if (year == 0) {
            try {
                String yearString = entry.getField(BibTeXEntry.KEY_YEAR).toUserString();
                year = Integer.parseInt(yearString);
            } catch (Exception e) {
            }
        }
    }
    
    void parseTags() {
        try {
            if (tags.equals(UNKNOWN)) {
                tags = entry.getField(KEY_TT_TAGS).toUserString();
            }
            tags = replaceUmlauts(tags);
            StringTokenizer tokenizer = new StringTokenizer(tags, ",");
            while (tokenizer.hasMoreTokens())
                tagList.add(tokenizer.nextToken().trim());
        } catch (Exception e) {
        }
        if (tagList.isEmpty())
            tagList.add("(none)");
    }
    
    public static String replaceUmlauts(String s) {
        s = s.replace("\\\"{", "{\\\"");
        s = s.replace("\\\"a", "&auml;");
        s = s.replace("\\\"o", "&ouml;");
        s = s.replace("\\\"u", "&uuml;");
        s = s.replace("\\\"A", "&Auml;");
        s = s.replace("\\\"O", "&Ouml;");
        s = s.replace("\\O", "Ø");
        s = s.replace("\\\"U", "&Uuml;");
        s = s.replace("\\ss", "&szlig;");
        s = s.replace("\\&\\#536;", "S");
        s = s.replace("\\&", "&amp;");
        s = s.replace("\\#", "#");
        s = s.replace("\\c", "");
        s = s.replace("\\v", "");
        s = s.replace("\\u", "");
        s = s.replace("\\l", "l");
        s = s.replace("\\i", "i");
        s = s.replace("\\k", "");
        s = s.replace("\\^", "");
        s = s.replace("\\'", "");
        s = s.replace("\\`", "");
        s = s.replace("\\,", " ");
        s = s.replace("\\~", "");
        s = s.replace("\\\"", "");
        s = s.replace("---", "&#8211;");
        s = s.replace("--", "&#8211;");
        s = s.replace("{", "");
        s = s.replace("}", "");
        s = s.replaceAll("\\s+", " ");
        return s;
    }
    
    public static String toURL(String s) {
        s = s.replace("&auml;", "ae");
        s = s.replace("&ouml;", "oe");
        s = s.replace("&uuml;", "ue");
        s = s.replace("&Auml;", "Ae");
        s = s.replace("&Ouml;", "Oe");
        s = s.replace("Ø", "O");
        s = s.replace("&Uuml;", "Ue");
        s = s.replace("&szlig;", "ss");
        s = s.replace("&amp;", "and");
        s = s.replace("&#8211;", "-");
        s = s.replace(":", "");
        s = s.replace("?", "");
        s = s.replace("\\", "");
        s = s.replace("/", "");
        s = s.replace("#", "");
        return s;
    }
    
    @Override
    public String toString() {
        return "BibtexEntry [entry=" + entry + ", key=" + key + ", author=" + author
                + ", authorList=" + authorList + ", title=" + title + ", venue=" + venue
                + ", tags=" + tags + ", tagList=" + tagList + ", year=" + year + ", citations="
                + citations + "]";
    }
    
}
