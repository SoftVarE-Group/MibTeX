/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

/**
 * A class that generates a .csv file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportCSV extends Export {
    
    public ExportCSV(String path, String file) throws Exception {
        super(path, file);
    }
    
    

    @Override
    public void writeDocument() {
        StringBuilder CSV = new StringBuilder();
        for (BibtexEntry entry : entries.values()){
            CSV.append("\"" + entry.key + "\";").append("\"" + entry.author + "\";").append("\"" + entry.title + "\";")
                    .append("\"" + entry.venue + "\";").append(entry.year + ";").append(entry.getCitations() + ";")
                    .append("\"" + entry.tags + "\"" + System.getProperty("line.separator"));
        }
        writeToFile(BibtexViewer.OUTPUT_DIR,"literature.csv",CSV.toString());
    }
    
}
