/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class that reads all BibTeX entries from a .csv file, gets the citations and put the entries back
 * in the .csv file
 * 
 * @author Christopher Sontag, Thomas Thuem
 */
public class ScholarService extends Thread {
    
    private static final String CITATIONS_FILE = "citations.csv";
    
	private Random rand = new Random();
    
    @Override
    public void run() {
        while (true) {
            List<CitationEntry> entries = readFromFile(CITATIONS_FILE);
            CitationEntry entry = nextEntry(entries);
            entry.updateCitations();
            System.out.println(entry.getCitations() + " citations of " + entry.getKey() + " with title \"" + entry.getTitle() + "\"");
            writeToFile(CITATIONS_FILE, entries);
            try {
                sleep(rand.nextInt(120000) + 120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected CitationEntry nextEntry(List<CitationEntry> entries) {
    	CitationEntry next = entries.get(0);
    	for (CitationEntry entry : entries)
            if (entry.getLastUpdate() < next.getLastUpdate())
            	next = entry;
		return next;
    }
    
    protected List<CitationEntry> readFromFile(String filename) {
        List<CitationEntry> entries = new ArrayList<CitationEntry>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(CitationService.CITATION_DIR
                +"\\"+ filename)))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] str = line.split(";");
                String key = replaceCSVSpeficics(str[0]);
                String title = replaceCSVSpeficics(str[1]);
                int citations = Integer.parseInt(str[2]);
                long lastUpdate = Long.parseLong(str[3]);
                entries.add(new CitationEntry(key, title, citations, lastUpdate));
            }
        } catch (IOException e) {
            System.out.println("IOException for " + filename);
            return entries;
        }
        return entries;
    }
    
    public String replaceCSVSpeficics(String str) {
        return str.replace("\"", "");
    }
    
    protected void writeToFile(String filename, List<CitationEntry> entries) {
        try {
            File file = new File(CitationService.CITATION_DIR +"\\"+ filename);
            System.out.println("Updating " + filename);
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            for (CitationEntry entry : entries) {
                out.append("\"" + entry.getKey() + "\";");
                out.append("\"" + entry.getTitle() + "\";");
                out.append(entry.getCitations() + ";");
                out.append(entry.getLastUpdate() + ";");
                out.append(System.getProperty("line.separator"));
            }
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println("Not found " + filename);
        } catch (IOException e) {
            System.out.println("IOException for " + filename);
        }
    }
    
}
