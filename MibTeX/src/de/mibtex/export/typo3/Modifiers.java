/* MibTeX - Minimalistic tool to manage your references with BibTeX
 *
 * Distributed under BSD 3-Clause License, available at Github
 *
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import de.mibtex.BibtexEntry;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This is a collection of default modifiers to use for the ExportTypo3Bibtex.
 * Each filter is a java.util.Function that takes a Typo3Entry and returns its modified version.
 * Most of the modifiers here are used for handling duplicate Typo3Entries (with respect to their title).
 *
 * @author Paul Maximilian Bittner
 */
public class Modifiers {
    /** add tags for our website **/
    public static final Function<Typo3Entry, Typo3Entry> TAG_THOMAS_AS_EDITOR =
            addTag("EditorialThomasThuem");
    public static final Function<Typo3Entry, Typo3Entry> TAG_IF_THOMAS_IS_EDITOR =
            Util.when(t -> t.editors.contains(Filters.THOMAS_THUEM), TAG_THOMAS_AS_EDITOR);
    public static final Function<Typo3Entry, Typo3Entry> TAG_IF_SOFTVARE =
            Util.when(Filters.IS_SOFTVARE_PUBLICATION, addTag("SoftVarE"));

    /** resolve special types of entries **/

    public static final Function<Typo3Entry, Typo3Entry> MARK_IF_VENUE_IS_SE =
            Util.when(t -> "SE".equals(t.source.venue), appendToTitle("(SE)"));
    public static final Function<Typo3Entry, Typo3Entry> MARK_IF_TO_APPEAR =
            Util.when(t -> t.note.toLowerCase().contains("to appear"), appendToVenue("(To Appear)"));
    public static final Function<Typo3Entry, Typo3Entry> MARK_AS_TECHREPORT =
            Util.whenForced(Filters.IS_TECHREPORT, appendToTitle("(Technical Report)"), "Given entry is not a technical report! (Perhaps an illegal modifier?)");
    public static final Function<Typo3Entry, Typo3Entry> MARK_AS_EXTENDED_ABSTRACT =
            appendToTitle("(Extended Abstract)");
    public static final Function<Typo3Entry, Typo3Entry> MARK_AS_SE_GI_PAPER =
            appendToTitle("- Summary");
    public static final Function<Typo3Entry, Typo3Entry> MARK_AS_PHDTHESIS =
            appendToTitle("(PhD Thesis)");
    public static final Function<Typo3Entry, Typo3Entry> MARK_AS_PROJECTTHESIS =
            appendToTitle("(Project Thesis)");

    public static Function<Typo3Entry, Typo3Entry> SET_SOFTVARE_URL =
            sideffect(t -> t.url = t.getPaperUrlInSoftVarERepo());

    /** misc **/
    public static final Function<Typo3Entry, Typo3Entry> ADD_PAPER_LINK_IF_SOFTVARE =
            Util.when(Filters.IS_SOFTVARE_PUBLICATION, SET_SOFTVARE_URL);
    public static final Function<Typo3Entry, Typo3Entry> KEEP_URL_IF_PRESENT =
            sideffect(t -> {
                final String url = t.source.getAttribute("url");
                if (BibtexEntry.isDefined(url)) {
                    t.url = url;
                }
            });

    public static final Function<Typo3Entry, Typo3Entry> SWITCH_AUTHORS_TO_EDITORS =
            sideffect(t -> {
                t.editors = t.authors;
                t.authors = new ArrayList<>();
            });

    public static final Function<Typo3Entry, Typo3Entry> CLEAR_URL = sideffect(t -> t.url = "");

    public static Function<Typo3Entry, Typo3Entry> appendToTitle(String suffix) {
        return sideffect(t -> t.title += " " + suffix);
    }

    public static Function<Typo3Entry, Typo3Entry> appendToVenue(String suffix) {
        return sideffect(t -> {
            if (t.isJournalPaper()) {
                t.journal += " " + suffix;
            } else {
                t.booktitle += " " + suffix;
            }
        });
    }

    public static Function<Typo3Entry, Typo3Entry> addTag(String tag) {
        return sideffect(t -> t.tags.add(tag));
    }

    public static Function<Typo3Entry, Typo3Entry> softVarEURLFile(final String pdfName) {
        return sideffect(t -> t.url = Typo3Entry.getPaperUrlInSoftVarERepo(t.year, pdfName));
    }

    public static Function<Typo3Entry, Typo3Entry> setURL(final String url) {
        return sideffect(t -> t.url = url);
    }

    public static Function<Typo3Entry, Typo3Entry> whenKeyIs(String key, Function<Typo3Entry, Typo3Entry> f) {
        return Util.when(t -> t.key.equals(key), f);
    }

    public static Function<Typo3Entry, Typo3Entry> sideffect(final Consumer<Typo3Entry> sideffect) {
        return t -> {
            sideffect.accept(t);
            return t;
        };
    }

    public static Function<Typo3Entry, Typo3Entry> setEntryType(final org.jbibtex.Key type) {
        return t -> {
            t.type = type.getValue();
            return t;
        };
    }
}
