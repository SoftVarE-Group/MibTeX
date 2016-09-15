/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import org.ini4j.Ini;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
            String citationDir;
            if (ini != null) {
                citationDir = ini.get("options", "citation-dir");
                if (citationDir.isEmpty()) {
                    CITATION_DIR = ini.get("options", "bibtex-dir");
                }
            }
		} else {
			CITATION_DIR = args[0];
		}
		try {
            File file = new File(CITATION_DIR, "citations.csv");
            ScholarService service = new ScholarService(file);
            service.start();
        } catch (Exception e) {
			e.printStackTrace();
		}
	}

}
