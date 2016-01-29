package de.mibtex.export;

import de.mibtex.BibtexEntry;

public class ExportCitations extends Export {
    
    private String outputPath;

    public ExportCitations(String path, String file, String outputPath) throws Exception {
        super(path, file);
        this.outputPath = outputPath;
    }

    @Override
    public void writeDocument() {
        StringBuilder CSV = new StringBuilder();
        for (BibtexEntry entry : entries.values()){
            CSV.append("\"" + entry.key + "\";").append("\"" + BibtexEntry.toURL(entry.title) + "\";")
                    .append(entry.citations + ";")
                    .append((System.currentTimeMillis()-(25*60*60))+";" + System.getProperty("line.separator"));
        }
        writeToFile(outputPath,"citations.csv",CSV.toString());    
    }
    
}
