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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;
import java.util.stream.Collectors;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Bittner
 */
public class ExportTypo3Bibtex extends Export {
	private final static String typo3TagsAttribute = "typo3Tags";
	
	private static class Filters {
		final static Predicate<BibtexEntry> Is_misc = b -> b.type.equals("misc");
		final static Predicate<BibtexEntry> Is_proceedings = b -> b.type.equals("proceedings");
		final static Predicate<BibtexEntry> Is_techreport = b -> b.type.equals("techreport");
		final static Predicate<BibtexEntry> Is_bachelorsthesis =
				b -> b.type.equals("mastersthesis") && GetAttribute(b, BibTeXEntry.KEY_TYPE).toLowerCase().startsWith("bachelor");
		final static Predicate<BibtexEntry> Is_mastersthesis = b -> b.type.equals("mastersthesis");
		final static Predicate<BibtexEntry> Is_phdthesis = b -> b.type.equals("phdthesis");
		
		final static Predicate<BibtexEntry> WithThomas = Filters.ByAuthors("Thomas Thüm");
		final static Predicate<BibtexEntry> WithThomasAtISF = WithThomas.and(b -> b.year < 2020);
		final static Predicate<BibtexEntry> WithThomasAtUlm = WithThomas.and(b -> b.year >= 2020);
		
		final static Predicate<BibtexEntry> BelongsToVariantSync = b -> {
			List<String> tags = b.tagList.get(typo3TagsAttribute);
			if (tags == null) return false;
			return splitAttributeListString(tags).stream().anyMatch(IsOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
		};
		
		private Filters() {}
		
		private static Predicate<BibtexEntry> ByKeys(String... keys) {
			return b -> Arrays.asList(keys)
					.stream()
					.anyMatch((b.key)::equals);
		}
		
		private static Predicate<BibtexEntry> ByAuthors(String... authors) {
			return b -> Arrays.asList(authors)
					.stream()
					.anyMatch(MakeTypo3Safe(b.author)::contains);
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
	
	private static class Tagger {
		final static Tagger MarkThomasAsEditor = new Tagger(
				Filters.WithThomas.and(b -> b.authorsAreEditors),
				b -> "EditorialThomasThuem");
		
		Predicate<BibtexEntry> condition;
		Function<BibtexEntry, String> tagFor;
		
		public Tagger(Predicate<BibtexEntry> condition, Function<BibtexEntry, String> tagFor) {
			this.condition = condition;
			this.tagFor = tagFor;
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
		ToURLOverwrites.putIfAbsent(":", ":");
		ToURLOverwrites.putIfAbsent("/", "/");
	}
	
	public ExportTypo3Bibtex(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		// Configure filtering of BibItems here
		final Predicate<BibtexEntry> bibFilter = Filters.Any();//Filters.ByKeys("BTS:SEFM19");
		final List<Tagger> taggers = Arrays.asList(Tagger.MarkThomasAsEditor);

		Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, MYabrv));

		String typo3 = entries.values().stream()
				.filter(bibFilter)
				.map(b -> toTypo3(b, variables, taggers))
//				.count() + "";
				.reduce((a, b) -> a + "\n\n" + b)
				.orElseGet(() -> "");

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

	private String toTypo3(BibtexEntry bib, Map<String, String> variables, List<Tagger> taggers) {
		String typo3 = "@" + bib.type + "{" + bib.key;

		typo3 += GenBibTeXAttributeIfPresent("type", GetAttribute(bib, BibTeXEntry.KEY_TYPE));
		typo3 += GenBibTeXAttribute(bib.authorsAreEditors ? "editor" : "author",
				bib.authorList.stream()
				.reduce((a, b) -> a + " and " + b)
				.orElseGet(() -> {throw new IllegalArgumentException("Author list is empty!");}));
		
		typo3 += GenBibTeXAttribute("title", bib.title);
		typo3 += GenBibTeXAttribute("year", Integer.toString(bib.year));
		
		{
			String booktitle;
			if (Filters.Is_techreport.test(bib)) {
				booktitle = ("Technical Report " + GetAttribute(bib, BibTeXEntry.KEY_NUMBER)).trim();
//			} else if (Filters.is_phdthesis.test(bib)) {
//				booktitle = "PhD Thesis";
//			} else if (Filters.is_bachelorsthesis.test(bib)) {
//				booktitle = "Bachelor's Thesis";
//			} else if (Filters.is_mastersthesis.test(bib)) {
//				booktitle = "Master's Thesis";
			} else {
				booktitle = lookup(GetAttribute(bib, BibTeXEntry.KEY_BOOKTITLE), variables);
			}
			typo3 += GenBibTeXAttributeIfPresent("booktitle", booktitle);
		}
		
		typo3 += GenBibTeXAttributeIfPresent("address", lookup(GetAttribute(bib, BibTeXEntry.KEY_ADDRESS), variables));
		typo3 += GenBibTeXAttributeIfPresent("publisher", lookup(GetAttribute(bib, BibTeXEntry.KEY_PUBLISHER), variables));
		typo3 += GenBibTeXAttributeIfPresent("journal", lookup(GetAttribute(bib, BibTeXEntry.KEY_JOURNAL), variables));
		typo3 += GenBibTeXAttributeIfPresent("location", lookup(GetAttribute(bib, "location"), variables));
		
		typo3 += GenBibTeXAttributeIfPresent("school", GetAttribute(bib, BibTeXEntry.KEY_SCHOOL));
		typo3 += GenBibTeXAttributeIfPresent("pages", GetAttribute(bib, BibTeXEntry.KEY_PAGES));
		
		typo3 += GenBibTeXAttributeIfPresent("doi", GetAttribute(bib, BibTeXEntry.KEY_DOI));
		typo3 += GenBibTeXAttributeIfPresent_Unsafe("isbn", GetAttribute(bib, "isbn"));
		typo3 += GenBibTeXAttributeIfPresent_Unsafe("issn", GetAttribute(bib, "issn"));
		
		{
			List<String> tags = bib.tagList.get(typo3TagsAttribute);
			if (tags == null) {tags = new ArrayList<>();}
			
			List<String> taggerTags = new ArrayList<>();
			for (Tagger tagger : taggers) {
				if (tagger.condition.test(bib)) {
					taggerTags.add(tagger.tagFor.apply(bib));
				}
			}
			
			Optional<String> tagsLine = Stream.concat(
						taggerTags.stream(),
						// Multiple keywords may be packed into one string and separated by "," or ";".
						// Thus, divide them here into multiple keywords.
						splitAttributeListString(tags).stream()
					).reduce((a, b) -> a + ", " + b);
			if (tagsLine.isPresent()) {
				typo3 += GenBibTeXAttributeIfPresent("tags", tagsLine.get());
			}
		}		

		return typo3 + "\n}";
	}
	
	private String lookup(String variable, final Map<String, String> variables) {
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
	
	private static String GenBibTeXAttribute_Unsafe(String name, String value) {
		return ",\n  " + name + " = {" + value + "}";
	}
	
	private static String GenBibTeXAttribute(String name, String value) {
		return GenBibTeXAttribute_Unsafe(name, MakeTypo3Safe(value));
	}
	
	private static String GenBibTeXAttributeIfPresent_Unsafe(String name, String value) {
		return BibtexEntry.IsDefined(value) ? GenBibTeXAttribute_Unsafe(name, value) : "";
	}
	
	private static String GenBibTeXAttributeIfPresent(String name, String value) {
		return BibtexEntry.IsDefined(value) ? GenBibTeXAttribute(name, value) : "";
	}
	
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
	
	@SafeVarargs
	private static <T> Predicate<T> AtMost(int limit, Predicate<? super T>... predicates) {
		return x -> {
			int matches = 0;
			for (Predicate<? super T> p : predicates) {
				matches += p.test(x) ? 1 : 0;
				if (matches > limit) {
					return false;
				}
			}
			return true;
		};
	}
}
