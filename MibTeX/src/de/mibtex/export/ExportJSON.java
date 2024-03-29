/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.util.Collection;
import java.util.List;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;


/**
 * A class that generates a .json file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportJSON extends Export{

    public ExportJSON(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        StringBuilder JSON = new StringBuilder();
        JSON.append("["+System.getProperty("line.separator"));
        for (BibtexEntry entry : entries.values()) {
            JSON.append("{")
            .append(getJSONAttribute("key",entry.key)+",")
            .append(getJSONAttribute("authors",entry.authorList)+",")
            .append(getJSONAttribute("title", entry.title)+",")
            .append(getJSONAttribute("venues", entry.venue)+",")
            .append(getJSONAttribute("year", entry.year)+",")
            .append(getJSONAttribute("citations", entry.getCitations())+",")
            .append(getJSONAttributeFromList("tags", entry.tagList.values()))
            .append("},"+System.getProperty("line.separator"));
        }
        JSON.append("]");
        writeToFile(BibtexViewer.OUTPUT_DIR,"literature.json",JSON.toString());
    }

    private String getJSONAttribute(String key, int str) {
        return "\""+key+"\":[{\"name\":\""+str+"\"}]";
    }

    private String getJSONAttribute(String key, String str) {
        return "\""+key+"\":[{\"name\":\""+BibtexEntry.toURL(str)+"\"}]";
    }
    
    private String getJSONAttribute(String key, List<String> list) {
        String str = "[";
        for (int i = 0; i < list.size(); i++) {
            str += "{\"name\":\""+BibtexEntry.toURL(list.get(i))+"\"}";
            if (i < (list.size()-1)) {
                str += ",";
            }
        }
        str += "]";
        return "\""+key+"\":"+str;
    }

    private String getJSONAttributeFromList(String key, Collection<List<String>> collection) {
        String str = "[";
        for (List l : collection) {
            str += getJSONAttribute(key, l);
        }
        str += "]";
        return "\""+key+"\":"+str;
    }
}
