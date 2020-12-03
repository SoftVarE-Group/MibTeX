package de.mibtex.export.typo3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbibtex.BibTeXEntry;

import de.mibtex.BibtexEntry;
import de.mibtex.export.ExportTypo3Bibtex;

public class Typo3Entry implements Comparable<Typo3Entry> {
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
		ToURLOverwrites.put("#", "#");
		ToURLOverwrites.put("&szlig;", "ß");
	}
	
	public final BibtexEntry source;
	
	public String type; // inproceedings, article, ...
	public String key;
	
	public String typeAttrib; // type = {...}
	public String title;
	public List<String> authors;
	public List<String> editors;
	public String booktitle;
	public String address;
	public String publisher;
	public String journal;
	public String location;
	public String school;
	public String pages;
	public int year;
	
	public String doi;
	public String isbn;
	public String issn;
	
	public String note;
	
	public List<String> tags;
	
	public Typo3Entry(BibtexEntry bib, Map<String, String> variables) {
		this.source = bib;
		
		this.type = bib.type;
		this.typeAttrib = MakeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_TYPE));
		this.key = bib.key;

		this.authors = new ArrayList<>();
		this.editors = new ArrayList<>();
		List<String> persons = bib.authorList.stream().map(Typo3Entry::MakeTypo3Safe).collect(Collectors.toList());
		if (bib.authorsAreEditors) {
			this.editors = persons;
		} else {
			this.authors = persons;
		}
		
		this.title = MakeTypo3Safe(bib.title);
		this.year = bib.year;

		this.address = MakeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_ADDRESS), variables));
		this.publisher = MakeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_PUBLISHER), variables));
		this.journal = MakeTypo3Safe(lookup(bib.getAttribute(BibTeXEntry.KEY_JOURNAL), variables));
		this.location = MakeTypo3Safe(lookup(bib.getAttribute("location"), variables));
		
		this.school = MakeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_SCHOOL));
		this.pages = MakeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_PAGES));
		
		this.doi = MakeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_DOI));
		this.isbn = bib.getAttribute("isbn");
		this.issn = bib.getAttribute("issn");
		
		this.note = MakeTypo3Safe(bib.getAttribute(BibTeXEntry.KEY_NOTE));
		
		{
			String booktitle;
			if (Filters.Is_techreport.test(this)) {
				booktitle = ("Technical Report " + bib.getAttribute(BibTeXEntry.KEY_NUMBER)).trim();
//			} else if (Filters.is_phdthesis.test(bib)) {
//				booktitle = "PhD Thesis";
//			} else if (Filters.is_bachelorsthesis.test(bib)) {
//				booktitle = "Bachelor's Thesis";
//			} else if (Filters.is_mastersthesis.test(bib)) {
//				booktitle = "Master's Thesis";
			} else {
				booktitle = lookup(bib.getAttribute(BibTeXEntry.KEY_BOOKTITLE), variables);
			}
			this.booktitle = MakeTypo3Safe(booktitle);
		}
		
		this.tags = bib.tagList.get(ExportTypo3Bibtex.Typo3TagsAttribute);
		if (this.tags == null) {
			this.tags = new ArrayList<>();
		} else {
			this.tags = Util.SplitAttributeListString(this.tags);
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
		
		typo3 += GenBibTeXAttributeIfPresent("tags", tags.stream().reduce((a, b) -> a + ", " + b).orElseGet(() -> ""));

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
	
	public static String MakeTypo3Safe(String s) {
		return BibtexEntry.toURL(BibtexEntry.replaceUmlauts(s.trim()), ToURLOverwrites);
	}
	
	private static String lookup(String variable, final Map<String, String> variables) {
		String value = variables.get(variable.toUpperCase());
		return value == null ? variable : value;
	}
	
	private static String GenBibTeXAttribute(String name, String value) {
		return ",\n  " + name + " = {" + value + "}";
	}
	
	private static String GenBibTeXAttributeIfPresent(String name, String value) {
		return BibtexEntry.IsDefined(value) ? GenBibTeXAttribute(name, value) : "";
	}
}