/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import de.mibtex.BibtexEntry;

/**
 * A class that generates a .csv file with all BibTeX entries
 * 
 * @author Christopher Sontag
 */
public class ExportCSV extends Export {
    
    public ExportCSV(String file) throws Exception {
        super(file);
    }
    
    

    @Override
    public void writeDocument() {
        StringBuilder CSV = new StringBuilder();
        for (BibtexEntry entry : entries){
            CSV.append("\"" + entry.key + "\";").append("\"" + entry.author + "\";").append("\"" + entry.title + "\";")
                    .append("\"" + entry.venue + "\";").append(entry.year + ";").append(entry.citations + ";")
                    .append("\"" + entry.tags + "\"" + BibtexEntry.lineSeperator);
        }
        writeToFile("literature.csv",CSV.toString());
    }
    
}
