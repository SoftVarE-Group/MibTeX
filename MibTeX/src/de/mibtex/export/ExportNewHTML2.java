/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;


/**
 * A class that generates a .json file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportNewHTML2 extends Export{

    public ExportNewHTML2(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        String input = readFromFile("resources/",new File("index2_in.html"));
        StringBuilder HTML = new StringBuilder();
        Set<String> venues = new HashSet<>();
        Set<String> tags = new HashSet<>();
        Set<Integer> years = new HashSet<>();
        for (BibtexEntry entry : entries.values()) {
            HTML.append("<tr id=\"")
            .append(entry.key+"\">")
            .append("<td>"+generateTitleLink(entry)+"</td>")
            .append("<td>"+generateAuthorLinks(entry)+"</td>")
            .append("<td>"+generateVenueLink(entry)+"</td>")
            .append("<td>"+generateTagLinks(entry)+"</td>")
            .append("<td>"+generateCitationLink(entry)+"</td>")
            .append("<td>"+generateYearLink(entry)+"</td>")
            .append("</tr>");
            venues.add(entry.venue);
            years.add(entry.year);
            tags.addAll(entry.tagList);
        }
        input = input.replace("DATA_INSERT_HERE", HTML.toString());
        input = input.replace("INSERT_BIB_PATH", BibtexViewer.BIBTEX_DIR+"literature.bib");
        input = insertOptionsStr(input,"INSERT_VENUE_OPTIONS",venues);
        input = insertOptionsStr(input, "INSERT_TAG_OPTIONS",tags);
        input = insertOptionsInt(input,"INSERT_YEAR_OPTIONS",years);
        
        writeToFile(BibtexViewer.HTML_DIR,"index2.html",input);
    }

    private String insertOptionsStr(String input, String replace, Set<String> set) {
        StringBuilder HTML = new StringBuilder();
        for (String el : set) {
            HTML.append("<option value=\""+el+"\">");
        }
        return input.replace(replace, HTML.toString());
    }
    
    private String insertOptionsInt(String input, String replace, Set<Integer> set) {
        StringBuilder HTML = new StringBuilder();
        for (int el : set) {
            HTML.append("<option value=\""+el+"\">");
        }
        return input.replace(replace, HTML.toString());
    }
    
    private String generateTitleLink(BibtexEntry entry) {
        return "<a href=\""+entry.getPDFPath()+"\">"+entry.title+"</a>";
    }
    
    private String generateAuthorLinks(BibtexEntry entry) {
        StringBuilder HTML = new StringBuilder();
        for (String author : entry.authorList) {
            HTML.append("<a href=\"\" onclick=\"SetAuthor('"+author.trim()+"');event.preventDefault();Filter();\">"+author+"</a>, ");
        }
        HTML.deleteCharAt(HTML.length()-1);
        HTML.deleteCharAt(HTML.length()-2);
        return HTML.toString();
    }
    
    private String generateVenueLink(BibtexEntry entry) {
        return "<a href=\"\" onclick=\"SetVenue('"+entry.venue.trim()+"');event.preventDefault();Filter();\">"+entry.venue+"</a>";
    }
    
    private String generateTagLinks(BibtexEntry entry) {
        StringBuilder HTML = new StringBuilder();
        for (String tag : entry.tagList) {
            HTML.append("<a href=\"\" onclick=\"SetTag('"+tag.trim()+"');event.preventDefault();Filter();\">"+tag+"</a>, ");
        }
        HTML.deleteCharAt(HTML.length()-1);
        return HTML.toString();
    }
    
    private String generateCitationLink(BibtexEntry entry) {
        if (entry.citations >=0)
            return "<a href=\"https://scholar.google.de/scholar?q="+entry.title+"\" target=\"_blank\">"+entry.citations+"</a>";
        return "<a href=\"https://scholar.google.de/scholar?q="+entry.title+"\" target=\"_blank\">0</a>";
    }

    private String generateYearLink(BibtexEntry entry) {
        return "<a href=\"\" onclick=\"SetYear('"+entry.year+"');event.preventDefault();Filter();\">"+entry.year+"</a>";
    }
}
