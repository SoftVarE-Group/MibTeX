/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import de.mibtex.export.*;
import org.ini4j.Ini;
import org.ini4j.Wini;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A class to export a given BibTeX file to another format
 *
 * @author Thomas Thuem, Christopher Sontag, Paul Bittner
 */
public class BibtexViewer {

    public static String BIBTEX_DIR = "";

    public static String MAIN_DIR = "";

    public static String OUTPUT_DIR = "";

    public static String COMMENTS_DIR_REL = "";

    public static String PDF_DIR_REL = "";

    public static String PREPRINTS_DIR = "";
    
    public static String SLIDES_DIR = "";

    public static String COMMENTS_DIR = "";

    public static String PDF_DIR = "";

    public static List<String> TAGS = new ArrayList<String>();

    private static boolean cleanOutputDir;

    private static boolean updateCitations;

    private static String format = "HTML";

    public static String CITATION_DIR;

    /**
     * Example arguments
     * <p>
     * BibtexViewer "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     * "C:\\Users\\tthuem\\Dropbox\\Literatur\\" "HTML\\" "..\\Library\\"
     * "Library\\" "tt-tags" "CSV/JSON/HTML/Classification" "true" "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
     *
     * @param args array containing:
     *             - path to Bibtex file path to main directory
     *             - relative path of the HTML to main directory
     *             - relative path of PDF files to the HTML folder (for linking files in HTML)
     *             - relative path of PDF files to main directory
     *             - list of the tag containing your keywords (format: "tag1,tag2,tag3")
     *             - format for export (CSV/JSON/HTML)
     *             - boolean for output cleaning (default: false)
     *             - boolean for update citations file (default:true)
     *             - path to citations file (default: Bibtex file path)
     */
    public static void main(String[] args) {
        if (args.length <= 1) {
            String configurationFile;
            if (args.length == 0)
                configurationFile = "options.ini";
            else {
                configurationFile = args[0];
            }
            File iniFile = new File(configurationFile);
            if (iniFile.exists()) {
                Ini ini = null;
                if (System.getProperty("os.name").contains("Windows")) {
                    try {
                        ini = new Wini(iniFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        ini = new Ini(iniFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (ini != null) {
                    BIBTEX_DIR = ini.get("options", "bibtex-dir");
                    MAIN_DIR = ini.get("options", "main-dir");
                    OUTPUT_DIR = MAIN_DIR + ini.get("options", "out-dir-rel");
                    PDF_DIR = MAIN_DIR + ini.get("options", "pdf-dir");
                    PDF_DIR_REL = ini.get("options", "pdf-dir-rel");
                    COMMENTS_DIR = MAIN_DIR + ini.get("options", "comment-dir");
                    PREPRINTS_DIR = ini.get("options", "preprints-dir");
                    SLIDES_DIR = ini.get("options", "slides-dir");
                    COMMENTS_DIR_REL = ini.get("options", "comment-dir-rel");
                    String[] tagArray = ini.get("options", "tags").split(",");
                    TAGS.addAll(Arrays.asList(tagArray));
                    cleanOutputDir = ini.get("options", "clean", Boolean.class);
                    updateCitations = ini.get("options", "citationService", Boolean.class);
                    String citationDir = ini.get("options", "citation-dir");
                    if (citationDir == null || citationDir.isEmpty()) {
                        CITATION_DIR = BIBTEX_DIR;
                    } else {
                        CITATION_DIR = citationDir;
                    }
                    format = ini.get("options", "out-format");
                } else {
                    System.out.println("Ini file reader is null!");
                    System.exit(0);
                }
            } else {
                System.out.println("Options file not found under: " + iniFile.getName());
            }
        } else {
            // TODO: Does not support "preprints-dir" nor "slides-dir" should this be deprecated?
            BIBTEX_DIR = args[0];
            MAIN_DIR = args[1];
            OUTPUT_DIR = MAIN_DIR + args[2];
            PDF_DIR_REL = args[3];
            PDF_DIR = MAIN_DIR + args[4];
            String[] tagArray = args[5].split(",");
            Collections.addAll(TAGS, tagArray);

            try {
                cleanOutputDir = Boolean.getBoolean(args[7]);
            } catch (Exception e) {
                System.out
                        .println("Output will not be cleaned");
                cleanOutputDir = false;
            }
            try {
                updateCitations = Boolean.getBoolean(args[8]);
            } catch (Exception e) {
                System.out
                        .println("Citations are going to be updated");
                updateCitations = true;
            }
            try {
                CITATION_DIR = args[9];
            } catch (Exception e) {
                System.out
                        .println("Citation is saved in Bibtex directory");
                CITATION_DIR = BIBTEX_DIR;
            }
            try {
                format = args[6];
            } catch (Exception e) {
                System.out
                        .println("Exportformat Parameter not recognized. Setting Exportformat to HTML");
            }
        }
        try {
            if (updateCitations && !"Citations".equals(format)) {
                new BibtexViewer("Citations");
            }
            if (format != null)
                new BibtexViewer(format);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BibtexViewer(String format) throws Exception {
        Export exporter = null;
        switch (format.toUpperCase()) {
            case "CSV":
                exporter = new ExportCSV(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "JSON":
                exporter = new ExportJSON(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "CITATIONS":
                exporter = new ExportCitations(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "CONFLICTS":
                exporter = new ExportConflicts(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "CLASSIFICATION":
                exporter = new ExportClassification(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "SAMPLING":
                exporter = new ExportSampling(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "SAMPLING LATEX":
                exporter = new ExportSamplingLatex(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "HTML_NEW":
                exporter = new ExportNewHTML(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "FIND_PDFS":
                exporter = new ExportFindPDFs(BibtexViewer.BIBTEX_DIR, "literature.bib");
                break;
            case "TYPO3":
            	exporter = new ExportTypo3Bibtex(BibtexViewer.BIBTEX_DIR, "literature.bib");
            	break;
            case "HTML":
            default:
                exporter = new ExportHTML(BibtexViewer.BIBTEX_DIR, "literature.bib");
        }
        if (cleanOutputDir) {
            Export.cleanOutputFolder();
        }
        // exporter.printMissingPDFs();
        // exporter.renameFiles();
        exporter.writeDocument();
    }
}
