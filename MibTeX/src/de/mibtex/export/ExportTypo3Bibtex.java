/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Filters;
import de.mibtex.export.typo3.Modifiers;
import de.mibtex.export.typo3.Typo3Entry;
import de.mibtex.export.typo3.Util;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Maximilian Bittner
 */
public class ExportTypo3Bibtex extends Export {
	// TODO: Having numbers in Bibtex tags is not conforming the Bibtex standard.
	public final static String TYPO3_TAGS_ATTRIBUTE = "typo3Tags";
	private final static String MYABRV = "MYabrv.bib";
	private final static String MYSHORT = "MYshort.bib";
	// We do not consider MYfull because it is deprecated.

	/**
	 * Choose the variables file that you want to use to substitute names in the exported bibtex file.
	 * Choose from
	 * @see MYabrv
	 * @see MYshort
	 */
	private final String VariablesFile = MYABRV;
	
	/**
	 * Select the filter you need to export only the publications you are interested in.
	 * A Typo3Entry t gets selected if bibFilter.test(t) returns 'true'.
	 * You may use or compose default filters and helper functions from de.mibtex.export.typo3.Filters.
	 * For instance, if you want to select a subset of specific publications manually, use
	 *     Filters.KeyIsOneOf("Key1", "Key2", ...)
	 * to select all publications with these keys.
	 * Compose filters with the respective methods of Predicate<T> (such as `and`, `or`).
	 */
	private final Predicate<Typo3Entry> bibFilter =
			//Filters.ANY,
			Filters.keyIsOneOf("Y21", "YWT:SPLC20")
			//Filters.BELONGS_TO_SOFTVARE
			//Filters.WithThomasAtUlm
			//Filters.Any()
			//Filters.keyIsOneOf("KTSB:ICSE21")
			//Filters.WithPaulAtUlm
			//Filters.WithPaulBeforeOrNotAtUlm
			//Filters.WithPaul.and(Filters.WithThomasBeforeUlm)
			//Filters.WITH_CHICO
			;

	/**
	 * Select the modifiers you want to apply to each entry after filtering.
	 * Each modifier is a function taking a Typo3Entry and returning the modified Typo3Entry.
	 * Default implementations can be found in de.mibtex.export.typo3.Modifiers.
	 * For instance, Modifiers.MarkIfThomasIsEditor checks if Thomas Thüm is an editor of the entry and if so,
	 * adds a specific tag to the publication that we use for the website.
	 * Some modifiers are dedicated to resolving duplicate entries (w.r.t. titles) because Typo3 considers entries with the same title to be the same.
	 * If unsure, leave unchanged.
	 */
	private final List<Function<Typo3Entry, Typo3Entry>> modifiers = Arrays.asList(
			  Modifiers.MARK_IF_THOMAS_IS_EDITOR
			, Modifiers.MARK_IF_TO_APPEAR
			, Modifiers.ADD_PAPER_LINK_IF_SOFTVARE
			
			// Custom solutions
			, Modifiers.whenKeyIs("DGT:EMSE21", Modifiers.MARK_THOMAS_AS_EDITOR)
			, Modifiers.whenKeyIs("Y21", Modifiers.KEEP_URL_IF_PRESENT)

			// Resolving duplicates
			, Modifiers.whenKeyIs("Y21", Modifiers.MARK_AS_PHDTHESIS)
			, Modifiers.whenKeyIs("KJN+:SE21", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("RSC+:SE21", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("TKK+:SPLC19", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("KTS+:SE19", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("KTP+:SE19", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("TKL:SPLC18", Modifiers.appendToTitle("(Second Edition)"))
			, Modifiers.whenKeyIs("KTM+:SE18", Modifiers.MARK_AS_EXTENDED_ABSTRACT)
			, Modifiers.whenKeyIs("KAT:TR16", Modifiers.MARK_IF_TECHREPORT)
			, Modifiers.whenKeyIs("useRLB+:AOSD14", Modifiers.MARK_IF_TECHREPORT)
			);

	public ExportTypo3Bibtex(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		// Parse the variables defined in MYabrv.bib
		final Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, VariablesFile));

		// Transform all Bibtex-Entries to Typo3Entries, filter them and apply all modifiers.
		final List<Typo3Entry> typo3Entries = entries.values().stream()
				.map(b -> new Typo3Entry(b, variables))
				.filter(bibFilter)
				.map(modifiers.stream().reduce(Function.identity(), Function::compose))
				.collect(Collectors.toList());

		// Generate the typo3-conforming Bibtex source code.
		final String typo3 = typo3Entries.stream()
				.map(Typo3Entry::toString)
				.reduce("", (a, b) -> a + "\n\n" + b);

		System.out.println(typo3);
		System.out.println();

		// Check if we have some duplicates left that were not resolved.
		final int duplicates = Util.getDuplicates(typo3Entries, (a, b) -> System.out.println("  > Found unresolved duplicate: " + a.title));
		final long numUniqueEntries = typo3Entries.size() - duplicates;

		System.out.println("\nExported " + typo3Entries.size() + " entries.");
		System.out.println("Thereof " + numUniqueEntries + " entries are unique (by title).\n");
		System.out.flush();

		if (duplicates > 0) {
			System.err.flush();
			System.err.println("There were unresolved duplicates that can cause problems when imported with TYPO3!");
		}

		writeToFileInUTF8(new File(BibtexViewer.OUTPUT_DIR, "typo3.bib"), typo3);
	}

	private static Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
		final Map<String, String> vars = new HashMap<String, String>();

		final BufferedReader file = readFromFile(pathToBibtex, Charset.forName("UTF-8"));
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
		} finally {
			try {
				file.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return vars;
	}
}
