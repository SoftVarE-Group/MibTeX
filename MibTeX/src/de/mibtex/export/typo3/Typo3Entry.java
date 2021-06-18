/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.export.ExportTypo3Bibtex;

/**
 * Represents a bibtex entry similar to BibtexEntry.java that will conform
 * to the publication importer of Typo3 when transformed to string.
 * @see toString
 * 
 * @author Paul Maximilian Bittner
 *
 */
public class Typo3Entry implements Comparable<Typo3Entry> {
	private static final String PAPER_REPO_URL = "https://github.com/SoftVarE-Group/Papers";
	private static final Map<String, String> TO_URL_OVERWRITES = new HashMap<>();
	static {
		TO_URL_OVERWRITES.put("&auml;", "ä");
		TO_URL_OVERWRITES.put("&ouml;", "ö");
		TO_URL_OVERWRITES.put("&uuml;", "ü");
		TO_URL_OVERWRITES.put("&Auml;", "Ä");
		TO_URL_OVERWRITES.put("&Ouml;", "Ö");
		TO_URL_OVERWRITES.put("&Uuml;", "Ü");
		TO_URL_OVERWRITES.put("&8211;", "-");
		TO_URL_OVERWRITES.put("?", "?");
		TO_URL_OVERWRITES.put(":", ":");
		TO_URL_OVERWRITES.put("/", "/");
		TO_URL_OVERWRITES.put("#", "#");
		TO_URL_OVERWRITES.put("&szlig;", "ß");
	}
	
	public final BibtexEntry source;
	
	public String type; // inproceedings, article, ...
	public String key;
	
	public String typeAttrib; // type = {...}
	public String title;
	public List<String> authors;
	public List<String> editors;
	public String venueVariable; // the variable used in the booktitle field in BibTags
	public String booktitle;
	public String address;
	public String publisher;
	public String journal;
	public String location;
	public String school;
	public String pages;
	public String month;
	public int year;
	
	public String doi;
	public String isbn;
	public String issn;
	
	public String note;
	
	public List<String> tags;
	
	public Typo3Entry(BibtexEntry bib, Map<String, String> variables) {
		this.source = bib;
		
		this.type = bib.type;
		this.typeAttrib = makeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_TYPE));
		this.key = bib.key;

		this.authors = new ArrayList<>();
		this.editors = new ArrayList<>();
		List<String> persons = bib.authorList.stream().map(Typo3Entry::makeTypo3Safe).collect(Collectors.toList());
		if (bib.authorsAreEditors) {
			this.editors = persons;
		} else {
			this.authors = persons;
		}
		
		this.title = makeTypo3Safe(bib.title);
		this.year = bib.year;
		this.month = bib.getAttribute(BibTeXEntry.KEY_MONTH);

		this.address = makeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_ADDRESS), variables));
		this.publisher = makeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_PUBLISHER), variables));
		this.journal = makeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_JOURNAL), variables));
		this.location = makeTypo3Safe(lookup(bib.getAttribute("location"), variables));
		
		this.school = makeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_SCHOOL));
		this.pages = makeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_PAGES));
		
		this.doi = makeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_DOI));
		this.isbn = bib.getAttribute("isbn");
		this.issn = bib.getAttribute("issn");
		
		this.note = makeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_NOTE));

		this.booktitle = parseBooktitle(bib, variables);
		this.venueVariable = bib.getAttribute(BibTeXEntry.KEY_BOOKTITLE);
		this.tags = parseTags(bib);
	}

	@Override
	public String toString() {
		String typo3 = "@" + type + "{" + key;

		typo3 += genBibTeXAttributeIfPresent("type", this.typeAttrib);
		typo3 += genAuthorList();
		typo3 += genBibTeXAttributeIfPresent("title", title);
		typo3 += genBibTeXAttributeIfPresent("year", Integer.toString(year));
		typo3 += genBibTeXAttributeIfPresent("month", month);
		typo3 += genBibTeXAttributeIfPresent("booktitle", booktitle);
		typo3 += genBibTeXAttributeIfPresent("address", address);
		typo3 += genBibTeXAttributeIfPresent("publisher", publisher);
		typo3 += genBibTeXAttributeIfPresent("journal", journal);
		typo3 += genBibTeXAttributeIfPresent("location", location);
		typo3 += genBibTeXAttributeIfPresent("school", school);
		typo3 += genBibTeXAttributeIfPresent("pages", pages);
		typo3 += genBibTeXAttributeIfPresent("doi", doi);
		typo3 += genBibTeXAttributeIfPresent("isbn", isbn);
		typo3 += genBibTeXAttributeIfPresent("issn", issn);
		typo3 += genBibTeXAttributeIfPresent("note", note);
		typo3 += genBibTeXAttributeIfPresent("tags", tags.stream().reduce((a, b) -> a + ", " + b).orElseGet(() -> ""));

		return typo3 + "\n}";
	}
	
	public String getPaperURL() {
		StringBuilder b = new StringBuilder();
		b.append(PAPER_REPO_URL);
		b.append("/blob/master/");
		b.append(year);
		b.append("/");
		b.append(year);
		b.append("-");
		b.append(venueVariable);
		b.append("-");
		b.append(this.source.getLastnameOfFirstAuthor());
		return b.toString();
	}
	
	private String genAuthorList() {
		List<String> persons;
		String personType;
		if (!authors.isEmpty()) {
			persons = authors;
			personType = "author";
		} else if (!editors.isEmpty()) {
			persons = editors;
			personType = "editor";
		} else if (Filters.IS_MISC.test(this)) {
			return "";
		} else {
			throw new RuntimeException("The Typo3Entry with key " + this.key + " has neither authors nor editors!");
		}

		return genBibTeXAttributeIfPresent(personType,
				persons.stream()
				.reduce((a, b) -> a + " and " + b)
				.orElseGet(() -> {throw new IllegalArgumentException("Person list is empty!");}));
	}
	
	@Override
	public boolean equals(Object other) {
		return other != null && other instanceof Typo3Entry && ((Typo3Entry) other).title.equals(this.title);
	}

	@Override
	public int compareTo(Typo3Entry other) {
		return title.compareTo(other.title);
	}
	
	public static String makeTypo3Safe(String s) {
		return BibtexEntry.toURL(BibtexEntry.replaceUmlauts(s.trim()), TO_URL_OVERWRITES);
	}
	
	private static String lookup(String variable, final Map<String, String> variables) {
		String value = variables.get(variable.toUpperCase());
		return value == null ? variable : value;
	}
	
	private static String genBibTeXAttribute(String name, String value) {
		return ",\n  " + name + " = {" + value + "}";
	}
	
	private static String genBibTeXAttributeIfPresent(String name, String value) {
		return BibtexEntry.isDefined(value) ? genBibTeXAttribute(name, value) : "";
	}
	
	private static String parseBooktitle(BibtexEntry bib, Map<String, String> variables) {
		return makeTypo3Safe(Util.when(
				Filters.IS_TECHREPORT_BIB, 
				b -> ("Technical Report " + b.getAttribute(BibTeXEntry.KEY_NUMBER)).trim(),
				b -> lookup(b.getAttribute(BibTeXEntry.KEY_BOOKTITLE), variables)
				).apply(bib));
	}
	
	private static List<String> parseTags(BibtexEntry bib) {
		List<String> tags = bib.tagList.get(ExportTypo3Bibtex.TYPO3_TAGS_ATTRIBUTE);
		if (tags == null) {
			return new ArrayList<>();
		} else {
			return Util.splitAttributeListString(tags);
		}
	}
}