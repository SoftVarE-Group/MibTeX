/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import java.util.function.Function;

/**
 * This is a collection of default modifiers to use for the ExportTypo3Bibtex.
 * Each filter is a java.util.Function that takes a Typo3Entry and returns its modified version.
 * Most of the modifiers here are used for handling duplicate Typo3Entries (with respect to their title).
 * 
 * @author Paul Maximilian Bittner
 */
public class Modifiers {
	public static Function<Typo3Entry, Typo3Entry> MarkIfThomasIsEditor = Util.If(t -> t.editors.contains(Filters.ThomasThuem), AddTag("EditorialThomasThuem"));
	public static Function<Typo3Entry, Typo3Entry> MarkIfVenueIsSE = Util.If(t -> "SE".equals(t.source.venue), AppendToTitle("(SE)"));
	public static Function<Typo3Entry, Typo3Entry> MarkIfToAppear = Util.If(t -> t.note.toLowerCase().contains("to appear"), AppendToBookTitle("(To Appear)"));
	public static Function<Typo3Entry, Typo3Entry> MarkIfTechreport = Util.IfForced(Filters.Is_techreport, AppendToTitle("(Technical Report)"), "Given entry is not a technical report! (Perhaps an illegal modifier?)");
	public static Function<Typo3Entry, Typo3Entry> MarkAsExtendedAbstract = AppendToTitle("(Extended Abstract)");
	
	public static Function<Typo3Entry, Typo3Entry> AppendToTitle(String suffix) {
		return t -> {
			t.title += " " + suffix;
			return t;
		};
	}
	
	public static Function<Typo3Entry, Typo3Entry> AppendToBookTitle(String suffix) {
		return t -> {
			t.booktitle += " " + suffix;
			return t;
		};
	} 
	
	public static Function<Typo3Entry, Typo3Entry> AddTag(String tag) {
		return t -> {
			t.tags.add(tag);
			return t;
		};
	}
	
	public static Function<Typo3Entry, Typo3Entry> IfKeyIs(String key, Function<Typo3Entry, Typo3Entry> f) {
		return Util.If(t -> t.key.equals(key), f);
	}
}
