package de.mibtex.citationservice;

import java.io.File;

public class CitationService {
    
    public static String BIBTEX_DIR;
    
    public static String MAIN_DIR;
    
    public static String HTML_DIR;
    
    public static String PDF_DIR_REL;
    
    public static String PDF_DIR;
    
    /**
     * Example arguments
     * 
     * CitationService "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * "C:\\Users\\tthuem\\Dropbox\\Literatur\\" "HTML\\" "..\\Library\\"
     * "Library\\"
     * 
     * @param arg containing path to the file path to main directory relative
     *        path of the HTML to main directory relative path of PDF files to
     *        the HTML folder (for linking files in HTML) relative path of PDF
     *        files to main directory name of the tag containing your keywords
     *        format for export (CSV/JSON/HTML)
     */
    public static void main(String[] args) {
        BIBTEX_DIR = args[0];
        MAIN_DIR = args[1];
        HTML_DIR = MAIN_DIR + args[2];
        PDF_DIR_REL = args[3];
        PDF_DIR = MAIN_DIR + args[4];
        try {
            File fileHandle = new File(BIBTEX_DIR + "/citations.csv");
            if (!fileHandle.exists()) {
                System.out.println("Not found " + BIBTEX_DIR + "citations.csv");
                return;
            }
            ScholarService service = new ScholarService();
            service.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
