/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Filters;
import de.mibtex.export.typo3.Typo3Entry;

/**
 * A class that generates a single text file with all my conflicts.
 *
 * @author Thomas Thuem
 */
public class ExportConflicts extends ExportTypo3Bibtex {

    public ExportConflicts(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
		// Parse the variables defined in MYabrv.bib
    	Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, VariablesFile));
    	
    	// TODO Paul, why does this still contain publications for which I am only an editor?
		// Transform all Bibtex-Entries to Typo3Entries, filter them and apply all modifiers.
		List<Typo3Entry> myPapers = entries.values().stream()
				.map(b -> new Typo3Entry(b, variables))
				.filter(Filters.authorIsOneOf(Filters.THOMAS_THUEM))
				.collect(Collectors.toList());

		Set<String> authors = new TreeSet<String>();
        for (Typo3Entry paper : myPapers) {
        	if (paper.year >= 2019) {
            	for (String author : paper.authors) {
            		authors.add(author);
            	}
        	}
        }
        
        // institutions
        authors.add("All (University of Ulm)");
        authors.add("All (TU Braunschweig)");
        
        // supervisors
        authors.add("Gunter Saake");
        authors.add("Christian Kästner");
        authors.add("Ina Schaefer");
        
        // advised students (doing their PhD in SE)
        authors.add("Daniel Lüddecke");
        authors.add("Stefan Krüger");
        authors.add("Jens Meinicke");
        authors.add("Sofia Ananieva");

        // co-advised PhD students
        authors.add("Reimar Schröter");
        authors.add("Mustafa Al-Hajjaji");
        authors.add("Matthias Kowal");
        authors.add("Sascha Lity");
        authors.add("Jeffrey M. Young");
        authors.add("Alexander Knüppel");
        authors.add("Alexander Kittelmann");
        authors.add("Sebastian Krieter");
        authors.add("Paul Maximilian Bittner");
        authors.add("Chico Sundermann");
        authors.add("Tobias Heß");
        authors.add("Marc Hentze");
        authors.add("Tobias Pett");
        authors.add("Sabrina Böhm");
        authors.add("Rahel Arens");
        
        // other conflicts
        authors.add("Thorsten Berger");
        authors.add("David Benavides");
        
        StringBuilder conflicts = new StringBuilder();
        for (String author : authors) {
        	conflicts.append(author);
        	conflicts.append(System.lineSeparator());
        }
        
        writeToFile(BibtexViewer.OUTPUT_DIR, "conflicts.txt", conflicts.toString());
    }

}
