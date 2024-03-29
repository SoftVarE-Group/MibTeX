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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;
import de.mibtex.citationservice.CitationEntry;

/**
 * A class that generates a .csv file with all BibTeX entries for the citation
 * service
 * 
 * @author Christopher Sontag
 */
public class ExportCitations extends Export {

	public ExportCitations(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		List<CitationEntry> newCitations = new ArrayList<CitationEntry>();
		List<CitationEntry> citations = readCitationFile(new File(BibtexViewer.CITATION_DIR, "citations.csv"));
		for (BibtexEntry entry : entries.values()) {
			CitationEntry newEntry = new CitationEntry(entry.key, encodeTitle(entry.title));
			if (citations.contains(newEntry)) {
				newCitations.add(citations.get(citations.indexOf(newEntry)));
			} else {
				newCitations.add(newEntry);
			}
		}
		writeCitationFile(new File(BibtexViewer.CITATION_DIR, "citations.csv"), newCitations);
	}

	protected List<CitationEntry> readCitationFile(File file) {
		System.out.print("Reading " + file.getName() + "... ");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		List<CitationEntry> entries = new ArrayList<CitationEntry>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				entries.add(CitationEntry.getFromCSV(line));
			}
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
		return entries;
	}

	protected void writeCitationFile(File file, List<CitationEntry> entries) {
		System.out.print("Updating " + file.getName() + "... ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (CitationEntry entry : entries) {
				out.append(entry.getCSVString());
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
	}

	public static String encodeTitle(String s) {
		s = s.replace("&auml;", "ae");
		s = s.replace("&ouml;", "oe");
		s = s.replace("&uuml;", "ue");
		s = s.replace("&Auml;", "Ae");
		s = s.replace("&Ouml;", "Oe");
		s = s.replace("�", "O");
		s = s.replace("&Uuml;", "Ue");
		s = s.replace("&szlig;", "ss");
		s = s.replace("&amp;", "and");
		s = s.replace("&#8211;", "-");
		s = s.replace("?", "");
		s = s.replace("\\", "");
		s = s.replace("/", "");
		s = s.replace("#", "");

		return s;
	}

}
