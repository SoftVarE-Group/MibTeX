/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.text.DateFormat;

/**
 * A class to store and update a singly entry of the citation file.
 * 
 * @author Christopher Sontag, Thomas Thuem
 */
public class CitationEntry implements Comparable<CitationEntry> {
    
    private static final String UNKNOWN = "unknown";
    
    public final static int UNINITIALIZED = -1;
    
    public final static int NOT_FOUND = -2;
    
    public final static int PROBLEM_OCCURED = -3;
    
    public final static int ROBOT = -4;
    
    public final static int NOT_IN_CITATION_SERVICE = -5;
    
    private String key = UNKNOWN;
    
    private String title = UNKNOWN;
    
    private int citations = UNINITIALIZED;
    
    private long lastUpdate = 0;
    
    public CitationEntry(String key, String title) {
        super();
        this.key = key;
        this.title = title;
    }
    
    public CitationEntry(String key, String title, int citations, long lastUpdate) {
        super();
        this.key = key;
        this.title = title;
        this.citations = citations;
        this.lastUpdate = lastUpdate;
    }
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public int getCitations() {
        return citations;
    }
    
    public void setCitations(int citations) {
        this.citations = citations;
    }
    
    public boolean updateCitations() {
    	int citationsTemp = 0;
        System.out.println("Updating the citations of " + key + " with title \"" + getTitle() + "\"...");
        System.out.println("\told citations: " + citations + "   old timestamp: " + getLastUpdateString());
        try {
            citationsTemp = ScholarCitations.getCitations(title);
            if (this.citations > 0 && citationsTemp < 0) {
                System.out.println("\t" + this.key
                        + ": Has an old citation count, but now an error occurres");
            }
            else if (citationsTemp != ROBOT) {
            	this.citations = citationsTemp;
            }
        } catch (Exception e) {
            this.citations = PROBLEM_OCCURED;
            e.printStackTrace();
        }
        this.lastUpdate = System.currentTimeMillis();
        System.out.println("\tnew citations: " + citations + "   new timestamp: " + getLastUpdateString());
        if (citationsTemp != ROBOT) {
        	return true;
        } else {
        	return false;
        }
    }
    
    private String getLastUpdateString() {
        return DateFormat.getInstance().format(lastUpdate);
    }
    
    public long getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(long last_update) {
        this.lastUpdate = last_update;
    }
    
    public String getCSVString() {
        StringBuilder out = new StringBuilder();
        out.append("\"" + getKey() + "\";");
        out.append("\"" + getTitle() + "\";");
        out.append(getCitations() + ";");
        out.append(getLastUpdate() + ";");
        out.append(System.getProperty("line.separator"));
        return out.toString();
    }
    
    @Override
    public int compareTo(CitationEntry entry) {
        return getKey().compareTo(entry.getKey());
    }
    
    @Override
    public boolean equals(Object v) {
        boolean value = false;
        if (v instanceof CitationEntry) {
            CitationEntry entry = (CitationEntry) v;
            value = (getKey().equals(entry.getKey()) && getTitle().equals(entry.getTitle()));
        }
        return value;
    }
    
    @Override
    public int hashCode() {
        return getKey().hashCode() * getTitle().hashCode();
    }
    
    public static CitationEntry getFromCSV(String csv) {
        String[] str = csv.split(";");
        String key = replaceCSVSpeficics(str[0]);
        String title = replaceCSVSpeficics(str[1]);
        int citations = Integer.parseInt(str[2]);
        long lastUpdate = Long.parseLong(str[3]);
        return new CitationEntry(key, title, citations, lastUpdate);
    }
    
    private static String replaceCSVSpeficics(String str) {
        return str.replace("\"", "");
    }
    
}
