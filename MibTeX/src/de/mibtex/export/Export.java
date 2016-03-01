/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXObject;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;
import org.jbibtex.ParseException;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexFilter;
import de.mibtex.BibtexViewer;
import de.mibtex.Levenshtein;
import de.mibtex.citationservice.CitationEntry;

/**
 * A abstract class that implements often used methods for the exporters
 * 
 * @author Thomas Thuem
 */
public abstract class Export {
    
    protected static LinkedHashMap<String, BibtexEntry> entries;
    
    protected static List<String> authors;
    
    protected static List<String> titles;
    
    protected static List<Integer> years;
    
    protected static List<String> venues;
    
    protected static List<String> tags;
    
    public Export(String path, String file) throws Exception {
        Reader reader = null;
        try {
            reader = new FileReader(new File(path + file));
            BibTeXParser parser = new BibTeXParser() {
                @Override
                public void checkStringResolution(Key key, BibTeXString string) {
                }
                
                @Override
                public void checkCrossReferenceResolution(Key key, BibTeXEntry entry) {
                }
            };
            BibTeXDatabase database = parser.parse(reader);
            extractEntries(database);
        } catch (FileNotFoundException e) {
            System.out.println("BibTeX-File not found under " + file);
        } catch (IOException e) {
            System.out.println("BibTeXParser has an IOExeption");
        } catch (ParseException e) {
            System.out.println("BibTeX-File cannot be parsed");
        } finally {
            reader.close();
        }
        readAuthors();
        readTitles();
        readYears();
        readVenues();
        readTags();
    }
    
    private void extractEntries(BibTeXDatabase database) {
        entries = new LinkedHashMap<String, BibtexEntry>();
        for (BibTeXObject object : database.getObjects()) {
            if (object instanceof BibTeXEntry) {
                BibtexEntry bibtexEntry = new BibtexEntry((BibTeXEntry) object);
                entries.put(bibtexEntry.key,bibtexEntry);
            }
        }
        readCitations();
    }
    
    private void readCitations() {
        List<CitationEntry> citationsEntries = new ArrayList<CitationEntry>();
        File fileHandle = new File(BibtexViewer.CITATION_DIR,"citations.csv");
        if (fileHandle.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileHandle))) {
                for (String line; (line = br.readLine()) != null;) {
                    citationsEntries.add(CitationEntry.getFromCSV(line));
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            for (CitationEntry citationEntry: citationsEntries) {
                if (entries.containsKey(citationEntry.getKey())) {
                    BibtexEntry bibtexEntry = entries.get(citationEntry.getKey());
                    bibtexEntry.citations = citationEntry.getCitations();
                    bibtexEntry.lastUpdate = citationEntry.getLastUpdate();
                }
            }
        }
        
    }
    
    private void readAuthors() {
        authors = new ArrayList<String>();
        for (BibtexEntry entry : entries.values())
            for (String author : entry.authorList)
                if (!authors.contains(author))
                    authors.add(author);
        Collections.sort(authors);
    }
    
    private void readTitles() {
        titles = new ArrayList<String>();
        for (BibtexEntry entry : entries.values())
            titles.add(entry.title);
        Collections.sort(titles);
    }
    
    private void readYears() {
        years = new ArrayList<Integer>();
        for (BibtexEntry entry : entries.values())
            if (!years.contains(entry.year))
                years.add(entry.year);
        Collections.sort(years);
    }
    
    private void readVenues() {
        venues = new ArrayList<String>();
        for (BibtexEntry entry : entries.values())
            if (!venues.contains(entry.venue))
                venues.add(entry.venue);
        Collections.sort(venues);
    }
    
    private void readTags() {
        tags = new ArrayList<String>();
        for (BibtexEntry entry : entries.values())
            for (String tag : entry.tagList)
                if (!tags.contains(tag))
                    tags.add(tag);
        Collections.sort(tags);
    }
    
    public void printMissingPDFs() {
        for (BibtexEntry entry : entries.values()) {
            File file = entry.getPDF();
            if (!file.exists())
                System.out.println(file.getName());
        }
    }
    
    public void renameFiles() {
        List<File> available = new ArrayList<File>();
        List<BibtexEntry> missing = new ArrayList<BibtexEntry>();
        try {
            for (File file : new File(BibtexViewer.PDF_DIR).listFiles())
                available.add(file);
        } catch (NullPointerException e) {
            System.out.println("No PDFs in " + BibtexViewer.PDF_DIR);
        }
        for (BibtexEntry entry : entries.values()) {
            File file = entry.getPDF();
            if (file.exists()) {
                if (!available.remove(file))
                    System.err.println("File comparison failed: " + file);
            } else {
                if (!"misc book".contains(entry.entry.getType().getValue()))
                    missing.add(entry);
            }
        }
        System.out.println("Correct = " + (entries.size() - missing.size()) + ", Available = "
                + available.size() + ", Missing = " + missing.size());
        System.out.println();
        Scanner answer = new Scanner(System.in);
        while (!available.isEmpty()) {
            int minDistance = Integer.MAX_VALUE;
            BibtexEntry missingEntry = null;
            File availableFile = null;
            for (BibtexEntry entry : missing) {
                for (File file : available) {
                    int distance = Levenshtein
                            .getDistance(file.getName(), entry.getPDF().getName());
                    if (distance < minDistance) {
                        minDistance = distance;
                        missingEntry = entry;
                        availableFile = file;
                    }
                }
            }
            // stop if names are too different from each other
            if (minDistance > availableFile.getName().length() * 0.7)
                break;
            if (availableFile != null) {
                System.out.println("Available: " + availableFile.getName());
                System.out.println("Missing: " + missingEntry.getPDF().getName());
                System.out.println("Key: " + missingEntry.entry.getKey().getValue());
                System.out.println("Distance: " + minDistance);
                System.out.println("Remaining: " + missing.size());
                if (answer.next().equals("y")) {
                    if (availableFile.renameTo(missingEntry.getPDF()))
                        available.remove(availableFile);
                    else
                        System.err.println("Renaming from \"" + availableFile.getAbsolutePath()
                                + "\" to \"" + missingEntry.getPDF() + "\" did not succeed!");
                }
                missing.remove(missingEntry);
            }
        }
        answer.close();
        System.out.println();
        for (File file : available) {
            if (!file.getName().startsWith("0"))
                file.renameTo(new File(file.getParentFile(), "0" + file.getName()));
            System.out.println("Available: " + file.getName());
        }
    }
    
    public void cleanOutputFolder() {
        for (File file : new File(BibtexViewer.HTML_DIR).listFiles())
            file.delete();
    }
    
    protected long countEntries(BibtexFilter filter) {
        long number = 0;
        for (BibtexEntry entry : entries.values())
            if (filter.include(entry))
                number++;
        return number;
    }
    
    protected String readFromFile(String path, File filename) {
        try {
            InputStream in = new FileInputStream(path + filename);
            StringBuilder out = new StringBuilder();
            byte[] b = new byte[4096];
            for (int n; (n = in.read(b)) != -1;) {
                out.append(new String(b, 0, n));
            }
            in.close();
            return out.toString();
        } catch (FileNotFoundException e) {
            System.out.println("Not Found " + filename);
        } catch (IOException e) {
            System.out.println("IOException for " + filename);
        }
        return "";
    }
    
    protected void writeToFile(String path, String filename, String content) {
        try {
            File file = new File(path + filename);
            String oldContent = readFromFile(path,new File(filename));
            if (!content.equals(oldContent)) {
                System.out.println("Updating " + filename);
                BufferedWriter out = new BufferedWriter(new FileWriter(file));
                out.write(content.toString());
                out.close();
//            } else {
//                System.out.println("Old content is the same! No update required!");
            }
        } catch (FileNotFoundException e) {
            System.out.println("Not Found " + filename);
        } catch (IOException e) {
            System.out.println("IOException for " + filename);
        }
    }
    
    public abstract void writeDocument();
}
