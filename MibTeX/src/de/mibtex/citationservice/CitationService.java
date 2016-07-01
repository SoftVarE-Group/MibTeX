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

	/**
	 * Example arguments
	 * 
	 * CitationService "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
	 * 
	 * @param arg
	 *            containing path to the citations.csv (default: BibTeX dir)
	 */
	public static void main(String[] args) {
		try {
			File file = new File(args[0], "citations.csv");
			ScholarService service = new ScholarService(file);
			service.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
