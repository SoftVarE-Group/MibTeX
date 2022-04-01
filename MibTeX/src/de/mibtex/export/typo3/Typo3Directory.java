package de.mibtex.export.typo3;

import java.util.function.Predicate;

import static de.mibtex.export.typo3.Filters.*;

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
}
