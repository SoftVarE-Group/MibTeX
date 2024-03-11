/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.File;
import java.util.*;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Typo3Entry;

/**
 * A class that generates a single .html file with all BibTeX entries
 *
 * @author Christopher Sontag
 */
public class ExportNewHTML extends Export {
    private Map<String, String> bibTagsVariables;
    
    public ExportNewHTML(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        bibTagsVariables = ExportTypo3Bibtex.readVariablesFromBibtexFile(new File(
                BibtexViewer.BIBTEX_DIR,
                ExportTypo3Bibtex.VariablesFile
        ));
        
        String input = readFromFile("resources/", new File("index_in.html"));
        StringBuilder HTML = new StringBuilder();
        Set<String> venues = new HashSet<>();
        Set<String> tags = new HashSet<>();
        Set<Integer> years = new HashSet<>();
        for (BibtexEntry entry : entries.values()) {
            HTML.append("<tr id=\"").append(entry.key).append("\">")
                    .append("<td>").append(generateAuthorLinks(entry)).append("</td>")
                    .append("<td>").append(generateTitleLink(entry)).append("</td>")
                    .append("<td>").append(generateVenueLink(entry)).append("</td>")
                    .append("<td>").append(generateTagLinks(entry)).append("</td>")
                    .append("<td>").append(generateCitationLink(entry)).append("</td>")
                    .append("<td>").append(generateYearLink(entry)).append("</td>")
                    .append("</tr>");
            venues.add(entry.venue);
            years.add(entry.year);
            tags.addAll(generateTagList(entry));
        }
        input = input.replace("DATA_INSERT_HERE", HTML.toString());
        input = input.replace("INSERT_BIB_PATH", BibtexViewer.BIBTEX_DIR
                + "literature.bib");
        input = insertOptionsStr(input, "INSERT_VENUE_OPTIONS", venues);
        input = insertOptionsStr(input, "INSERT_TAG_OPTIONS", tags);
        input = insertOptionsInt(input, "INSERT_YEAR_OPTIONS", years);

        writeToFile(BibtexViewer.OUTPUT_DIR, "index.html", input);
    }

    private String insertOptionsStr(String input, String replace,
                                    Set<String> set) {
        StringBuilder HTML = new StringBuilder();
        for (String el : set) {
            HTML.append("<option value=\"").append(el).append("\">");
        }
        return input.replace(replace, HTML.toString());
    }

    private String insertOptionsInt(String input, String replace,
                                    Set<Integer> set) {
        StringBuilder HTML = new StringBuilder();
        for (int el : set) {
            HTML.append("<option value=\"").append(el).append("\">");
        }
        return input.replace(replace, HTML.toString());
    }

    private static String generateTitleLink(BibtexEntry entry) {
        return ExportHTML.getHTMLTitle(entry);
    }

    private String generateAuthorLinks(BibtexEntry entry) {
    	List<String> authors;
        if (entry.authorList.isEmpty()) {
        	authors = new ArrayList<>(1);
        	authors.add("unknown");
        } else {
        	authors = entry.authorList;
        }

        StringBuilder HTML = new StringBuilder();
        for (String author : authors) {
            HTML.append("<a href=\"\" onclick=\"setTag('searchAuthor','").append(author.trim())
                .append("');event.preventDefault();Filter();\">").append(author).append("</a>, ");
        }
        HTML.deleteCharAt(HTML.length() - 1);
        HTML.deleteCharAt(HTML.length() - 2);
        return HTML.toString();
    }

    private String generateVenueLink(BibtexEntry entry) {
        return "<a href=\"\" onclick=\"setTag('searchVenue','" + entry.venue.trim()
                + "');event.preventDefault();Filter();\">" + entry.venue
                + "</a>";
    }

    private String generateTagLinks(BibtexEntry entry) {
        final Typo3Entry entryAsT3 = ExportTypo3Bibtex.applyModifiers(
                new Typo3Entry(entry, bibTagsVariables)
        );

        StringBuilder html = new StringBuilder();
		if (entry.getCommentsPath().exists()) {
			html.append(" <a href=\"");
			html.append(entry.getRelativeCommentsPath());
			html.append("\">");
			html.append(entry.key);
			html.append("</a>, ");
		}
		else {
			html.append(entry.key).append(", ");
		}
        // DOI
		if (!entry.doi.isEmpty()) {
			html.append("<a href=\"https://dx.doi.org/");
			html.append(entry.doi);
			html.append("\">doi</a>, ");
		}
        // URL
        final boolean hasURL = !entry.url.isEmpty();
		if (hasURL) {
			html.append("<a href=\"");
			html.append(entry.url);
			html.append("\">url</a>, ");
		}
        // Typo3 URL for preprints
        final boolean hasT3URL = !entryAsT3.url.isBlank();
        if (hasT3URL && !(hasURL && entry.url.equals(entryAsT3.url))) {
            html.append("<a href=\"");
            html.append(entryAsT3.url);
            html.append("\">preprint</a>, ");
        }
        // other tags
        for (int i = 0; i < BibtexViewer.TAGS.size(); i++) {
        	String tag = BibtexViewer.TAGS.get(i);
        	List<String> tags = entry.tagList.get(tag);
        	if (tags != null) {
                String prefix = tag.replace("-tags", ":").replace("Tags", ":");
                for (int j = 0; j < tags.size(); j++) {
                    html.append("<a href=\"\" onclick=\"setTag('searchTag','");
                    html.append(prefix).append(tags.get(j).trim());
                    html.append("');event.preventDefault();Filter();\">");
                    html.append(prefix).append(tags.get(j).trim());
                    html.append("</a>, ");
                }
        	}
        }
        html.delete(html.lastIndexOf(","), html.length());
        return html.toString();
    }

    private List<String> generateTagList(BibtexEntry entry) {
        List<String> tags = new ArrayList<>();
        tags.add(entry.key);
        int i = 0;
        for (List<String> tagList : entry.tagList.values()) {
            String prefix = BibtexViewer.TAGS.get(i).replace("tags", "");
            for (String tag : tagList) {
                String combinedTag = (i!=0?prefix:"") + tag;
                tags.add(combinedTag);
            }
            i += 1;
        }
        return tags;
    }

    private String generateCitationLink(BibtexEntry entry) {
        return "<a href=\"https://scholar.google.de/scholar?q=" + entry.title
                + "\" target=\"_blank\">" + entry.getCitationsPerYear() + "</a>";
    }

    private String generateYearLink(BibtexEntry entry) {
        return "<a href=\"\" onclick=\"setTag('searchYear', '" + entry.year
                + "');event.preventDefault();Filter();\">" + entry.year
                + "</a>";
    }
}
