/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import java.util.Arrays;
import java.util.function.Predicate;

import de.mibtex.BibtexEntry;

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
	
	public final static Predicate<Typo3Entry> ANY = b -> true;
	
	public final static Predicate<Typo3Entry> IS_MISC = b -> b.type.equals("misc");
	public final static Predicate<Typo3Entry> IS_PROCEEDINGS = b -> b.type.equals("proceedings");
	public final static Predicate<Typo3Entry> IS_TECHREPORT = b -> b.type.equals("techreport");
	public final static Predicate<BibtexEntry> IS_TECHREPORT_BIB = b -> b.type.equals("techreport");
	public final static Predicate<Typo3Entry> IS_BACHELORSTHESIS =
			b -> b.type.equals("mastersthesis") && b.typeAttrib.toLowerCase().startsWith("bachelor");
	public final static Predicate<Typo3Entry> IS_MASTERSTHESIS = b -> b.type.equals("mastersthesis");
	public final static Predicate<Typo3Entry> IS_PHDTHESIS = b -> b.type.equals("phdthesis");

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

	public final static Predicate<Typo3Entry> BELONGS_TO_SOFTVARE = Filters
			.authorOrEditorIsOneOf(THOMAS_THUEM, CHICO_SUNDERMANN, TOBIAS_HESS, PAUL_MAXIMILIAN_BITTNER)
			.and(IS_MASTERSTHESIS.negate())
			.and(WITH_PAUL_AT_ICG.negate())
			.and(b -> b.year >= 2020);

	public final static Predicate<Typo3Entry> BELONGS_TO_VARIANTSYNC = b -> {
		if (b.tags == null) return false;
		return b.tags.stream().anyMatch(Util.isOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
	};
	
	private Filters() {}
	
	/**
	 * The predicate returns true iff the entry's key matches one of the given keys.
	 */
	public static Predicate<Typo3Entry> keyIsOneOf(String... keys) {
		return b -> Arrays.asList(keys)
				.stream()
				.anyMatch(b.key::equals);
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
