package de.mibtex.export;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

public class ExportCitations extends Export {

    public ExportCitations(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        StringBuilder CSV = new StringBuilder();
        for (BibtexEntry entry : entries.values()){
            CSV.append("\"" + entry.key + "\";").append("\"" + BibtexEntry.toURL(entry.title) + "\";")
                    .append(entry.citations + ";")
                    .append((System.currentTimeMillis()-(25*60*60))+";" + System.getProperty("line.separator"));
        }
        writeToFile(BibtexViewer.CITATION_DIR,"citations.csv",CSV.toString());    
    }
    
}
