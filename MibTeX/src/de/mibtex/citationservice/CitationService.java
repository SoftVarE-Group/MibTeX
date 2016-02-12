/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.io.File;

/**
 * A class to export the citations from scholar for each BibTeX entry
 * 
 * @author Christopher Sontag
 */
public class CitationService {
    
    public static String CITATION_DIR;
    
    /**
     * Example arguments
     * 
     * CitationService "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * 
     * @param arg containing path to the citations.csv (default: BibTeX dir)
     */
    public static void main(String[] args) {
        CITATION_DIR = args[0];
        System.out.println(CITATION_DIR);
        try {
            File fileHandle = new File(CITATION_DIR + "/citations.csv");
            if (!fileHandle.exists()) {
                System.out.println("Not found " + CITATION_DIR + "citations.csv");
                return;
            }
            ScholarService service = new ScholarService();
            service.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
