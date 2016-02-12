/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import de.mibtex.export.Export;
import de.mibtex.export.ExportCSV;
import de.mibtex.export.ExportCitations;
import de.mibtex.export.ExportHTML;
import de.mibtex.export.ExportJSON;
import de.mibtex.export.ExportNewHTML;

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
    
    private static boolean cleanOutputDir;
    
    public static String CITATION_DIR; 
    
    /**
     * Example arguments
     * 
     * BibtexViewer "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * "C:\\Users\\tthuem\\Dropbox\\Literatur\\" "HTML\\" "..\\Library\\"
     * "Library\\" "tt-tags" "CSV/JSON/HTML" "true" "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * 
     * @param args array containing:
     *          - path to Bibtex file path to main directory
     *          - relative path of the HTML to main directory 
     *          - relative path of PDF files to the HTML folder (for linking files in HTML) 
     *          - relative path of PDF files to main directory 
     *          - name of the tag containing your keywords 
     *          - format for export (CSV/JSON/HTML) 
     *          - boolean for output cleaning (default: false)
     *          - path to citations file (default: Bibtex file path
     */
    public static void main(String[] args) {
        BIBTEX_DIR = args[0];
        MAIN_DIR = args[1];
        HTML_DIR = MAIN_DIR + args[2];
        PDF_DIR_REL = args[3];
        PDF_DIR = MAIN_DIR + args[4];
        TAGS = args[5];
        try {
            cleanOutputDir = Boolean.getBoolean(args[7]);
        } catch (Exception e) {
            System.out
            .println("Output will not be cleaned");
            cleanOutputDir = false;
        }
        try {
            CITATION_DIR = args[8];
        } catch (Exception e) {
            System.out
            .println("Citation is saved in Bibtex directory");
            CITATION_DIR = BIBTEX_DIR;
        }
        System.out.println(CITATION_DIR);
        String format = "HTML";
        try {
            format = args[6];
        } catch (Exception e) {
            System.out
                    .println("Exportformat Parameter not recognized. Setting Exportformat to HTML");
        }
        try {
            if (format != null)
                new BibtexViewer(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public BibtexViewer(String format) throws Exception {
        Export exporter = null;
        switch (format) {
            case "CSV":
                exporter = new ExportCSV(BibtexViewer.BIBTEX_DIR,"literature.bib");
                break;
            case "JSON":
                exporter = new ExportJSON(BibtexViewer.BIBTEX_DIR,"literature.bib");
                break;
            case "Citations":
                exporter = new ExportCitations(BibtexViewer.BIBTEX_DIR,"literature.bib");
                break;
            case "HTML_NEW":
                exporter = new ExportNewHTML(BibtexViewer.BIBTEX_DIR,"literature.bib");
                break;
            case "HTML":
            default:
                exporter = new ExportHTML(BibtexViewer.BIBTEX_DIR,"literature.bib");
        }
        if (cleanOutputDir) {
            exporter.cleanOutputFolder();
        }
        // exporter.printMissingPDFs();
        // exporter.renameFiles();
        exporter.writeDocument();
    }
}
