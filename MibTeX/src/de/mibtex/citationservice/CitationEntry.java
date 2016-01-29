package de.mibtex.citationservice;

public class CitationEntry {
    
    private static final String UNKNOWN = "unknown";
    
    // Should wait at least 24h before updating citations
    private static final long timeToWait = 24 * 60 * 60;
    
    private String key = UNKNOWN;
    
    private String title = UNKNOWN;
    
    private int citations = 0;
    
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
    
    public int updateCitations() {
        if ((System.currentTimeMillis()) > this.lastUpdate + CitationEntry.timeToWait) {
            System.out.println("The citations of "+this.key+" are outdated! Start Update");
            this.citations = ScholarCitations.getCitations(title.replace(" ", "%20"));
            this.lastUpdate = System.currentTimeMillis();
        } else {
            System.out.println("The citations of "+this.key+" are up to date");
        }
       
        return this.citations;
    }
    
    public long getLastUpdate() {
        return lastUpdate;
    }
    
    public void setLastUpdate(long last_update) {
        this.lastUpdate = last_update;
    }
    
}
