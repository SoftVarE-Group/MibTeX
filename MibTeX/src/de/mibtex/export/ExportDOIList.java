package de.mibtex.export;

import static de.mibtex.export.typo3.Filters.IS_ISF_PUBLICATION;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Typo3Entry;

public class ExportDOIList extends ExportTypo3Bibtex {

    public ExportDOIList(String path, String file) throws Exception {
        super(path, file);
    }

    @Override
    public void writeDocument() {
        System.out.println("=== Parsing BibTeX entries to Typo3 entries ===");
        System.out.println("  BibTags files have been read from from " + BibtexViewer.BIBTEX_DIR);

        final File variablesFileLocation = new File(BibtexViewer.BIBTEX_DIR, VariablesFile);
        System.out.println("  Parse variables from " + variablesFileLocation);
        // Parse the variables defined in MYabrv.bib
        final Map<String, String> variables = readVariablesFromBibtexFile(variablesFileLocation);

        System.out.println("  Converting entries...");
        // Transform all Bibtex-Entries to Typo3Entries, filter them and apply all modifiers.
        final List<String> dois = entries.values().stream()
                .map(b -> new Typo3Entry(b, variables))
                .filter(IS_ISF_PUBLICATION)
                // .map(ExportTypo3Bibtex::applyModifiers)
                .map(x -> getDoiString(x))
                .filter(x -> !x.isEmpty())
                .map(x -> "https://doi.org/" + x)
                .collect(Collectors.toList());
        
        System.out.println(String.join("\n", dois));
    }

    private String getDoiString(Typo3Entry entry) {
        return entry.doi;
    }
    
}
