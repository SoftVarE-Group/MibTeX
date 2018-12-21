/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.io.File;
import java.io.IOException;

import org.ini4j.Ini;

/**
 * A class to export the citations from scholar for each BibTeX entry
 * 
 * @author Christopher Sontag
 */
public class CitationService {

	private static String CITATION_DIR;

	/**
	 * Example arguments
	 * 
	 * CitationService "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
	 * 
	 * @param args
	 *            containing path to the citations.csv (default: BibTeX dir)
	 */
	public static void main(String[] args) {
		File iniFile = new File("options.ini");
		if (iniFile.exists()) {
			Ini ini = null;
			try {
				ini = new Ini(iniFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
            if (ini != null) {
            	CITATION_DIR = ini.get("options", "citation-dir");
                if (CITATION_DIR == null || CITATION_DIR.isEmpty()) {
                    CITATION_DIR = ini.get("options", "bibtex-dir");
                }
            }
		} else {
			try {
				CITATION_DIR = args[0];
			} catch (IndexOutOfBoundsException e) {

			}
		}
		try {
            File citationsFile = new File(CITATION_DIR, "citations.csv");
            File problemsFile = new File(CITATION_DIR, "problems.csv");
            if (!problemsFile.exists()) problemsFile.createNewFile();
            ScholarService service = new ScholarService(citationsFile, problemsFile);
            service.start();
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

}
