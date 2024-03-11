/* MibTeX - Minimalistic tool to manage your references with BibTeX
 *
 * Distributed under BSD 3-Clause License, available at Github
 *
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import de.mibtex.BibtexViewer;
import de.mibtex.export.typo3.Filters;
import de.mibtex.export.typo3.Typo3Directory;
import de.mibtex.export.typo3.Typo3Entry;
import de.mibtex.export.typo3.Util;
import org.jbibtex.Key;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static de.mibtex.export.typo3.Filters.*;
import static de.mibtex.export.typo3.Modifiers.*;
import static de.mibtex.export.typo3.Util.when;

/**
 * Exports the bibtex file to bibtex in a carefully adjusted format such that the BibTex-Importer of Typo3 (Website-Framework) can read it correctly.
 *
 * @author Paul Maximilian Bittner
 */
public class ExportTypo3Bibtex extends Export {
    /**
     * This key exists in Typo3 and we need it for proper display of
     * bachelor's and master's thesis.
     * When just using the type "@mastersthesis" the line "Master's Thesis" is displayed
     * on the website twice because reasons. >.<
     */
    public static final Key TYPE_THESIS = new Key("thesis");
    public final static String TYPO3_TAGS_ATTRIBUTE = "typo3Tags";
    private final static String MYABRV = "MYabrv.bib";
    private final static String MYSHORT = "MYshort.bib";
    // We do not consider MYfull because it is deprecated.

    private final static Typo3Directory PUBLICATIONS_DIR = new Typo3Directory(
            "typo3_Publikationen_SoftVarE.bib",
            "Publikationen SoftVarE",
            Typo3Directory.PublikationenSoftVarE.or(BELONGS_TO_VARIANTSYNC_PUBLICATIONS)
    );
    private final static Typo3Directory THESES_DIR = new Typo3Directory(
            "typo3_Abschlussarbeiten_SoftVarE.bib",
            "Abschlussarbeiten SoftVarE",
            Typo3Directory.AbschlussarbeitenSoftVarE.or(BELONGS_TO_VARIANTSYNC_THESES)
    );

    /**
     * Choose the variables file that you want to use to substitute names in the exported bibtex file.
     * Choose from
     * @see MYabrv
     * @see MYshort
     */
    static final String VariablesFile = MYABRV;

    /**
     * Select the filter you need to export only the publications you are interested in.
     * A Typo3Entry t gets selected if bibFilter.test(t) returns 'true'.
     * You may use or compose default filters and helper functions from de.mibtex.export.typo3.Filters.
     * For instance, if you want to select a subset of specific publications manually, use
     *     Filters.KeyIsOneOf("Key1", "Key2", ...)
     * to select all publications with these keys.
     * Compose filters with the respective methods of Predicate<T> (such as `and`, `or`).
     */
    private final Predicate<Typo3Entry> bibFilter =
            Filters.SHOULD_BE_PUT_ON_WEBSITE//.and(Filters.keyIsOneOf("DGT:EMSE21"));
//            Filters.THESIS_SUPERVISED_BY_SOFTVARE.or(Filters.WITH_PAUL_AT_ICG) // upload to "Abschlussarbeiten"
//            Filters.THESIS_AUTHORED_BY_SOFTVARE // upload to "Publikationen"
//            Filters.IS_SOFTVARE_WEBSITE_PAPER.and(Filters.WITH_THOMAS_BEFORE_ULM)
//            Filters.IS_SOFTVARE_WEBSITE_PAPER.and(Filters.WITH_THOMAS_BEFORE_ULM.negate())
//			  Filters.BELONGS_TO_SOFTVARE
//            Filters.BELONGS_TO_OBDDIMAL
//			  Filters.keyIsOneOf("TCA:SPLC21")
            ;

    /**
     * Select the modifiers you want to apply to each entry after filtering.
     * Each modifier is a function taking a Typo3Entry and returning the modified Typo3Entry.
     * Default implementations can be found in de.mibtex.export.typo3.Modifiers.
     * For instance, Modifiers.MarkIfThomasIsEditor checks if Thomas Thüm is an editor of the entry and if so,
     * adds a specific tag to the publication that we use for the website.
     * Some modifiers are dedicated to resolving duplicate entries (w.r.t. titles) because Typo3 considers entries with the same title to be the same.
     * If unsure, leave unchanged.
     */
    public static List<Function<Typo3Entry, Typo3Entry>> modifiers = Arrays.asList(
              TAG_IF_THOMAS_IS_EDITOR
            , TAG_IF_SOFTVARE
            , MARK_IF_TO_APPEAR
            , when(IS_MASTERSTHESIS, setEntryType(TYPE_THESIS)) // Need for proper display on website

            // Custom Links to Preprints
            , whenKeyIs("KKS+:SE23", softVarEURLFile("2023-SE-Kuiter-Tseitin"))
            , whenKeyIs("KKK+:SE23", softVarEURLFile("2023-SE-Kuiter-variED"))
            , whenKeyIs("AMK+:GPCE16", softVarEURLFile("2016-GPCE-Al-Hajjaji-Demo"))
            , whenKeyIs("TLK:SPLC16", softVarEURLFile("2016-SPLC-Thuem-Tutorial"))
            , whenKeyIs("RLB+:AOSDtool14", softVarEURLFile("2014-AOSD-Rebelo-Demo"))
            , whenKeyIs("Thuem15", softVarEURLFile("2015-PhD-Thuem"))
            , whenKeyIs("TSP+:ISRN12", softVarEURLFile("2012-ISRN-Thuem"))
            , whenKeyIs("SSS+16", softVarEURLFile("2016-WSRE-Schink"))
            , whenKeyIs("JMJ+19", softVarEURLFile("2019-SPP1593-Jung"))
            , whenKeyIs("THA+19", softVarEURLFile("2019-SPP1593-Thuem"))
            , whenKeyIs("TKS:ConfWS18", softVarEURLFile("2018-CONFWS-Thuem"))
            , whenKeyIs("DGT:EMSE21", softVarEURLFile("2021-EMSE-Duchien"))
            , whenKeyIs("TB12", CLEAR_URL)
            , whenKeyIs("MTS+17", CLEAR_URL)
            , when(Filters.PREPRINT_EXISTS_IN_PREPRINT_DIR,
                    ADD_PAPER_LINK_IF_SOFTVARE,
                    KEEP_URL_IF_PRESENT
            )
            , when(Filters.SLIDES_EXIST_IS_SLIDES_DIR,
                    ADD_PAPER_LINK_IF_SOFTVARE, // TODO
                    KEEP_URL_IF_PRESENT // TODO
            )

            // Other custom solutions
            , whenKeyIs("Young21", KEEP_URL_IF_PRESENT)

            // Resolving duplicates
            , whenKeyIs("RBP+:TR22subsumedbyRBP+:LMCS23", MARK_AS_TECHREPORT)
            , whenKeyIs("SHN+:SE24", MARK_AS_SE_GI_PAPER)
            , whenKeyIs("KKS+:SE23", MARK_AS_SE_GI_PAPER)
            , whenKeyIs("KKK+:SE23", MARK_AS_SE_GI_PAPER)
            , whenKeyIs("FSTR:SE23", MARK_AS_SE_GI_PAPER)
            , whenKeyIs("Young21", MARK_AS_PHDTHESIS)
            , whenKeyIs("KJN+:SE21", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("RSC+:SE21", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("TKK+:SPLC19", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("KTS+:SE19", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("KTP+:SE19", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("TKL:SPLC18", appendToTitle("(Second Edition)"))
            , whenKeyIs("KTM+:SE18", MARK_AS_EXTENDED_ABSTRACT)
            , whenKeyIs("KAT:TR16", MARK_AS_TECHREPORT)
            , whenKeyIs("RLB+:TR13subsumedbyRLB+:AOSD14", MARK_AS_TECHREPORT)
            , whenKeyIs("Bittner19", MARK_AS_PROJECTTHESIS)
            , whenKeyIs("Sprey19", MARK_AS_PROJECTTHESIS)
            , whenKeyIs("PK14", MARK_AS_TECHREPORT)
    );

    private final List<Typo3Directory> typo3Directories = List.of(
            PUBLICATIONS_DIR,
            THESES_DIR
    );

    public ExportTypo3Bibtex(String path, String file) throws Exception {
        super(path, file);
    }

    public static Typo3Entry applyModifiers(final Typo3Entry t) {
        return modifiers.stream().reduce(Function.identity(), Function::compose).apply(t);
    }

    @Override
    public void writeDocument() {
        System.err.println(
                "FIXME: SHN+:SE24 is currently marked as SE paper by a custom rule. " +
                "It might get renamed in BibTags in the future. " +
                "In case this happens, the rule might have be removed."
        );
        // Parse the variables defined in MYabrv.bib
        final Map<String, String> variables = readVariablesFromBibtexFile(new File(BibtexViewer.BIBTEX_DIR, VariablesFile));

        // Transform all Bibtex-Entries to Typo3Entries, filter them and apply all modifiers.
        final List<Typo3Entry> typo3Entries = entries.values().stream()
                .map(b -> new Typo3Entry(b, variables))
                .filter(bibFilter)
                .map(ExportTypo3Bibtex::applyModifiers)
                .collect(Collectors.toList());

        final StringBuilder uploadInstructions = new StringBuilder(System.lineSeparator());
        uploadInstructions.append("To correctly import your entries to Typo3, you should upload:");
        uploadInstructions.append(System.lineSeparator());
        for (final Typo3Directory t3dir : typo3Directories) {
            final boolean atLeastOneEntryExported = exportEntriesOfDirectory(typo3Entries, t3dir);
            if (atLeastOneEntryExported) {
                uploadInstructions
                        .append("  - \"")
                        .append(t3dir.getAbsolutePathToFile())
                        .append("\" to directory \"")
                        .append(t3dir.directoryNameInTypo3())
                        .append("\" in Typo3.")
                        .append(System.lineSeparator());
            }
        }

        System.out.println(uploadInstructions);
    }

    public static boolean exportEntriesOfDirectory(List<Typo3Entry> typo3Entries, final Typo3Directory t3dir) {
        typo3Entries = Util.filter(typo3Entries, t3dir.belongsToDirectory());

        final File file = t3dir.getAbsolutePathToFile();
        boolean exportedAFile = false;

        // Generate the typo3-conforming Bibtex source code.
        final String typo3 = typo3Entries.stream()
                .map(Typo3Entry::toString)
                .reduce("", (a, b) -> a + "\n\n" + b);

        System.out.println("=== EXPORTING " + t3dir.generatedFileName() + " ===");
        if (!typo3.isBlank()) {
            // Check if we have some duplicates left that were not resolved.
            final int duplicates = Util.getDuplicates(typo3Entries, (a, b) -> {
                if (a.title.isBlank() && b.title.isBlank()) {
                    System.out.println("  > Found entries without title: " + a.key + ", " + b.key);
                    return;
                }
                System.out.println("  > Found unresolved duplicate title: " + a.title + " (" + a.key + ", " + b.key + ")\n");
            });
            final long numUniqueEntries = typo3Entries.size() - duplicates;

            System.out.println("  Exported " + typo3Entries.size() + " entries.");
            System.out.println("  Thereof " + numUniqueEntries + " entries are unique (by title).\n");
            System.out.flush();

            if (duplicates > 0) {
                System.err.flush();
                System.err.println("  There were unresolved duplicates that can cause problems when imported with TYPO3!");
            }

            writeToFileInUTF8(file, typo3);
            exportedAFile = true;
        } else {
            System.out.println("  No entries given, nothing to do.");
        }

        System.out.println("=== DONE ===");
        return exportedAFile;
    }

    static Map<String, String> readVariablesFromBibtexFile(File pathToBibtex) {
        final Map<String, String> vars = new HashMap<>();

        final BufferedReader file = readFromFile(pathToBibtex, StandardCharsets.UTF_8);
        if (file == null) {
            throw new RuntimeException("Could read file " + pathToBibtex + " for some reason.");
        }
        try {
            while (file.ready()) {
                String line = file.readLine().trim();
                if (line.startsWith("@String")) {
                    line = line.substring(line.indexOf("{") + 1);
                    String[] words = line.substring(0, line.lastIndexOf("}")).split("=");
                    if (words.length == 2) {
                        String var = words[0].trim().toUpperCase();
                        // Remove apostrophes ("") at the beginning and end.
                        String val = words[1].trim().substring(1, words[1].length() - 2);
                        vars.put(var, val);
                        //System.out.println("[ExportTypo3Bibtex.readVariablesFromBibtexFile] " + var + " -> " + val);
                    }
                }
            }
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return vars;
    }
}
