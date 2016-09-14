/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.File;
import java.util.List;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;


/**
 * A class that generates a .json file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportNewHTML extends Export{

    public ExportNewHTML(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        StringBuilder JSON = new StringBuilder();
        JSON.append("[");
        for (BibtexEntry entry : entries.values()) {
            JSON.append("{")
            .append(getJSONAttribute("key",entry.key)+",")
            .append(getJSONAttribute("authors",entry.authorList)+",")
            .append(getJSONAttribute("title", entry.title)+",")
            .append(getJSONAttribute("venues", entry.venue)+",")
            .append(getJSONAttribute("year", entry.year)+",")
            .append(getJSONAttribute("citations", entry.getCitations())+",")
            .append(getJSONAttribute("tags", entry.tagList)+",")
					.append(getJSONAttribute("pdf", entry.getRelativePDFPath()))
            .append("},");
        }
        JSON.append("]");
        String input = readFromFile("resources/",new File("index_in.html"));
        input = input.replace("JSON_DATA_INSERT_HERE", JSON.toString());
        writeToFile(BibtexViewer.OUTPUT_DIR,"index.html",input);
    }

    private String getJSONAttribute(String key, int str) {
        return "\""+key+"\":[{\"name\":\""+str+"\"}]";
    }

    private String getJSONAttribute(String key, String str) {
        if (!key.equals("pdf")){
            str = BibtexEntry.toURL(str);
        }
        return "\""+key+"\":[{\"name\":\""+str+"\"}]";
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
}
