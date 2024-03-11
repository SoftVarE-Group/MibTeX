/* MibTeX - Minimalistic tool to manage your references with BibTeX
 *
 * Distributed under BSD 3-Clause License, available at Github
 *
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * This is a collection of default filters to use for the ExportTypo3Bibtex.
 * Each filter is a java.util.Predicate that can be combined with propositional operators (and, or, negate, ...)
 *
 * @author Paul Maximilian Bittner
 */
public class Filters {
    public final static String THOMAS_THUEM = "Thomas Thüm";
    public final static String CHICO_SUNDERMANN = "Chico Sundermann";
    public final static String TOBIAS_HESS = "Tobias Heß";
    public final static String PAUL_MAXIMILIAN_BITTNER = "Paul Maximilian Bittner";
    public final static String SEBASTIAN_KRIETER = "Sebastian Krieter";
    public final static String SABRINA_BOEHM = "Sabrina Böhm";
    public final static String RAHEL_SUNDERMANN = "Rahel Sundermann";
    public final static String ALEXANDER_SCHULTHEISS = "Alexander Schultheiß";

    public final static Predicate<Typo3Entry> ANY = b -> true;

    public final static Predicate<Typo3Entry> IS_MISC = b -> b.type.equals("misc");
    public final static Predicate<Typo3Entry> IS_PROCEEDINGS = b -> b.type.equals("proceedings");
    public final static Predicate<Typo3Entry> IS_SOFTWARE = b -> b.type.equals("software");
    public final static Predicate<Typo3Entry> IS_TECHREPORT = b -> b.type.equals("techreport");
    public final static Predicate<BibtexEntry> IS_TECHREPORT_BIB = b -> b.type.equals("techreport");
    public final static Predicate<Typo3Entry> IS_BACHELORSTHESIS =
            b -> b.type.equals("mastersthesis") && b.typeAttrib.toLowerCase().startsWith("bachelor");
    public final static Predicate<Typo3Entry> IS_MASTERSTHESIS = b -> b.type.equals("mastersthesis");
    public final static Predicate<Typo3Entry> IS_PHDTHESIS = b -> b.type.equals("phdthesis");
    public final static Predicate<Typo3Entry> IS_THESIS = b -> b.type.equals("thesis");
    public final static Predicate<Typo3Entry> IS_ANY_KIND_OF_THESIS = IS_MASTERSTHESIS.or(IS_PHDTHESIS).or(IS_THESIS);

    public final static Predicate<Typo3Entry> WITH_THOMAS = authorOrEditorIsOneOf(THOMAS_THUEM).and(IS_MISC.negate());
    public final static Predicate<Typo3Entry> WITH_THOMAS_BEFORE_ULM = WITH_THOMAS.and(b -> b.year < 2020);
    public final static Predicate<Typo3Entry> WITH_THOMAS_AT_ULM = WITH_THOMAS.and(b -> b.year >= 2020);

    public final static Predicate<Typo3Entry> WITH_CHICO = authorIsOneOf(CHICO_SUNDERMANN);

    public final static Predicate<Typo3Entry> WITH_PAUL = Filters.authorIsOneOf(PAUL_MAXIMILIAN_BITTNER);
    public final static Predicate<Typo3Entry> WITH_PAUL_AT_ICG = WITH_PAUL.and(t -> t.source.getAttribute("pb-tags").contains("ICG"));
    public final static Predicate<Typo3Entry> WITH_PAUL_AT_ULM =
            WITH_PAUL
                    .and(WITH_PAUL_AT_ICG.negate())
                    .and(IS_MASTERSTHESIS.negate())
                    .and(t -> t.year >= 2020);
    public final static Predicate<Typo3Entry> WITH_PAUL_BEFORE_OR_NOT_AT_ULM = WITH_PAUL.and(WITH_PAUL_AT_ULM.negate());

    public final static Predicate<Typo3Entry> AUTHORED_BY_SOFTVARE =
            authorIsOneOf(
                    THOMAS_THUEM
                    , CHICO_SUNDERMANN
                    , TOBIAS_HESS
                    , PAUL_MAXIMILIAN_BITTNER
                    , SEBASTIAN_KRIETER
                    , SABRINA_BOEHM
                    , RAHEL_SUNDERMANN
                    , ALEXANDER_SCHULTHEISS
            );
    public final static Predicate<Typo3Entry> EDITED_BY_SOFTVARE =
            editorIsOneOf(
                    THOMAS_THUEM
                    , CHICO_SUNDERMANN
                    , TOBIAS_HESS
                    , PAUL_MAXIMILIAN_BITTNER
                    , SEBASTIAN_KRIETER
                    , SABRINA_BOEHM
                    , RAHEL_SUNDERMANN
                    , ALEXANDER_SCHULTHEISS
            );

    public final static Predicate<Typo3Entry> THESIS_BY_SOFTVARE =
            // Supervised by one of us
            hasAtLeastOneTagOf("SupervisorTT", "SupervisorTH", "SupervisorPB", "SupervisorCS", "SupervisorSK", "SupervisorSB", "SupervisorAS", "SupervisorRS")
                    // or written by one of us.
                    .or(
                            IS_ANY_KIND_OF_THESIS.and(AUTHORED_BY_SOFTVARE)
                    );

    public final static Predicate<Typo3Entry> IS_SOFTVARE_PUBLICATION =
            AUTHORED_BY_SOFTVARE.or(EDITED_BY_SOFTVARE).and(THESIS_BY_SOFTVARE.negate());

    public final static Predicate<Typo3Entry> THESIS_AUTHORED_BY_SOFTVARE =
            IS_MASTERSTHESIS.and(AUTHORED_BY_SOFTVARE);
    public final static Predicate<Typo3Entry> BELONGS_TO_VARIANTSYNC_PUBLICATIONS = hasAtLeastOneTagOf(
            "VariantSyncPub", "VariantSyncPre");
    public final static Predicate<Typo3Entry> BELONGS_TO_VARIANTSYNC_THESES = hasAtLeastOneTagOf(
            "VariantSyncMT");
    public final static Predicate<Typo3Entry> BELONGS_TO_VARIANTSYNC = BELONGS_TO_VARIANTSYNC_PUBLICATIONS.or(BELONGS_TO_VARIANTSYNC_THESES);
    public final static Predicate<Typo3Entry> BELONGS_TO_OBDDIMAL = hasAtLeastOneTagOf(
            "OBDDimal", "OBDDimalTheses");
    public final static Predicate<Typo3Entry> BELONGS_TO_UVL = hasAtLeastOneTagOf(
            "UVL");
    public final static Predicate<Typo3Entry> BELONGS_TO_FMCOUNTING = hasAtLeastOneTagOf(
            "FMCounting");

    public final static Predicate<Typo3Entry> SHOULD_BE_PUT_ON_WEBSITE = IS_SOFTVARE_PUBLICATION.or(THESIS_BY_SOFTVARE);

    public final static Predicate<Typo3Entry> OPARU =
            IS_SOFTVARE_PUBLICATION
                    .and(AUTHORED_BY_SOFTVARE) // remove editorials
                    .and(IS_MISC.negate()) // remove non papers
                    .and(IS_SOFTWARE.negate()); // remove tools
                    // and is from uni ulm

    /**
     * The predicate returns true if the preprint for the given entry exists in our SoftVarE/Papers repository.
     * This predicate expects a local clone of the repository to be located at {@link BibtexViewer#PDF_DIR_REL}.
     */
    public final static Predicate<Typo3Entry> PREPRINT_EXISTS_IN_PREPRINT_DIR = t ->
        Files.exists(Path.of(t.getPaperUrlInRepo(BibtexViewer.PREPRINTS_DIR)));
    
    /**
     * The predicate returns true if the slides for the given entry exists in our SoftVarE/Slides repository.
     * This predicate expects a local clone of the repository to be located at {@link BibtexViewer#PDF_DIR_REL}.
     */
    public final static Predicate<Typo3Entry> SLIDES_EXIST_IS_SLIDES_DIR = t ->
            Files.exists(Path.of(t.getSlidesUrlInRepo(BibtexViewer.SLIDES_DIR)));

    

    public static Predicate<Typo3Entry> hasAtLeastOneTagOf(final String... tags) {
        return b -> {
            if (b.tags == null) return false;
            return b.tags.stream().anyMatch(Util.isOneOf(tags));
        };
    }

    private Filters() {}

    /**
     * The predicate returns true iff the entry's key matches one of the given keys.
     */
    public static Predicate<Typo3Entry> keyIsOneOf(String... keys) {
        return b -> Arrays.asList(keys).contains(b.key);
    }

    /**
     * @return A predicate that returns true iff the entry's author list contains at least one of the given authors or if the editor list does so.
     * An author string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
     */
    public static Predicate<Typo3Entry> authorOrEditorIsOneOf(String... authors) {
        return authorIsOneOf(authors).or(editorIsOneOf(authors));
    }


    /**
     * @return A predicate that returns true iff the entry's author list contains at least one of the given authors.
     * An author string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
     */
    public static Predicate<Typo3Entry> authorIsOneOf(String... authors) {
        return b -> Util.anyMatch(b.authors::contains, authors);
    }


    /**
     * @return A predicate that returns true iff the entry's editor list contains at least one of the given authors.
     * An editor string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
     */
    public static Predicate<Typo3Entry> editorIsOneOf(String... editors) {
        return b -> Util.anyMatch(b.editors::contains, editors);
    }
}
