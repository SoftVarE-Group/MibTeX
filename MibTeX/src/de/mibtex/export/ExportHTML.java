/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.IOException;
import java.util.List;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexFilter;
import de.mibtex.BibtexViewer;

/**
 * A class to generate an HTML page for a given BibTeX file.
 * 
 * @author Thomas Thuem
 */
public class ExportHTML extends Export {

	public ExportHTML(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		try {
			writeIndex();
			writeAuthors();
			writeYears();
			writeVenues();
			writeTags();
			writeLists();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
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
					return entry.tagList.values().contains(tag);
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
		writeToFile(BibtexViewer.OUTPUT_DIR, filename, content.toString());
	}

	void writeToHTML(String filename, BibtexFilter filter) {
		StringBuilder content = new StringBuilder();
		writeHeader(content, "Literature");
		writeBibtex(content, filter);
		writeFooter(content);
		writeToFile(BibtexViewer.OUTPUT_DIR, filename, content.toString());
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
		writeTableHeading(builder, "Cites");
		writeTableHeading(builder, "Year");
		builder.append("</tr>\r\n");
		for (BibtexEntry entry : entries.values())
			if (filter.include(entry)) {
				builder.append("<tr>\r\n<td>");
				builder.append(getHTMLAuthor(entry) + "</td><td>");
				builder.append(getHTMLTitle(entry) + "</td><td>");
				builder.append(getHTMLVenue(entry) + "</td><td>");
				builder.append(getHTMLTags(entry) + "</td><td>");
				builder.append(getHTMLCitations(entry) + "</td><td>");
				builder.append(getHTMLYear(entry) + "</td>\r\n");
				builder.append("</tr>\r\n");
			}
		builder.append("</table>\r\n");
	}

	private void writeTableHeading(StringBuilder builder, String title) {
		builder.append("<th><a href=\"" + title + "s.htm\">" + title
				+ "</a></th>");
	}

	private String getHTMLAuthor(BibtexEntry entry) {
		String s = "";
		for (String author : entry.authorList)
			s += "<a href=\"" + BibtexEntry.toURL(author) + ".htm\">" + author
					+ "</a>, ";
		return s.substring(0, s.length() - 2);
	}

	public static String getHTMLTitle(BibtexEntry entry) {
		String title = "unspecified"; // for misc entries
		if (BibtexEntry.isDefined(entry.title)) {
			title = entry.title;
		}
		
		String htmlTitle = "<a href=\"" + entry.getRelativePDFPath() + "\">";
		if (entry.getPDFPath().exists()) {
			htmlTitle += title;
		} else {
			//System.out.println("Entry \"" + entry.getPDFPath() + "\" does not exist!");
			htmlTitle = title + " " + htmlTitle + "pdf";
		}
		return htmlTitle + "</a>";
	}

	private String getHTMLVenue(BibtexEntry entry) {
		return "<a href=\"" + entry.venue + ".htm\">" + entry.venue + "</a>";
	}

	private String getHTMLTags(BibtexEntry entry) {
		String s = entry.key + ", ";
		for (List<String> tags : entry.tagList.values())
			for (String tag : tags)
			s += "<a href=\"" + BibtexEntry.toURL(tag) + ".htm\">" + tag
					+ "</a>, ";
		return s.substring(0, s.length() - 2);
	}

	private String getHTMLCitations(BibtexEntry entry) {
		return "<a href=\"http://scholar.google.de/scholar?q=" + entry.title
				+ "\" target=\"scholar_window\">" + entry.getCitations()
				+ "</a>";
	}

	private String getHTMLYear(BibtexEntry entry) {
		return "<a href=\"" + entry.year + ".htm\">" + entry.year + "</a>";
	}

}
