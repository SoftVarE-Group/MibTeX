/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

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
    
    private String key = UNKNOWN;
    
    private String title = UNKNOWN;
    
    private int citations = UNINITIALIZED;
    
    private long lastUpdate = 0;
    
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
    
    public void updateCitations() {
        System.out.println("Updating the citations of " + key + " with title \"" + getTitle() + "\"...");
        System.out.println("\told citations: " + citations + "   old timestamp: " + lastUpdate);
        try {
			this.citations = ScholarCitations.getCitations(title.replace(" ", "%20"));
		} catch (Exception e) {
			this.citations = PROBLEM_OCCURED;
			e.printStackTrace();
		}
        this.lastUpdate = System.currentTimeMillis();
        System.out.println("\tnew citations: " + citations + "   new timestamp: " + lastUpdate);
    }
    
    public long getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(long last_update) {
        this.lastUpdate = last_update;
    }

    @Override
    public int compareTo(CitationEntry entry) {
        return getKey().compareTo(entry.getKey());
    }
    
}
