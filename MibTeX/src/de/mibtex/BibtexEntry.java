/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import de.mibtex.citationservice.CitationEntry;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.Key;
import org.jbibtex.Value;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

/**
 * A class storing a single BibTeX entry with several options for manipulation.
 * 
 * @author Thomas Thuem, Christopher Sontag, Paul Maximilian Bittner
 */
public class BibtexEntry {
	private static final String UNKNOWN_ATTRIBUTE = "unknown";
	private static final String EMPTY_ATTRIBUTE = "";
	private static final Map<String, Integer> MONTH_NAME_TO_NUMBER;

	public List<Key> KEY_LIST = new ArrayList<>();
	// public static final Key KEY_TT_TAGS = new Key(BibtexViewer.TAGS);

	public BibTeXEntry entry = null;
	
	public String type = UNKNOWN_ATTRIBUTE;
	public String key = UNKNOWN_ATTRIBUTE;

	public String author = UNKNOWN_ATTRIBUTE;
	public List<String> authorList = new ArrayList<>();
	public boolean authorsAreEditors = false;

	public String title = UNKNOWN_ATTRIBUTE;
	public String venue = UNKNOWN_ATTRIBUTE;

	public String doi = EMPTY_ATTRIBUTE;
	public String url = EMPTY_ATTRIBUTE;

	public int year = 0;

	public List<String> tags = new ArrayList<>();
	public LinkedHashMap<String, List<String>> tagList = new LinkedHashMap<>();

	public int citations = CitationEntry.NOT_IN_CITATION_SERVICE;
	public long lastUpdate = 0;
	
	static {
		MONTH_NAME_TO_NUMBER = new HashMap<>();
		MONTH_NAME_TO_NUMBER.put("january", 1);
		MONTH_NAME_TO_NUMBER.put("february", 2);
		MONTH_NAME_TO_NUMBER.put("march", 3);
		MONTH_NAME_TO_NUMBER.put("april", 4);
		MONTH_NAME_TO_NUMBER.put("may", 5);
		MONTH_NAME_TO_NUMBER.put("june", 6);
		MONTH_NAME_TO_NUMBER.put("july", 7);
		MONTH_NAME_TO_NUMBER.put("august", 8);
		MONTH_NAME_TO_NUMBER.put("september", 9);
		MONTH_NAME_TO_NUMBER.put("october", 10);
		MONTH_NAME_TO_NUMBER.put("november", 11);
		MONTH_NAME_TO_NUMBER.put("december", 12);
	}

	public BibtexEntry(BibTeXEntry entry) {
		for (String tagKey : BibtexViewer.TAGS) {
			KEY_LIST.add(new Key(tagKey));
		}
		this.entry = entry;
		parseKey();
		parseType();
		parseAuthor();
		parseTitle();
		parseVenue();
		parseYear();
		parseTags();
		parseDOIandURL();
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
		System.err.println("[BibtexEntry(String,String,String,String,List<String>,int,int)] Field type remains unitialized!");
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

	private String getYearPath() {
		String folder1 = (year - year % 10) + "s";
		if (year < 1970) return "0000s";
		if (year < 1990) return folder1;
		String folder2 = year + "";
        return FileUtils.concat(folder1, folder2).toString();
	}

	private File getCommentsFile() {
		String path = getYearPath();
		String pdf = key.replace(":", "-").trim();
		pdf += "-comments.pdf";
		return FileUtils.concat(path, pdf);
	}

	private File getPDFFile() {
		String path = getYearPath();
		String pdf = key.replace(":", "-").trim();
		pdf += ".pdf";
		return FileUtils.concat(path, pdf);
	}

	public File getCommentsPath() {
		return FileUtils.concat(BibtexViewer.COMMENTS_DIR, getCommentsFile().toString());
	}

	public File getPDFPath() {
		return FileUtils.concat(BibtexViewer.PDF_DIR, getPDFFile().toString());
	}

	@Deprecated
	public File getOldPDFPath() {
		String pdf = "";
		
		if (!authorList.isEmpty()) {
			pdf += getLastnameOfFirstAuthor();
			if (authorList.size() == 2)
				pdf += " and " + getLastnameOfAuthorNo(1);
			else if (authorList.size() > 2)
				pdf += " et al.";
		}
		if (!venue.startsWith("("))
			pdf += " " + venue;
		if (year > 0)
			pdf += " " + year;
		if (!EMPTY_ATTRIBUTE.equals(title)) {
			pdf += " " + title;
		} else {
			pdf += " " + key;
		}
		pdf = pdf.trim() + ".pdf";
		return FileUtils.concat(BibtexViewer.MAIN_DIR, toURL(pdf));
	}

	public String getRelativeCommentsPath() {
		return FileUtils.concat(BibtexViewer.COMMENTS_DIR_REL, getCommentsFile().toString()).toString();
	}

	@Deprecated
	public String getOldRelativePDFPath() {
		return getOldPDFPath().getName();
	}

	private String getLastname(String name) {
		return name.substring(name.lastIndexOf(" ") + 1);
	}
	
	public String getLastnameOfAuthorNo(int authorIndex) {
		return getLastname(authorList.get(authorIndex));
	}
	
	public String getLastnameOfFirstAuthor() {
		return getLastnameOfAuthorNo(0);
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
				Value field = entry.getField(BibTeXEntry.KEY_AUTHOR);
				authorsAreEditors = false;
				
				if (field == null) {
					field = entry.getField(BibTeXEntry.KEY_EDITOR);
					authorsAreEditors = true;
				}
				
				if (field == null) {
					author = EMPTY_ATTRIBUTE;
					if (!isMisc()) {
						System.err.println("[BibtexEntry.parseAuthor] Warning: " + key + " does neither have authors nor editors!");
					}
				} else {
					author = field.toUserString();
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
		} catch (Exception e) { // What types of exceptions are expected here?
			if (authorList.equals(UNKNOWN_ATTRIBUTE))
				authorList.add(author);
			e.printStackTrace();
		}
	}

	void parseTitle() {
		if (title.equals(UNKNOWN_ATTRIBUTE)) {
			Value field = entry.getField(BibTeXEntry.KEY_TITLE);
			if (field == null) {
				title = EMPTY_ATTRIBUTE;
				if (!isMisc()) {
					System.err.println("[BibtexEntry.parseTitle] Warning: " + key + " does not have a title!");
				}
			} else {
				title = field.toUserString();
			}
		}
		title = replaceUmlauts(title);
	}

	void parseVenue() {
		if (venue.equals(UNKNOWN_ATTRIBUTE)) {
			venue = "(" + entry.getType().getValue() + ")";
			if (venue.equalsIgnoreCase("(incollection)"))
				return;
			try {
				venue = entry.getField(BibTeXEntry.KEY_BOOKTITLE).toUserString().toUpperCase();
			} catch (Exception e) {
			}
			try {
				venue = entry.getField(BibTeXEntry.KEY_JOURNAL).toUserString().toUpperCase();
			} catch (Exception e) {
			}
		}
	}

	void parseDOIandURL() {
		if (doi.equals(EMPTY_ATTRIBUTE)) {
			try {
				doi = entry.getField(BibTeXEntry.KEY_DOI).toUserString();
			} catch (Exception e) {
			}
		}
		if (url.equals(EMPTY_ATTRIBUTE)) {
			try {
				url = entry.getField(BibTeXEntry.KEY_URL).toUserString();
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
	
	boolean isMisc() {
		return "misc".equals(type);
	}
	
	public Optional<Integer> getMonthAsNumber() {
		final String monthName = getAttribute(BibTeXEntry.KEY_MONTH).toLowerCase();
		if (MONTH_NAME_TO_NUMBER.containsKey(monthName)) {
			return Optional.of(MONTH_NAME_TO_NUMBER.get(monthName));
		}
		return Optional.empty();
	}

	/**
	 * Returns the value as string associated to the given bibtex key.
	 * Returns an empty string if no such key could be found.
	 */
	public String getAttribute(org.jbibtex.Key attribKey) {
		org.jbibtex.Value attrib = this.entry.getField(attribKey);
		return attrib != null ? attrib.toUserString() : "";
	}
	/**
	 * Returns the value as string associated to the given bibtex key name.
	 * Returns an empty string if no such key could be found.
	 */
	public String getAttribute(String attribKey) {
		return getAttribute(new org.jbibtex.Key(attribKey));
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
		HashMap<String, String> replacements = new HashMap<>(overwrites);
		replacements.putIfAbsent("&auml;", "ae");
		replacements.putIfAbsent("&ouml;", "oe");
		replacements.putIfAbsent("&uuml;", "ue");
		replacements.putIfAbsent("&Auml;", "Ae");
		replacements.putIfAbsent("&Ouml;", "Oe");
		replacements.putIfAbsent("ï¿½", "O");
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

	public static boolean isDefined(String attribute) {
		return
				attribute != null
				&& !attribute.isEmpty()
				&& !attribute.equals(BibtexEntry.UNKNOWN_ATTRIBUTE)
				&& !attribute.equals(BibtexEntry.EMPTY_ATTRIBUTE)
				&& (!attribute.startsWith("(") || !attribute.endsWith(")"));
	}
}
