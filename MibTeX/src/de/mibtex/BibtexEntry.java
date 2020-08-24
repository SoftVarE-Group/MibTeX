/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.Value;

import de.mibtex.citationservice.CitationEntry;

/**
 * A class storing a single BibTeX entry with several options for manipulation.
 * 
 * @author Thomas Thuem, Christopher Sontag
 */
public class BibtexEntry {

	private static final String UNKNOWN_ATTRIBUTE = "unknown";

	public List<Key> KEY_LIST = new ArrayList<>();
	// public static final Key KEY_TT_TAGS = new Key(BibtexViewer.TAGS);

	public BibTeXEntry entry = null;
	
	public String type = UNKNOWN_ATTRIBUTE;
	public String key = UNKNOWN_ATTRIBUTE;

	public String author = UNKNOWN_ATTRIBUTE;
	public List<String> authorList = new ArrayList<String>();
	public boolean authorsAreEditors = false;

	public String title = UNKNOWN_ATTRIBUTE;
	public String venue = UNKNOWN_ATTRIBUTE;

	public int year = 0;

	public List<String> tags = new ArrayList<>();
	public LinkedHashMap<String, List<String>> tagList = new LinkedHashMap<>();

	public int citations = CitationEntry.NOT_IN_CITATION_SERVICE;
	public long lastUpdate = 0;

	public BibtexEntry(BibTeXEntry entry) {
		for (String tagKey : BibtexViewer.TAGS) {
			KEY_LIST.add(new Key(tagKey));
		}
		this.entry = entry;
		parseKey();
		parseAuthor();
		parseTitle();
		parseVenue();
		parseYear();
		parseTags();
		parseType();
	}

	public BibtexEntry(String key, String author, String title, String venue, List<String> tags, int year,
			int citations) {
		this.key = key;
		this.author = author;
		this.title = title;
		this.venue = venue;
		this.tags = tags;
		this.year = year;
		this.citations = citations;
		parseAuthor();
		parseTags();
	}

	public int getCitations() {
		return citations;
	}

	public String getCitationsPerYear() {
		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
		double totalYears = currentYear - year;
		if (citations <= 0 || year <= 0 || totalYears < 2)
			return citations + "";
		return (int) (citations / totalYears + 0.5) + " (" + citations + ")";
	}

	public File getPDFPath() {
		String pdf = getLastname(authorList.get(0));
		if (authorList.size() == 2)
			pdf += " and " + getLastname(authorList.get(1));
		else if (authorList.size() > 2)
			pdf += " et al.";
		if (!venue.startsWith("("))
			pdf += " " + venue;
		if (year > 0)
			pdf += " " + year;
		pdf += " " + title;
		pdf += ".pdf";
		return new File(BibtexViewer.PDF_DIR, toURL(pdf));
	}

	public String getRelativePDFPath() {
		return new File(BibtexViewer.PDF_DIR_REL, getPDFPath().getName()).toString();
	}

	private String getLastname(String name) {
		return name.substring(name.lastIndexOf(" ") + 1);
	}

	void parseKey() {
		try {
			if (key.equals(UNKNOWN_ATTRIBUTE)) {
				key = entry.getKey().getValue();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void parseAuthor() {
		try {
			if (author.equals(UNKNOWN_ATTRIBUTE)) {
				try {
					author = entry.getField(BibTeXEntry.KEY_AUTHOR).toUserString();
					authorsAreEditors = false;
				} catch (Exception e) {
					author = entry.getField(BibTeXEntry.KEY_EDITOR).toUserString();
					authorsAreEditors = true;
				}
			}
			author = replaceUmlauts(author);
			author = author.replace(" and ", "; ");
			while (author.contains(",")) {
				int comma = author.indexOf(",");
				int before = author.lastIndexOf("; ", comma) + 2;
				if (before < 2)
					before = 0;
				int after = author.indexOf("; ", comma);
				if (after < 0)
					after = author.length();
				author = author.substring(0, before) + author.substring(comma + 1, after) + " "
						+ author.substring(before, comma) + author.substring(after);
			}
			author = author.replace("; ", ", ");
			StringTokenizer tokenizer = new StringTokenizer(author, ",");
			while (tokenizer.hasMoreTokens())
				authorList.add(tokenizer.nextToken().trim());
		} catch (Exception e) {
			if (authorList.equals(UNKNOWN_ATTRIBUTE))
				authorList.add(author);
			e.printStackTrace();
		}
	}

	void parseTitle() {
		try {
			if (title.equals(UNKNOWN_ATTRIBUTE)) {
				title = entry.getField(BibTeXEntry.KEY_TITLE).toUserString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		title = replaceUmlauts(title);
	}

	void parseVenue() {
		if (venue.equals(UNKNOWN_ATTRIBUTE)) {
			venue = "(" + entry.getType().getValue() + ")";
			if (venue.equalsIgnoreCase("(incollection)"))
				return;
			try {
				venue = entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString();
			} catch (Exception e) {
			}
			try {
				venue = entry.getField(BibTeXEntry.KEY_JOURNAL).toUserString();
			} catch (Exception e) {
			}
		}
	}

	void parseYear() {
		if (year == 0) {
			try {
				String yearString = entry.getField(BibTeXEntry.KEY_YEAR).toUserString();
				year = Integer.parseInt(yearString);
			} catch (Exception e) {
			}
		}
	}

	void parseTags() {
		try {
			if (tags.isEmpty()) {
				for (Key key : KEY_LIST) {
					List<String> tagsForKey = new ArrayList<>();
					Value value = entry.getField(key);
					if (value != null) {
						String tag = value.toUserString();
						tag = replaceUmlauts(tag);
						tags.add(tag);
						StringTokenizer tokenizer = new StringTokenizer(tag, ",");
						while (tokenizer.hasMoreTokens())
							tagsForKey.add(tokenizer.nextToken().trim());

						tagList.put(key.getValue(), tagsForKey);
					}
				}
				// tags = entry.getField(KEY_TT_TAGS).toUserString();
			}

		} catch (Exception e) {
			System.out.println("Parsing tag list failed for unknown reason");
		}
	}
	
	void parseType() {
		type = entry.getType().getValue();
	}

	public static String replaceUmlauts(String s) {
		s = s.replace("\\\"{", "{\\\"");
		s = s.replace("\\\"a", "&auml;");
		s = s.replace("\\\"o", "&ouml;");
		s = s.replace("\\\"u", "&uuml;");
		s = s.replace("\\\"A", "&Auml;");
		s = s.replace("\\\"O", "&Ouml;");
		s = s.replace("\\\"U", "&Uuml;");
		s = s.replace("\\ss", "&szlig;");
		s = s.replace("\\&\\#536;", "S");
		s = s.replace("\\&", "&amp;");
		s = s.replace("\\#", "#");
		s = s.replace("\\c", "");
		s = s.replace("\\v", "");
		s = s.replace("\\u", "");
		s = s.replace("\\l", "l");
		s = s.replace("\\i", "i");
		s = s.replace("\\k", "");
		s = s.replace("\\^", "");
		s = s.replace("\\'", "");
		s = s.replace("\\`", "");
		s = s.replace("\\,", " ");
		s = s.replace("\\~", "");
		s = s.replace("\\\"", "");
		s = s.replace("\\", "");
		s = s.replace("---", "&#8211;");
		s = s.replace("--", "&#8211;");
		s = s.replace("{", "");
		s = s.replace("}", "");
		s = s.replaceAll("\\s+", " ");
		return s;
	}

	public static String toURL(String s) {
		return toURL(s, new HashMap<>());
	}
	
	public static String toURL(String s, Map<String, String> overwrites) {
		HashMap<String, String> replacements = new HashMap<String, String>(overwrites);
		replacements.putIfAbsent("&auml;", "ae");
		replacements.putIfAbsent("&ouml;", "oe");
		replacements.putIfAbsent("&uuml;", "ue");
		replacements.putIfAbsent("&Auml;", "Ae");
		replacements.putIfAbsent("&Ouml;", "Oe");
		replacements.putIfAbsent("�", "O");
		replacements.putIfAbsent("&Uuml;", "Ue");
		replacements.putIfAbsent("&szlig;", "ss");
		replacements.putIfAbsent("&amp;", "and");
		replacements.putIfAbsent("&#8211;", "-");
		replacements.putIfAbsent(":", "");
		replacements.putIfAbsent("?", "");
		replacements.putIfAbsent("\\", "");
		replacements.putIfAbsent("/", "");
		replacements.putIfAbsent("#", "");
		
		for (Entry<String, String> entry : replacements.entrySet()) {
			s = s.replace(entry.getKey(), entry.getValue());
		}
		
		return s;
	}

	@Override
	public String toString() {
		return "BibtexEntry [entry=" + entry + ", key=" + key + ", author=" + author + ", authorList=" + authorList
				+ ", title=" + title + ", venue=" + venue + ", tags=" + tags + ", tagList=" + tagList + ", year=" + year
				+ ", citations=" + citations + "]";
	}

	public static boolean IsDefined(String attribute) {
		return attribute != null && !attribute.isEmpty() && !attribute.equals(BibtexEntry.UNKNOWN_ATTRIBUTE) && (!attribute.startsWith("(") || !attribute.endsWith(")"));
	}
}
