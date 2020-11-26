package de.mibtex.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Filters;
import de.mibtex.export.typo3.Modifiers;
import de.mibtex.export.typo3.Typo3Entry;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Bittner
 */
public class ExportTypo3Bibtex extends Export {
	// TODO: Having numbers in Bibtex tags is not conforming the Bibtex standard.
	public final static String Typo3TagsAttribute = "typo3Tags";
	private final static String MYabrv = "MYabrv.bib";
	private final static String MYshort = "MYshort.bib";
	// We do not consider MYfull because it is deprecated.
	
	/**
	 * Select the filter you need to export only the publications you are interested in.
	 * A Typo3Entry t gets selected if bibFilter.test(t) returns 'true'.
	 * You may use or compose default filters and helper functions from de.mibtex.export.typo3.Filters.
	 * For instance, if you want to select a subset of specific publications manually, use
	 *     Filters.KeyIsOneOf("Key1", "Key2", ...)
	 * to select all publications with these keys.
	 */
	private final Predicate<Typo3Entry> bibFilter =
			//Filters.Any()
			//Filters.KeyIsOneOf("RSC+:SE21", "KJN+:SE21", "KJN+:FASE20")
			Filters.AuthorOrEditorIsOneOf(Filters.ThomasThuem).and(Filters.Is_misc.negate())
	;
	
	/**
	 * Select the modifiers you want to apply to each entry after filtering.
	 * Each modifier is a function taking a Typo3Entry and returning the modified Typo3Entry.
	 * Default implementations can be found in de.mibtex.export.typo3.Modifiers.
	 * For instance, Modifiers::MarkIfThomasIsEditor checks if Thomas Thüm is an editor of the entry and if so,
	 * adds a specific tag to the publication that we use for the website.
	 * Some modifiers are dedicated to resolving duplicate entries (w.r.t. titles) because Typo3 considers entries with the same title to be the same.
	 * If unsure, leave unchanged.
	 */
	private final List<Function<Typo3Entry, Typo3Entry>> modifiers = Arrays.asList(
			  Modifiers.MarkIfThomasIsEditor
			, Modifiers.MarkIfToAppear
			
			// Resolving duplicates
			, Modifiers.IfKeyIs("useRLB+:AOSD14", Modifiers.MarkIfTechreport)
			, Modifiers.IfKeyIs("TKL:SPLC18", Modifiers.AppendToTitle("(Second Edition)"))
			, Modifiers.IfKeyIs("KAT:TR16", Modifiers.MarkIfTechreport)
			, Modifiers.IfKeyIs("TKK+:SPLC19", Modifiers.MarkAsExtendedAbstract)
			, Modifiers.IfKeyIs("KTS+:SE19", Modifiers.MarkAsExtendedAbstract)
			, Modifiers.IfKeyIs("KJN+:SE21", Modifiers.MarkAsExtendedAbstract)
			, Modifiers.IfKeyIs("RSC+:SE21", Modifiers.MarkAsExtendedAbstract)
			, Modifiers.IfKeyIs("KTP+:SE19", Modifiers.MarkAsExtendedAbstract)
	);
	
	
	public ExportTypo3Bibtex(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		// TODO: Wiki-Eintrag
		// TODO: Warum fehlen noch 20? Publikationen von Thomas.
		/**
		 * 1.) Duplikate kann es online nicht geben, da Typo3 den Titel als Primary Key nimmt.
		 * 2.) Manche Duplikate sind online aber dennoch vorhanden, weil das eine im Ordner "Publikationen" und das andere in "Alte Publikationen Thomas Thüm" liegt.
		 * 
		 * Stats:
		 * A = Thomas ist Autor.
		 * E = Thomas ist Editor.
		 * ----------------------------------
		 * Filter               |total|unique
		 * ----------------------------------
		 * A                    | 111 | 102
		 * E                    |   5 |   5
		 * A und E              |   0 |   0
		 * A oder E             | 116 | 107
		 * (A oder E) und !misc | 115 | 107
		 */

		Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, MYabrv));
		
		List<Typo3Entry> typo3Entries = entries.values().stream()
				.map(b -> new Typo3Entry(b, variables))
				.filter(bibFilter)
				.map(modifiers.stream().reduce(Function.identity(), Function::compose))
				.collect(Collectors.toList());
		
		String typo3 = typo3Entries.stream()
				.map(Typo3Entry::toString)
				.reduce("", (a, b) -> a + "\n\n" + b);

		System.out.println(typo3);
		System.out.println();
		
		// Check if we have some duplicates left that were not resolved.
		Collections.sort(typo3Entries);
		int duplicates = 0;
		for (int i = 0; i < typo3Entries.size() - 1; ++i) {
			if (typo3Entries.get(i).equals(typo3Entries.get(i + 1))) {
				System.out.println("  > Found unresolved duplicate: " + typo3Entries.get(i).title);
				++duplicates;
			}
		}
		
		final long numUniqueEntries = typo3Entries.size() - duplicates;
		
		System.out.println("\nExported " + typo3Entries.size() + " entries.");
		System.out.println("Thereof " + numUniqueEntries + " entries are unique (by title).");
		System.out.println();
		System.out.flush();
		
		if (duplicates > 0) {
			System.err.flush();
			System.err.println("There were unresolved duplicates that can cause problems when imported with TYPO3!");
		}

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		writeToFile(new File(BibtexViewer.OUTPUT_DIR, "typo3.bib"), typo3, encoder);
	}

	private static Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
		Map<String, String> vars = new HashMap<String, String>();
		
		BufferedReader file = readFromFile(pathToBibtex, Charset.forName("UTF-8"));
		try {
			while (file.ready()) {
				String line = file.readLine().trim();
				if (line.startsWith("@String")) {
					line = line.substring(line.indexOf("{") + 1);
					String[] words = line.substring(0, line.lastIndexOf("}")).split("=");
					if (words.length == 2) {
						String var = words[0].trim().toUpperCase();
						// Remove apostrophes ("") at the beginning and end.
						String val = words[1].trim().substring(1, words[1].length() - 2);
						vars.put(var, val);
						//System.out.println("[ExportTypo3Bibtex.readVariablesFromBibtexFile] " + var + " -> " + val);
					}
				}
			}
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return vars;
	}
}
