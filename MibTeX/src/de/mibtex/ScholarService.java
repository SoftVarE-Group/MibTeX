/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;
/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.mibtex.export.Export;
import de.mibtex.export.ExportCSV;

/**
 * A class that reads all BibTeX entries from a .csv file, gets the citations and put the entries back
 * in the .csv file
 * 
 * @author Christopher Sontag
 */
public class ScholarService extends Thread {
    
    private List<BibtexEntry> entries;
    private Random rand;
    
    @Override
    public void run() {
        for (BibtexEntry entry : entries) {
            entry.getCitations();
            System.out.println(entry.key + " is cited " + entry.citations);
            try {
                sleep(rand.nextInt(120000) + 120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        writeToFile("literature.csv", entries);
    }
    
    public ScholarService() throws Exception {
        rand = new Random(120000);
        Export exporter = new ExportCSV("literature.bib");
        exporter.writeDocument();
        entries = readFromFile("literature.csv", ";");
    }
    
    protected List<BibtexEntry> readFromFile(String filename, String delimeter)
            throws FileNotFoundException, IOException {
        List<BibtexEntry> entries = new ArrayList<BibtexEntry>();
        try (BufferedReader br = new BufferedReader(new FileReader(new File(BibtexViewer.HTML_DIR
                + filename)))) {
            for (String line; (line = br.readLine()) != null;) {
                String[] str = line.split(delimeter);
                String key = replaceCSVSpeficics(str[0]);
                String author = replaceCSVSpeficics(str[1]).replace(",", ";");
                String title = replaceCSVSpeficics(str[2]);
                String venue = replaceCSVSpeficics(str[3]);
                int year = Integer.parseInt(str[4]);
                int citations = Integer.parseInt(str[5]);
                String tags = replaceCSVSpeficics(str[6]);
                entries.add(new BibtexEntry(key, author, title, venue, tags, year, citations));
            }
        }
        return entries;
    }
    
    public String replaceCSVSpeficics(String str) {
        return str.replace("\"", "");
    }
    
    protected void writeToFile(String filename, List<BibtexEntry> entries) {
        try {
            File file = new File(BibtexViewer.HTML_DIR + filename);
            List<BibtexEntry> oldContent = readFromFile(filename, ";");
            if (!entries.equals(oldContent)) {
                System.out.println("Updating " + filename);
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                StringBuilder CSV = new StringBuilder();
                for (BibtexEntry entry : entries) {
                    CSV.append("\"" + entry.key + "\";").append("\"" + entry.author + "\";")
                            .append("\"" + entry.title + "\";").append("\"" + entry.venue + "\";")
                            .append(entry.year + ";").append(entry.citations + ";")
                            .append("\"" + entry.tags + "\"" + BibtexEntry.lineSeperator);
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
