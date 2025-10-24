package de.mibtex.export.typo3;

import de.mibtex.BibtexViewer;

import java.io.File;
import java.util.function.Predicate;

import static de.mibtex.export.typo3.Filters.*;

/**
 * This record reflects a directory in the Typo3 backend.
 * For different purposes, we created different directories in Typo3 at the Ulm University.
 * These directories held all the imported BibTeX entries we exported via MibTeX.
 * We used different directories for a better overview and to be able to re-generate particular BibTeX entries
 * on the website if necessary (by deleting the directory in the backend and re-generating it with MibTeX and the use of this class).
 * This record denotes such a directory by giving it a name and determining which entries should be
 * put into that directory.
 * For examples, see {@link de.mibtex.export.ExportTypo3Bibtex}.
 * @author Paul Bittner
 */
public record Typo3Directory(
        String generatedFileName,
        String directoryNameInTypo3,
        Predicate<Typo3Entry> belongsToDirectory
) {
    public final static Predicate<Typo3Entry> PublikationenSoftVarE = IS_SOFTVARE_PUBLICATION;
    public final static Predicate<Typo3Entry> AbschlussarbeitenSoftVarE = THESIS_BY_SOFTVARE;

    @Deprecated
    public final static Predicate<Typo3Entry> Alte_Publikationen_Thomas_Thuem =
            WITH_THOMAS_BEFORE_ULM;
    @Deprecated
    public final static Predicate<Typo3Entry> Alte_Publikationen_Paul_Bittner =
            WITH_PAUL_BEFORE_OR_NOT_AT_ULM.and(Alte_Publikationen_Thomas_Thuem.negate());
    @Deprecated
    public final static Predicate<Typo3Entry> Publikationen =
            authorOrEditorIsOneOf(THOMAS_THUEM, CHICO_SUNDERMANN, TOBIAS_HESS, PAUL_MAXIMILIAN_BITTNER)
                    .and(Alte_Publikationen_Thomas_Thuem.negate())
                    .and(Alte_Publikationen_Paul_Bittner.negate());
    @Deprecated
    public final static Predicate<Typo3Entry> Abschlussarbeiten =
            THESIS_BY_SOFTVARE;
    
    public File getAbsolutePathToFile() {
        return new File(BibtexViewer.OUTPUT_DIR, generatedFileName());
    }
}
