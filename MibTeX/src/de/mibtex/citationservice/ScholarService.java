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
    
	private static final int MIN_DELAY = 5;

	private static final int EXTRA_DELAY = 5;

	private Random rand = new Random();

	private File citationsFile;
    
    public ScholarService(File file) {
		citationsFile = file;
	}

	@Override
    public void run() {
        while (true) {
            List<CitationEntry> entries = readFromFile(citationsFile);
            CitationEntry entry = nextEntry(entries);
            entry.updateCitations();
            writeToFile(citationsFile, entries);
            try {
                sleep(rand.nextInt(MIN_DELAY*60000) + EXTRA_DELAY*60000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    protected CitationEntry nextEntry(List<CitationEntry> entries) {
    	CitationEntry next = entries.get(0);
    	for (CitationEntry entry : entries) {
            if (entry.getCitations() == CitationEntry.UNINITIALIZED)
            	return entry;
    		if (entry.getLastUpdate() < next.getLastUpdate())
            	next = entry;
    	}
		return next;
    }
    
    protected List<CitationEntry> readFromFile(File file) {
    	System.out.print("Reading " + file.getName() + "... ");
        List<CitationEntry> entries = new ArrayList<CitationEntry>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] str = line.split(";");
                String key = replaceCSVSpeficics(str[0]);
                String title = replaceCSVSpeficics(str[1]);
                int citations = Integer.parseInt(str[2]);
                long lastUpdate = Long.parseLong(str[3]);
                entries.add(new CitationEntry(key, title, citations, lastUpdate));
            }
        } catch (IOException e) {
            System.out.println("IOException for " + file.getAbsolutePath());
        }
    	System.out.println("done.");
        return entries;
    }
    
    public String replaceCSVSpeficics(String str) {
        return str.replace("\"", "");
    }
    
    protected void writeToFile(File file, List<CitationEntry> entries) {
    	System.out.print("Updating " + file.getName() + "... ");
        try {
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
            System.out.println("Not found " + file.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("IOException for " + file.getAbsolutePath());
        }
    	System.out.println("done.");
    }
    
}
