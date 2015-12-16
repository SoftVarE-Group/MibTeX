/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.util.List;

import de.mibtex.BibtexEntry;


/**
 * A class that generates a .json file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportJSON extends Export{

    public ExportJSON(String file) throws Exception {
        super(file);
    }

    @Override
    public void writeDocument() {
        StringBuilder JSON = new StringBuilder();
        JSON.append("{"+BibtexEntry.lineSeperator);
        for (BibtexEntry entry : entries) {
            JSON.append("{")
            .append(getJSONAttribute("key",entry.key)+",")
            .append(getJSONAttribute("authors",entry.authorList)+",")
            .append(getJSONAttribute("title", entry.title)+",")
            .append(getJSONAttribute("venues", entry.venue)+",")
            .append(getJSONAttribute("year", entry.year)+",")
            .append(getJSONAttribute("citations", entry.citations)+",")
            .append(getJSONAttribute("tags", entry.tagList))
            .append("}"+BibtexEntry.lineSeperator);
        }
        JSON.append("}");
        writeToFile("literature.json",JSON.toString());
    }

    private String getJSONAttribute(String key, int str) {
        return "\""+key+"\":"+str;
    }

    private String getJSONAttribute(String key, String str) {
        return "\""+key+"\":\""+BibtexEntry.toURL(str)+"\"";
    }
    
    private String getJSONAttribute(String key, List<String> list) {
        String str = "[";
        for (int i = 0; i < list.size(); i++) {
            str += "{"+getJSONAttribute("name",list.get(i))+"}";
            if (i < (list.size()-1)) {
                str += ",";
            }
        }
        str += "]";
        return "\""+key+"\":"+str;
    }
}
