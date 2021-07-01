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
	private static final String SOFTVARE_PAPER_REPO_URL = "https://github.com/SoftVarE-Group/Papers/raw/master/";
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
	public String shortVenue; // the variable used in the booktitle or journal field in BibTags
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
	
	/// Empty by default. We only set the URL via modifiers as we have no general policy for where and how to get URLs.
	public String url = "";
	
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
		this.month = bib.getMonthAsNumber().map(i -> i.toString()).orElse("");

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
		this.tags = parseTags(bib);
		this.shortVenue = parseVenue(bib, variables);
	}

	@Override
	public String toString() {
		final StringBuilder typo3 = new StringBuilder();
		typo3.append("@").append(type).append("{").append(key);

		typo3.append(genBibTeXAttributeIfPresent("type", this.typeAttrib));
		typo3.append(genAuthorList());
		typo3.append(genBibTeXAttributeIfPresent("title", title));
		typo3.append(genBibTeXAttributeIfPresent("year", Integer.toString(year)));
		typo3.append(genBibTeXAttributeIfPresent("month", month));
		typo3.append(genBibTeXAttributeIfPresent("booktitle", booktitle));
		typo3.append(genBibTeXAttributeIfPresent("address", address));
		typo3.append(genBibTeXAttributeIfPresent("publisher", publisher));
		typo3.append(genBibTeXAttributeIfPresent("journal", journal));
		typo3.append(genBibTeXAttributeIfPresent("location", location));
		typo3.append(genBibTeXAttributeIfPresent("school", school));
		typo3.append(genBibTeXAttributeIfPresent("pages", pages));
		typo3.append(genBibTeXAttributeIfPresent("doi", doi));
		typo3.append(genBibTeXAttributeIfPresent("isbn", isbn));
		typo3.append(genBibTeXAttributeIfPresent("issn", issn));
		typo3.append(genBibTeXAttributeIfPresent("note", note));
		typo3.append(genBibTeXAttributeIfPresent("tags", tags.stream().reduce((a, b) -> a + ", " + b).orElseGet(() -> "")));
		typo3.append(genBibTeXAttributeIfPresent("url", url));
		
		typo3.append("\n}");
		
		return typo3.toString();
	}
	
	public String getPaperUrlInSoftVarERepo() {
		final String venue = makeTypo3Safe(shortVenue);
		
		final StringBuilder b = new StringBuilder();
		b.append(SOFTVARE_PAPER_REPO_URL);
		b.append(year);
		b.append("/");
		b.append(year);
		if (!venue.isEmpty()) {
			b.append("-");
			b.append(venue);
		}
		b.append("-");
		b.append(BibtexEntry.toURL(this.source.getLastnameOfFirstAuthor()));
		b.append(".pdf");
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
	
	public boolean isJournalPaper() {
		return BibtexEntry.isDefined(journal);
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
	
	private static String parseVenue(BibtexEntry bib, final Map<String, String> variables) {
		// If the venue is a not just a single word but a long name, we don't have a short venue name.
		if (Util.indexOfFirstMatch(bib.venue, Character::isWhitespace) < bib.venue.length()) {
			return "";
		}

		// Remove parenthesis such that, for example, "(techreport)" becomes "techreport".
		return bib.venue.replaceAll("[()]", "").trim();
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