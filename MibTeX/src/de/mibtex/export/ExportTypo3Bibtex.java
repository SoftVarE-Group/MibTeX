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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Bittner
 */
public class ExportTypo3Bibtex extends Export {
	// TODO: Having numbers in Bibtex tags is not conforming the Bibtex standard.
	private final static String Typo3TagsAttribute = "typo3Tags";
	private final static String Thomas = "Thomas Thüm";
	private final static String Chico = "Chico Sundermann";
	private final static String Tobias = "Tobias Heß";
	private final static String Paul = "Paul Maximilan Bittner";
	
	private static class Typo3Entry implements Comparable<Typo3Entry> {
		final BibtexEntry source;
		
		String type; // inproceedings, article, ...
		String key;
		
		String typeAttrib; // type = {...}
		String title;
		List<String> authors;
		List<String> editors;
		String booktitle;
		String address;
		String publisher;
		String journal;
		String location;
		String school;
		String pages;
		int year;
		
		String doi;
		String isbn;
		String issn;
		
		String note;
		
		List<String> tags;
		
		public Typo3Entry(BibtexEntry bib, Map<String, String> variables) {
			this.source = bib;
			
			this.type = bib.type;
			this.typeAttrib = MakeTypo3Safe(GetAttribute(bib, BibTeXEntry.KEY_TYPE));
			this.key = bib.key;

			this.authors = new ArrayList<>();
			this.editors = new ArrayList<>();
			List<String> persons = bib.authorList.stream().map(ExportTypo3Bibtex::MakeTypo3Safe).collect(Collectors.toList());
			if (bib.authorsAreEditors) {
				this.editors = persons;
			} else {
				this.authors = persons;
			}
			
			this.title = MakeTypo3Safe(bib.title);
			this.year = bib.year;

			this.address = MakeTypo3Safe(lookup(GetAttribute(bib, BibTeXEntry.KEY_ADDRESS), variables));
			this.publisher = MakeTypo3Safe(lookup(GetAttribute(bib, BibTeXEntry.KEY_PUBLISHER), variables));
			this.journal = MakeTypo3Safe(lookup(GetAttribute(bib, BibTeXEntry.KEY_JOURNAL), variables));
			this.location = MakeTypo3Safe(lookup(GetAttribute(bib, "location"), variables));
			
			this.school = MakeTypo3Safe(GetAttribute(bib, BibTeXEntry.KEY_SCHOOL));
			this.pages = MakeTypo3Safe(GetAttribute(bib, BibTeXEntry.KEY_PAGES));
			
			this.doi = MakeTypo3Safe(GetAttribute(bib, BibTeXEntry.KEY_DOI));
			this.isbn = GetAttribute(bib, "isbn");
			this.issn = GetAttribute(bib, "issn");
			
			this.note = MakeTypo3Safe(GetAttribute(bib, BibTeXEntry.KEY_NOTE));
			
			{
				String booktitle;
				if (Filters.Is_techreport.test(this)) {
					booktitle = ("Technical Report " + GetAttribute(bib, BibTeXEntry.KEY_NUMBER)).trim();
//				} else if (Filters.is_phdthesis.test(bib)) {
//					booktitle = "PhD Thesis";
//				} else if (Filters.is_bachelorsthesis.test(bib)) {
//					booktitle = "Bachelor's Thesis";
//				} else if (Filters.is_mastersthesis.test(bib)) {
//					booktitle = "Master's Thesis";
				} else {
					booktitle = lookup(GetAttribute(bib, BibTeXEntry.KEY_BOOKTITLE), variables);
				}
				this.booktitle = MakeTypo3Safe(booktitle);
			}
			
			this.tags = bib.tagList.get(Typo3TagsAttribute);
			if (this.tags == null) {
				this.tags = new ArrayList<>();
			} else {
				this.tags = splitAttributeListString(this.tags);
			}
		}

		@Override
		public String toString() {
			String typo3 = "@" + type + "{" + key;

			typo3 += GenBibTeXAttributeIfPresent("type", this.typeAttrib);
			
			List<String> persons;
			String personType;
			if (!authors.isEmpty()) {
				persons = authors;
				personType = "author";
			} else if (!editors.isEmpty()) {
				persons = editors;
				personType = "editor";
			} else {
				throw new RuntimeException("The Typo3Entry with key " + this.key + " has neither authors nor editors!");
			}
			typo3 += GenBibTeXAttributeIfPresent(personType,
					persons.stream()
					.reduce((a, b) -> a + " and " + b)
					.orElseGet(() -> {throw new IllegalArgumentException("Person list is empty!");}));
			
			typo3 += GenBibTeXAttributeIfPresent("title", title);
			typo3 += GenBibTeXAttributeIfPresent("year", Integer.toString(year));
			typo3 += GenBibTeXAttributeIfPresent("booktitle", booktitle);
			
			typo3 += GenBibTeXAttributeIfPresent("address", address);
			typo3 += GenBibTeXAttributeIfPresent("publisher", publisher);
			typo3 += GenBibTeXAttributeIfPresent("journal", journal);
			typo3 += GenBibTeXAttributeIfPresent("location", location);
			
			typo3 += GenBibTeXAttributeIfPresent("school", school);
			typo3 += GenBibTeXAttributeIfPresent("pages", pages);
			
			typo3 += GenBibTeXAttributeIfPresent("doi", doi);
			typo3 += GenBibTeXAttributeIfPresent("isbn", isbn);
			typo3 += GenBibTeXAttributeIfPresent("issn", issn);
			
			typo3 += GenBibTeXAttributeIfPresent("note", note);
			
			typo3 += GenBibTeXAttributeIfPresent("tags", tags.stream().reduce("", (a, b) -> a + ", " + b));

			return typo3 + "\n}";
		}
		
		@Override
		public boolean equals(Object other) {
			return other != null && other instanceof Typo3Entry && ((Typo3Entry) other).title.equals(this.title);
		}

		@Override
		public int compareTo(Typo3Entry other) {
			return title.compareTo(other.title);
		}
	}

	private static class Filters {
		final static Predicate<Typo3Entry> Is_misc = b -> b.type.equals("misc");
		final static Predicate<Typo3Entry> Is_proceedings = b -> b.type.equals("proceedings");
		final static Predicate<Typo3Entry> Is_techreport = b -> b.type.equals("techreport");
		final static Predicate<Typo3Entry> Is_bachelorsthesis =
				b -> b.type.equals("mastersthesis") && b.typeAttrib.toLowerCase().startsWith("bachelor");
		final static Predicate<Typo3Entry> Is_mastersthesis = b -> b.type.equals("mastersthesis");
		final static Predicate<Typo3Entry> Is_phdthesis = b -> b.type.equals("phdthesis");
		
		final static Predicate<Typo3Entry> WithThomas = Filters.AuthorOrEditorIsOneOf(Thomas);
		final static Predicate<Typo3Entry> WithThomasBeforeUlm = WithThomas.and(b -> b.year < 2020);
		final static Predicate<Typo3Entry> WithThomasAtUlm = WithThomas.and(b -> b.year >= 2020);
		
		final static Predicate<Typo3Entry> SoftVarE = Filters.AuthorOrEditorIsOneOf(Thomas, Chico, Tobias, Paul).and(b -> b.year >= 2020);
		
		final static Predicate<Typo3Entry> BelongsToVariantSync = b -> {
			if (b.tags == null) return false;
			return b.tags.stream().anyMatch(IsOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
		};
		
		private Filters() {}
		
		private static Predicate<Typo3Entry> KeyIsOneOf(String... keys) {
			return b -> Arrays.asList(keys)
					.stream()
					.anyMatch(b.key::equals);
		}
		
		private static Predicate<Typo3Entry> AuthorOrEditorIsOneOf(String... authors) {
			return AuthorIsOneOf(authors).or(EditorIsOneOf(authors));
		}
		
		private static Predicate<Typo3Entry> AuthorIsOneOf(String... authors) {
			return b -> AnyMatch(b.authors::contains, authors);
		}
		
		private static Predicate<Typo3Entry> EditorIsOneOf(String... editors) {
			return b -> AnyMatch(b.editors::contains, editors);
		}
		
		@SafeVarargs
		private static <T> Predicate<T> IsOneOf(T... elements) {
			return s -> AnyMatch(s::equals, elements);
		}
		
		private static <T> boolean AnyMatch(Predicate<T> condition, T... elements) {
			return Arrays.asList(elements).stream().anyMatch(condition);
		}
		
		private static <T> Predicate<T> Any() {
			return x -> true;
		}
	}
	
	private static class Modifiers {
		public static Typo3Entry MarkThomasAsEditor(Typo3Entry t) {
			if (t.editors.contains("Thomas Thüm")) {
				t.tags.add("EditorialThomasThuem");
			}
			return t;
		}
		
		public static Typo3Entry MarkTitleIfVenueIsSE(Typo3Entry t) {
			if ("SE".equals(t.source.venue)) {
				t.title += " (SE)";
			}
			return t;
		}
	}
	
	private static String MYabrv = "MYabrv.bib";
	private static String MYshort = "MYshort.bib";
	// We do not consider MYfull because it is deprecated.
	
	private static Map<String, String> ToURLOverwrites = new HashMap<>();
	static {
		ToURLOverwrites.put("&auml;", "ä");
		ToURLOverwrites.put("&ouml;", "ö");
		ToURLOverwrites.put("&uuml;", "ü");
		ToURLOverwrites.put("&Auml;", "Ä");
		ToURLOverwrites.put("&Ouml;", "Ö");
		ToURLOverwrites.put("&Uuml;", "Ü");
		ToURLOverwrites.put("&8211;", "-");
		ToURLOverwrites.put("?", "?");
		ToURLOverwrites.put(":", ":");
		ToURLOverwrites.put("/", "/");
		ToURLOverwrites.put("&szlig;", "ß");
	}
	
	public ExportTypo3Bibtex(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		// TODO: Wiki-Eintrag
		// TODO: TO appear -> Add to Booktitle?
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
		
		/**
		 * Select the filter you need to export only the publications you are interested in.
		 * A Typo3Entry t gets selected if bibFilter.test(t) returns true.
		 * You may use default filters and helper functions from the inner class Filters.
		 * If you want to select a subset of specific publications manually, use
		 *     Filters.KeyIsOneOf("Key1", "Key2", ...)
		 * to select all publications with these keys.
		 */
		final Predicate<Typo3Entry> bibFilter =
				//Filters.Any()
				//Filters.KeyIsOneOf("RSC+:SE21", "KJN+:SE21", "KJN+:FASE20")
				//Filters.KeyIsOneOf("RSC+:SE21")
				//Filters.AuthorIsOneOf("Thomas Thüm")
				//Filters.EditorIsOneOf("Thomas Thüm")
				Filters.AuthorIsOneOf(Thomas).or(Filters.EditorIsOneOf(Thomas))
				.and(Filters.Is_misc.negate())
		;
		
		/**
		 * Select the modifiers you want to apply to each entry after filtering.
		 * For instance, Modifiers::MarkThomasAsEditor checks if Thomas Thüm is an editor of the entry and if so,
		 * adds a specific tag to the publication that we use for the website.
		 * If unsure, leave unchanged.
		 */
		final List<Function<Typo3Entry, Typo3Entry>> modifiers = Arrays.asList(
				Modifiers::MarkThomasAsEditor
				//, Modifiers::MarkTitleIfVenueIsSE
		);

		/**
		 * Working code from here.
		 * It should not be necessary that you touch this.
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
		
		Collections.sort(typo3Entries);
		
		int duplicates = 0;
		for (int i = 0; i < typo3Entries.size() - 1; ++i) {
			if (typo3Entries.get(i).equals(typo3Entries.get(i + 1))) {
				duplicates += resolveDuplicate(typo3Entries.get(i), typo3Entries.get(i + 1)) ? 0 : 1;
			}
		}
		
		long numUniqueEntries = typo3Entries.size() - duplicates;
		
		System.out.println(typo3);
		System.out.println("\nExported " + typo3Entries.size() + " entries.");
		System.out.println("Thereof " + numUniqueEntries + " entries are unique (by title).");
		System.out.println();

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		writeToFile(new File(BibtexViewer.OUTPUT_DIR, "typo3.bib"), typo3, encoder);
	}
	
	private boolean resolveDuplicate(Typo3Entry a, Typo3Entry b) {
		System.out.println("Found duplicate " + a.title);
		System.out.println("  ... remains unresolved.");
		return false;
	}

	private Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
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

	private static String lookup(String variable, final Map<String, String> variables) {
		String value = variables.get(variable.toUpperCase());
		return value == null ? variable : value;
	}
	
	private static String GetAttribute(BibtexEntry entry, org.jbibtex.Key attribKey) {
		org.jbibtex.Value attrib = entry.entry.getField(attribKey);
		return attrib != null ? attrib.toUserString() : "";
	}
	
	private static String GetAttribute(BibtexEntry entry, String attribKey) {
		return GetAttribute(entry, new org.jbibtex.Key(attribKey));
	}
	
	private static String MakeTypo3Safe(String s) {
		return BibtexEntry.toURL(BibtexEntry.replaceUmlauts(s.trim()), ToURLOverwrites);
	}
	
	private static String GenBibTeXAttribute(String name, String value) {
		return ",\n  " + name + " = {" + value + "}";
	}
	
	private static String GenBibTeXAttributeIfPresent(String name, String value) {
		return BibtexEntry.IsDefined(value) ? GenBibTeXAttribute(name, value) : "";
	}
	
	// Utils
	
	/**
	 * Splits all strings in the input list to separate strings.
	 * For example, given l = ["a", "b,c", "d;e,f"] as input, we get
	 * splitAttributeListString(l) = ["a", "b", "c", "d" , "e", "f"].
	 * @param tags
	 * @return
	 */
	private static List<String> splitAttributeListString(List<String> tags) {
		Function<String, Function<List<String>, List<String>>> splitByDivider = div -> l -> {
			return l.stream().collect(
					() -> new ArrayList<String>(),
					(list, kw) -> ((List<String>)list).addAll(
							Arrays.stream(kw.split(div))
							.map(String::trim)
							.collect(Collectors.toList())),
					(list1, list2) -> list1.addAll(list2));
		};
		
		return Arrays.asList(",", ";").stream()
				.map(splitByDivider)
				.reduce(Function.identity(), Function::compose)
				.apply(tags);
	}
}
