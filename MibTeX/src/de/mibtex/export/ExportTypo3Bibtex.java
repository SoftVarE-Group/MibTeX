package de.mibtex.export;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Bittner
 */
public class ExportTypo3Bibtex extends Export {
	private static String MYabrv = "MYabrv.bib";
	private static String MYshort = "MYshort.bib";
	// We do not consider MYfull because it is deprecated.
	
	private static Map<String, String> ToURLOverwrites = new HashMap<>();
	static {
		ToURLOverwrites.putIfAbsent("&auml;", "ä");
		ToURLOverwrites.putIfAbsent("&ouml;", "ö");
		ToURLOverwrites.putIfAbsent("&uuml;", "ü");
		ToURLOverwrites.putIfAbsent("&Auml;", "Ä");
		ToURLOverwrites.putIfAbsent("&Ouml;", "Ö");
		ToURLOverwrites.putIfAbsent("&Uuml;", "Ü");
	}
	
	public ExportTypo3Bibtex(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		final Predicate<BibtexEntry> filter =
				// filter for our institute
				b -> Arrays.asList("Thomas Thüm", "Paul Maximilian Bittner", "Chico Sundermann", "Jeffrey Young", "Tobias Heß").stream().anyMatch(MakeTypo3Safe(b.author)::contains);

		Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, MYabrv));
		String typo3 = entries.values().stream()
				.filter(filter)
				.map(b -> toTypo3(b, variables))
				.reduce((a, b) -> a + "\n\n" + b)
				.orElseGet(() -> "");
		
		writeToFile(new File(BibtexViewer.OUTPUT_DIR, "typo3.bib"), typo3);
	}
	
	private Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
		Map<String, String> vars = new HashMap<String, String>();
		
		String[] file = readFromFile(pathToBibtex).split(System.lineSeparator());
		for (String line : file) {
			line = line.trim();
			if (line.startsWith("@String")) {
				line = line.substring(line.lastIndexOf("{"));
				String[] words = line.substring(0, line.indexOf("}")).split("=");
				if (words.length == 2) {
					String var = words[0].trim();
					// remove apostrophes ("") at the beginning and end
					String val = words[1].trim().substring(1, words[1].length() - 2);
					vars.put(var, val);
				}
			}
		}
		return vars;
	}

	private String toTypo3(BibtexEntry bib, Map<String, String> variables) {
		String typo3 = "@" + bib.type + "{" + bib.key;
		
		typo3 += GenBibTeXAttribute("author", bib.author);
		typo3 += GenBibTeXAttribute("title", bib.title);
		typo3 += GenBibTeXAttribute("year", Integer.toString(bib.year));
		typo3 += GenBibTeXAttributeIfPresent("booktitle", lookup(bib.venue, variables));
		typo3 += GenBibTeXAttributeIfPresent("address", lookup(GetAttribute(bib, BibTeXEntry.KEY_ADDRESS), variables));
		typo3 += GenBibTeXAttributeIfPresent("publisher", lookup(GetAttribute(bib, BibTeXEntry.KEY_PUBLISHER), variables));
		typo3 += GenBibTeXAttributeIfPresent("journal", lookup(GetAttribute(bib, BibTeXEntry.KEY_JOURNAL), variables));
		//typo3 += GenBibTeXAttributeIfPresent("series", lookup(GetAttribute(bib, BibTeXEntry.KEY_SERIES), variables));
		typo3 += GenBibTeXAttributeIfPresent("doi", lookup(GetAttribute(bib, BibTeXEntry.KEY_DOI), variables));

		return typo3 + "\n}";
	}
	
	private String lookup(String variable, final Map<String, String> variables) {
		String value = variables.get(variable);
		return value == null ? variable : value;
	}
	
	private static String GetAttribute(BibtexEntry entry, org.jbibtex.Key attribKey) {
		org.jbibtex.Value attrib = entry.entry.getField(attribKey);
		return attrib != null ? attrib.toUserString() : "";
	}
	
	private static String MakeTypo3Safe(String s) {
		return BibtexEntry.toURL(BibtexEntry.replaceUmlauts(s.trim()), ToURLOverwrites);
	}
	
	private static String GenBibTeXAttribute(String name, String value) {
		return ",\n  " + name + " = {" + MakeTypo3Safe(value) + "}";
	}
	
	private static String GenBibTeXAttributeIfPresent(String name, String value) {
		return BibtexEntry.IsDefined(value) ? GenBibTeXAttribute(name, value) : "";
	}
}
