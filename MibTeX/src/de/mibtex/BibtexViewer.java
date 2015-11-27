/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.jbibtex.BibTeXDatabase;
import org.jbibtex.BibTeXEntry;
import org.jbibtex.BibTeXObject;
import org.jbibtex.BibTeXParser;
import org.jbibtex.BibTeXString;
import org.jbibtex.Key;

/**
 * A class to generate an HTML page for a given BibTeX file.
 * 
 * @author Thomas Thuem
 */
public class BibtexViewer {

	public static String BIBTEX_DIR;

	public static String DROPBOX_DIR;

	public static String HTML_DIR;

	public static String PDF_DIR_REL;

	public static String PDF_DIR;

	public static String TAGS;

	/**
	 * Example arguments
	 * 
	 * BibtexViewer "C:\\Users\\tthuem\\workspace4.2.1\\tthuem-Bibtex\\"
	 * "C:\\Users\\tthuem\\Dropbox\\Literatur\\" "HTML\\" "..\\Library\\"
	 * "Library\\" "tt-tags"
	 * 
	 * @param args array containing 
	 *            path to Bibtex file
	 *            path to main directory
	 *            relative path of the HTML to main directory
	 *            relative path of PDF files to the HTML folder (for linking files in HTML) 
	 *            relative path of PDF files to main directory
	 *            name of the tag containing your keywords
	 */
	public static void main(String[] args) {
		BIBTEX_DIR = args[0];
		DROPBOX_DIR = args[1];
		HTML_DIR = DROPBOX_DIR + args[2];
		PDF_DIR_REL = args[3];
		PDF_DIR = DROPBOX_DIR + args[4];
		TAGS = args[5];
		try {
			new BibtexViewer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	List<BibtexEntry> entries;

	List<String> authors;

	List<String> titles;

	List<Integer> years;

	List<String> venues;

	List<String> tags;

	public BibtexViewer() throws Exception {
		// cleanOutputFolder();
		parseBibTeX("literature.bib");
		printMissingPDFs();
		writeIndex();
		writeAuthors();
		writeYears();
		writeVenues();
		writeTags();
		writeLists();
		renameFiles();
	}

	private void printMissingPDFs() {
		for (BibtexEntry entry : entries) {
			File file = entry.getPDF();
			if (!file.exists())
				System.out.println(file.getName());
		}
		System.out.println();
	}

	void renameFiles() {
		List<File> available = new ArrayList<File>();
		List<BibtexEntry> missing = new ArrayList<BibtexEntry>();
		for (File file : new File(BibtexViewer.PDF_DIR).listFiles())
			available.add(file);
		for (BibtexEntry entry : entries) {
			File file = entry.getPDF();
			if (file.exists()) {
				if (!available.remove(file))
					System.err.println("File comparison failed: " + file);
			} else {
				if (!"misc book".contains(entry.entry.getType().getValue()))
					missing.add(entry);
			}
		}
		System.out.println("Correct = " + (entries.size() - missing.size())
				+ ", Available = " + available.size() + ", Missing = "
				+ missing.size());
		System.out.println();
		Scanner answer = new Scanner(System.in);
		while (!available.isEmpty()) {
			int minDistance = Integer.MAX_VALUE;
			BibtexEntry missingEntry = null;
			File availableFile = null;
			for (BibtexEntry entry : missing) {
				for (File file : available) {
					int distance = Levenshtein.getDistance(file.getName(),
							entry.getPDF().getName());
					if (distance < minDistance) {
						minDistance = distance;
						missingEntry = entry;
						availableFile = file;
					}
				}
			}
			// stop if names are too different from each other
			if (minDistance > availableFile.getName().length() * 0.7)
				break;
			if (availableFile != null) {
				System.out.println("Available: " + availableFile.getName());
				System.out.println("Missing: "
						+ missingEntry.getPDF().getName());
				System.out.println("Key: "
						+ missingEntry.entry.getKey().getValue());
				System.out.println("Distance: " + minDistance);
				System.out.println("Remaining: " + missing.size());
				if (answer.next().equals("y")) {
					if (availableFile.renameTo(missingEntry.getPDF()))
						available.remove(availableFile);
					else
						System.err
								.println("Renaming from \""
										+ availableFile.getAbsolutePath()
										+ "\" to \"" + missingEntry.getPDF()
										+ "\" did not succeed!");
				}
				missing.remove(missingEntry);
			}
		}
		answer.close();
		System.out.println();
		for (File file : available) {
			if (!file.getName().startsWith("0"))
				file.renameTo(new File(file.getParentFile(), "0"
						+ file.getName()));
			System.out.println("Available: " + file.getName());
		}
	}

	void cleanOutputFolder() {
		for (File file : new File(HTML_DIR).listFiles())
			file.delete();
	}

	void writeIndex() throws IOException {
		writeToHTML("index.htm", new BibtexFilter() {
			@Override
			public String getTitle() {
				return "Literature";
			}

			@Override
			public boolean include(BibtexEntry entry) {
				return true;
			}
		});
	}

	void writeAuthors() throws IOException {
		for (final String author : authors) {
			writeToHTML(BibtexEntry.toURL(author) + ".htm", new BibtexFilter() {
				@Override
				public String getTitle() {
					return "Author = " + author;
				}

				@Override
				public boolean include(BibtexEntry entry) {
					return entry.authorList.contains(author);
				}
			});
		}
	}

	void writeYears() throws IOException {
		for (final int year : years) {
			writeToHTML(year + ".htm", new BibtexFilter() {
				@Override
				public String getTitle() {
					return "Year = " + year;
				}

				@Override
				public boolean include(BibtexEntry entry) {
					return entry.year == year;
				}
			});
		}
	}

	void writeVenues() throws IOException {
		for (final String venue : venues) {
			writeToHTML(venue + ".htm", new BibtexFilter() {
				@Override
				public String getTitle() {
					return "Venue = " + venue;
				}

				@Override
				public boolean include(BibtexEntry entry) {
					return entry.venue.equalsIgnoreCase(venue);
				}
			});
		}
	}

	void writeTags() throws IOException {
		for (final String tag : tags) {
			writeToHTML(BibtexEntry.toURL(tag) + ".htm", new BibtexFilter() {
				@Override
				public String getTitle() {
					return "Tag = " + tag;
				}

				@Override
				public boolean include(BibtexEntry entry) {
					return entry.tagList.contains(tag);
				}
			});
		}
	}

	void writeLists() {
		writeList("Authors", authors);
		writeList("Titles", titles);
		writeList("Venues", venues);
		writeList("Tags", tags);
		writeList("Years", years);
	}

	void writeList(String title, List<?> list) {
		String filename = BibtexEntry.toURL(title) + ".htm";
		StringBuilder content = new StringBuilder();
		writeHeader(content, title);
		content.append("<center><h1>" + title
				+ " <a href=\"index.htm\">(X)</a>");
		content.append("</h1></center>\r\n");
		content.append(list.size() + "<br/><br/>\r\n");
		for (Object o : list) {
			content.append("<a href=\"");
			content.append(BibtexEntry.toURL(o.toString()));
			content.append(".htm\">" + o + "</a><br/>\r\n");
		}
		writeFooter(content);
		writeToFile(filename, content.toString());
	}

	public void parseBibTeX(String file) throws Exception {
		Reader reader = new FileReader(new File(BIBTEX_DIR + file));
		try {
			BibTeXParser parser = new BibTeXParser() {
				@Override
				public void checkStringResolution(Key key, BibTeXString string) {
				}

				@Override
				public void checkCrossReferenceResolution(Key key,
						BibTeXEntry entry) {
				}
			};
			BibTeXDatabase database = parser.parse(reader);
			extractEntries(database);
		} finally {
			reader.close();
		}
		readAuthors();
		readTitles();
		readYears();
		readVenues();
		readTags();
	}

	private void readAuthors() {
		authors = new ArrayList<String>();
		for (BibtexEntry entry : entries)
			for (String author : entry.authorList)
				if (!authors.contains(author))
					authors.add(author);
		Collections.sort(authors);
	}

	private void readTitles() {
		titles = new ArrayList<String>();
		for (BibtexEntry entry : entries)
			titles.add(entry.title);
		Collections.sort(titles);
	}

	private void readYears() {
		years = new ArrayList<Integer>();
		for (BibtexEntry entry : entries)
			if (!years.contains(entry.year))
				years.add(entry.year);
		Collections.sort(years);
	}

	private void readVenues() {
		venues = new ArrayList<String>();
		for (BibtexEntry entry : entries)
			if (!venues.contains(entry.venue))
				venues.add(entry.venue);
		Collections.sort(venues);
	}

	private void readTags() {
		tags = new ArrayList<String>();
		for (BibtexEntry entry : entries)
			for (String tag : entry.tagList)
				if (!tags.contains(tag))
					tags.add(tag);
		Collections.sort(tags);
	}

	private void extractEntries(BibTeXDatabase database) {
		entries = new ArrayList<BibtexEntry>();
		for (BibTeXObject object : database.getObjects())
			if (object instanceof BibTeXEntry)
				entries.add(new BibtexEntry((BibTeXEntry) object));
	}

	void writeToHTML(String filename, BibtexFilter filter) {
		StringBuilder content = new StringBuilder();
		writeHeader(content, "Literature");
		writeBibtex(content, filter);
		writeFooter(content);
		writeToFile(filename, content.toString());
	}

	void writeToFile(String filename, String content) {
		try {
			File file = new File(HTML_DIR + filename);
			String oldContent = readFromFile(file);
			if (!content.equals(oldContent)) {
				System.out.println("Updating " + filename);
				BufferedWriter out = new BufferedWriter(new FileWriter(file));
				out.write(content.toString());
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	String readFromFile(File file) {
		try {
			InputStream in = new FileInputStream(file);
			StringBuilder out = new StringBuilder();
			byte[] b = new byte[4096];
			for (int n; (n = in.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
			in.close();
			return out.toString();
		} catch (Exception e) {
		}
		return null;
	}

	private void writeHeader(StringBuilder builder, String title) {
		builder.append("<html><head>");
		builder.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">");
		builder.append("<style type=\"text/css\">body {font-family:Verdana; color:red;} a {text-decoration:none; color:black;} table {border-collapse:collapse;} td,th {border:thin solid lightgray; padding:3px; text-align:center;}</style>");
		builder.append("<title>");
		builder.append(title);
		builder.append("</title>");
		builder.append("</head><body>\r\n");
	}

	private void writeFooter(StringBuilder builder) {
		builder.append("</body></html>\r\n");
	}

	void writeBibtex(StringBuilder builder, BibtexFilter filter) {
		builder.append("<center><h1>" + filter.getTitle());
		if (!filter.getTitle().equalsIgnoreCase("Literature"))
			builder.append(" <a href=\"index.htm\">(X)</a>");
		builder.append("</h1></center>\r\n");
		builder.append(countEntries(filter));
		builder.append("<table><tr>");
		writeTableHeading(builder, "Author");
		writeTableHeading(builder, "Title");
		writeTableHeading(builder, "Venue");
		writeTableHeading(builder, "Tag");
		// builder.append("<th>Cites</th>");
		writeTableHeading(builder, "Year");
		builder.append("</tr>\r\n");
		for (BibtexEntry entry : entries)
			if (filter.include(entry)) {
				builder.append("<tr>\r\n<td>");
				builder.append(entry.getHTMLAuthor() + "</td><td>");
				builder.append(entry.getHTMLTitle() + "</td><td>");
				builder.append(entry.getHTMLVenue() + "</td><td>");
				builder.append(entry.getHTMLTags() + "</td><td>");
				// builder.append(entry.getCitations() + "</td><td>");
				builder.append(entry.getHTMLYear() + "</td>\r\n");
				builder.append("</tr>\r\n");
			}
		builder.append("</table>\r\n");
	}

	private void writeTableHeading(StringBuilder builder, String title) {
		builder.append("<th><a href=\"" + title + "s.htm\">" + title
				+ "</a></th>");
	}

	private long countEntries(BibtexFilter filter) {
		long number = 0;
		for (BibtexEntry entry : entries)
			if (filter.include(entry))
				number++;
		return number;
	}

}
