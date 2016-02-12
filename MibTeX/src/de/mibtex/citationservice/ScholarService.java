/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;
/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.mibtex.BibtexEntry;

/**
 * A class that reads all BibTeX entries from a .csv file, gets the citations and put the entries back
 * in the .csv file
 * 
 * @author Christopher Sontag
 */
public class ScholarService extends Thread {
    
    private List<CitationEntry> entries;
    private Random rand;
    
    @Override
    public void run() {
        for (CitationEntry entry : entries) {
            entry.updateCitations();
            System.out.println(entry.getKey() + " is cited " + entry.getCitations());
            writeToFile("citations.csv", entries);
            try {
                sleep(rand.nextInt(120000) + 120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //writeToFile("citations.csv", entries);
    }
    
    public ScholarService() throws Exception {
        rand = new Random(120000);
        entries = readFromFile("citations.csv", ";");
        Collections.sort(entries);
    }
    
    protected List<CitationEntry> readFromFile(String filename, String delimeter) {
        List<CitationEntry> entries = new ArrayList<CitationEntry>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(CitationService.BIBTEX_DIR
                + filename)))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] str = line.split(delimeter);
                String key = replaceCSVSpeficics(str[0]);
                String title = replaceCSVSpeficics(str[1]);
                int citations = Integer.parseInt(str[2]);
                long lastUpdate = Long.parseLong(str[3]);
                
                entries.add(new CitationEntry(key, title, citations, lastUpdate));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            return entries;
        }
        return entries;
    }
    
    public String replaceCSVSpeficics(String str) {
        return str.replace("\"", "");
    }
    
    protected void writeToFile(String filename, List<CitationEntry> entries) {
        try {
            File file = new File(CitationService.BIBTEX_DIR + filename);
            List<CitationEntry> oldContent = readFromFile(filename, ";");
            if (!entries.equals(oldContent)) {
                System.out.println("Updating " + filename);
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                StringBuilder CSV = new StringBuilder();
                for (CitationEntry entry : entries) {
                    CSV.append("\"" + entry.getKey() + "\";").append("\"" + entry.getTitle() + "\";")
                            .append(entry.getCitations() + ";").append(entry.getLastUpdate() + ";")
                            .append(System.getProperty("line.separator"));
                }
                out.write(CSV.toString());
                out.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Not Found " + filename);
        } catch (IOException e) {
            System.out.println("IOException for " + filename);
        }
    }
    
}
