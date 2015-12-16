/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import de.mibtex.export.Export;
import de.mibtex.export.ExportCSV;
import de.mibtex.export.ExportHTML;
import de.mibtex.export.ExportJSON;

/**
 * A class to export a given BibTeX file to another format
 * 
 * @author Thomas Thuem, Christopher Sontag
 */
public class BibtexViewer {
    
    public static String BIBTEX_DIR;
    
    public static String MAIN_DIR;
    
    public static String HTML_DIR;
    
    public static String PDF_DIR_REL;
    
    public static String PDF_DIR;
    
    public static String TAGS;
    
    /**
     * Example arguments
     * 
     * BibtexViewer "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * "C:\\Users\\tthuem\\Dropbox\\Literatur\\" "HTML\\" "..\\Library\\"
     * "Library\\" "tt-tags" "CSV/JSON/HTML" "true/false"
     * 
     * @param args array containing path to Bibtex file path to main directory
     *        relative path of the HTML to main directory relative path of PDF
     *        files to the HTML folder (for linking files in HTML) relative path
     *        of PDF files to main directory name of the tag containing your
     *        keywords format for export (CSV/JSON/HTML) boolean for the
     *        background citation service
     */
    public static void main(String[] args) {
        BIBTEX_DIR = args[0];
        MAIN_DIR = args[1];
        HTML_DIR = MAIN_DIR + args[2];
        PDF_DIR_REL = args[3];
        PDF_DIR = MAIN_DIR + args[4];
        TAGS = args[5];
        String format = "HTML";
        try {
            format = args[6];
        } catch (Exception e) {
            System.out
                    .println("Exportformat Parameter not recognized. Setting Exportformat to HTML");
        }
        boolean citationService = false;
        try {
            citationService = Boolean.parseBoolean(args[6]);
        } catch (Exception e) {
            System.out.println("Citationservice Parameter not recognized. Service will not run!");
        }
        try {
            if (format != null)
                new BibtexViewer(format);
            if (citationService) {
                ScholarService scholarService = new ScholarService();
                scholarService.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public BibtexViewer(String format) throws Exception {
        Export exporter = null;
        switch (format) {
            case "CSV":
                exporter = new ExportCSV("literature.bib");
                break;
            case "JSON":
                exporter = new ExportJSON("literature.bib");
                break;
            case "HTML":
            default:
                exporter = new ExportHTML("literature.bib");
        }
        exporter.cleanOutputFolder();
        // exporter.printMissingPDFs();
        // exporter.renameFiles();
        exporter.writeDocument();
    }
}
