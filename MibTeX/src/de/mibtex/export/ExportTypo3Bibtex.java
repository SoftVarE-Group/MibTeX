package de.mibtex.export;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Bittner
 */
public class ExportTypo3Bibtex extends Export {
	// TODO: Having numbers in Bibtex tags is not conforming the Bibtex standard.
	private final static String Typo3TagsAttribute = "typo3Tags";

	private static class Typo3Entry {
		String type; // inproceedings, article, ...
		String key;
		String typeAttrib; // type = {...}
		String title;
		List<String> authors;
		List<String> editors;
		int year;
		String booktitle;
		
		String address;
		String publisher;
		String journal;
		String location;
		
		String school;
		String pages;
		
		String doi;
		String isbn;
		String issn;
		
		List<String> tags;
		
		public Typo3Entry(BibtexEntry bib, Map<String, String> variables) {
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
			
			typo3 += GenBibTeXAttributeIfPresent("tags", tags.stream().reduce("", (a, b) -> a + ", " + b));

			return typo3 + "\n}";
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
		
		final static Predicate<Typo3Entry> WithThomas = Filters.AuthorOrEditorIsOneOf("Thomas Thüm");
		final static Predicate<Typo3Entry> WithThomasAtISF = WithThomas.and(b -> b.year < 2020);
		final static Predicate<Typo3Entry> WithThomasAtUlm = WithThomas.and(b -> b.year >= 2020);
		
		final static Predicate<Typo3Entry> SoftVarE = Filters.AuthorOrEditorIsOneOf("Thomas Thüm", "Chico Sundermann", "Tobias Heß", "Paul Maximilian Bittner").and(b -> b.year >= 2020);
		
		final static Predicate<Typo3Entry> BelongsToVariantSync = b -> {
			if (b.tags == null) return false;
			return b.tags.stream().anyMatch(IsOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
		};
		
		private Filters() {}
		
		private static Predicate<Typo3Entry> KeyIsOneOf(String... keys) {
			return b -> Arrays.asList(keys)
					.stream()
					.anyMatch((b.key)::equals);
		}
		
		private static Predicate<Typo3Entry> AuthorOrEditorIsOneOf(String... authors) {
			return b -> Arrays.asList(authors)
					.stream()
					.anyMatch(author -> b.authors.contains(author) || b.editors.contains(author));
		}
		
		@SafeVarargs
		private static <T> Predicate<T> IsOneOf(T... elements) {
			return s -> Arrays.asList(elements)
					.stream()
					.anyMatch(s::equals);
		}
		
		private static <T> Predicate<T> Any() {
			return x -> true;
		}
	}
	
	private static class Modifiers {
		public static Typo3Entry MarkThomasAsEditor(Typo3Entry t) {
			// This is so pure .... not
			if (t.editors.contains("Thomas Thüm")) {
				t.tags.add("EditorialThomasThuem");
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
		// TODO: GI not found in MYabrv
		// TODO: TO appear
		// TODO: Doppelte Namen -> Hardcoded solution
		// TODO: Warum fehlen noch 20? Publikationen von Thomas.
		// TODO: Wiki-Eintrag
		
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
				Filters.KeyIsOneOf("RSC+:SE21", "KJN+:SE21")
				//Filters.SoftVarE
				//Filters.WithThomas
		;
		
		/**
		 * Select the modifiers you want to apply to each entry after filtering.
		 * For instance, Modifiers::MarkThomasAsEditor checks if Thomas Thüm is an editor of the entry and if so,
		 * adds a specific tag to the publication that we use for the website.
		 * If unsure, leave unchanged.
		 */
		final List<Function<Typo3Entry, Typo3Entry>> modifiers = Arrays.asList(
				Modifiers::MarkThomasAsEditor
		);

		/**
		 * Working code from here.
		 * It should not be necessary that you touch this.
		 */
		Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, MYabrv));
		String typo3 = entries.values().stream()
				.map(b -> new Typo3Entry(b, variables))
				.filter(bibFilter)
				.map(modifiers.stream().reduce(Function.identity(), Function::compose))
				.map(Typo3Entry::toString)
/*
				.count() + ""
/*/
				.reduce("", (a, b) -> a + "\n\n" + b)
//*/
				;

		System.out.println(typo3);

        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPORT);
        encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
		writeToFile(new File(BibtexViewer.OUTPUT_DIR, "typo3.bib"), typo3, encoder);
	}
	
	private Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
		Map<String, String> vars = new HashMap<String, String>();
		
		String[] file = readFromFile(pathToBibtex).split(System.lineSeparator());
		for (String line : file) {
			line = line.trim();
			if (line.startsWith("@String")) {
				line = line.substring(line.lastIndexOf("{") + 1);
				String[] words = line.substring(0, line.indexOf("}")).split("=");
				if (words.length == 2) {
					String var = words[0].trim().toUpperCase();
					// remove apostrophes ("") at the beginning and end
					String val = words[1].trim().substring(1, words[1].length() - 2);
					vars.put(var, val);
//					System.out.println("[ExportTypo3Bibtex.readVariablesFromBibtexFile] " + var + " -> " + val);
				}
			}
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
	
	private static List<String> splitAttributeListString(List<String> tags) {
		return Repeat(tags,
				Arrays.asList(",", ";"),
				(div, l) -> l.stream().collect(
						() -> new ArrayList<String>(),
						(list, kw) -> ((List<String>)list).addAll(Arrays.stream(kw.split(div)).map(s -> s.trim()).collect(Collectors.toList())
								),
						(list1, list2) -> list1.addAll(list2)));
	}
	
	private static <T, A> T Repeat(T t, Collection<A> args, BiFunction<A, T, T> f) {
		for (A a : args) {
			t = f.apply(a,  t);
		}
		return t;
	}
}
