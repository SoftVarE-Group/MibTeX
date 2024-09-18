/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import de.mibtex.citationservice.CitationService;
import de.mibtex.export.*;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static String PREPRINTS_DIR = "";

    public static String COMMENTS_DIR = "";

    public static String PDF_DIR = "";

    public static List<String> TAGS = new ArrayList<String>();

    public static List<String> FILTERTAGS = new ArrayList<String>();

    private static boolean cleanOutputDir;

    private static boolean citationServiceActive;

    private static String format = "HTML";

    public static String CITATION_DIR;

    /**
     * @param args array containing path to ini file
     */
    public static void main(String[] args) {
        final Path iniPath = Paths.get(
          args.length == 0 ? "options.ini"
                           : args[0]
        );

        final Ini ini;
        try {
            ini = Ini.fromFile(iniPath);
        } catch (IOException e) {
            System.err.println("Error when parsing ini file: " + iniPath);
            e.printStackTrace(System.err);
            return;
        }

        BIBTEX_DIR = ini.get("bibtex-dir");
        MAIN_DIR = ini.get("main-dir");
        OUTPUT_DIR = MAIN_DIR + ini.get("out-dir-rel");
        PDF_DIR = MAIN_DIR + ini.get("pdf-dir");
        COMMENTS_DIR = MAIN_DIR + ini.get("comment-dir");
        PREPRINTS_DIR = ini.get("preprints-dir");
        COMMENTS_DIR_REL = ini.get("comment-dir-rel");
        try {
        	String[] tagArray = ini.get("tags").split(",");
        	TAGS.addAll(Arrays.asList(tagArray));
        } catch (Exception e) {}
        try {
	        String[] filterTagArray = ini.get("filter-tags").split(",");
	        FILTERTAGS.addAll(Arrays.asList(filterTagArray));
        } catch (Exception e) {}
        try {
        	cleanOutputDir = Ini.parseBool(ini.get("clean"));
        } catch (Exception e) {}
        try {
        	citationServiceActive = Ini.parseBool(ini.get("citation-service"));
        } catch (Exception e) {}
        String citationDir = ini.get("citation-dir");
        if (citationDir == null || citationDir.isEmpty()) {
            CITATION_DIR = BIBTEX_DIR;
        } else {
            CITATION_DIR = citationDir;
        }
        format = ini.get("out-format");

        try {
            if (citationServiceActive && !"Citations".equalsIgnoreCase(format)) {
                new BibtexViewer("Citations");
            }
            else if (format != null) {
                new BibtexViewer(format);
            }
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
            case "SAMPLING_LATEX":
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
        exporter.writeDocument();
        Export.renameFiles(false);
        Export.renameFiles(true);
        if (citationServiceActive) {
            CitationService.main(new String[] {BibtexViewer.CITATION_DIR});
        }
    }
}
