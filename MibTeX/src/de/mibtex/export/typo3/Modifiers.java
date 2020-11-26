package de.mibtex.export.typo3;

import java.util.function.Function;

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
