/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * This is a collection of default filters to use for the ExportTypo3Bibtex.
 * Each filter is a java.util.Predicate that can be combined with propositional operators (and, or, negate, ...)
 * 
 * @author Paul Maximilian Bittner
 */
public class Filters {
	public final static String ThomasThuem = "Thomas Thüm";
	public final static String ChicoSundermann = "Chico Sundermann";
	public final static String TobiasHess = "Tobias Heß";
	public final static String PaulMBittner = "Paul Maximilian Bittner";
	
	public final static Predicate<Typo3Entry> Is_misc = b -> b.type.equals("misc");
	public final static Predicate<Typo3Entry> Is_proceedings = b -> b.type.equals("proceedings");
	public final static Predicate<Typo3Entry> Is_techreport = b -> b.type.equals("techreport");
	public final static Predicate<Typo3Entry> Is_bachelorsthesis =
			b -> b.type.equals("mastersthesis") && b.typeAttrib.toLowerCase().startsWith("bachelor");
	public final static Predicate<Typo3Entry> Is_mastersthesis = b -> b.type.equals("mastersthesis");
	public final static Predicate<Typo3Entry> Is_phdthesis = b -> b.type.equals("phdthesis");

	public final static Predicate<Typo3Entry> WithThomas = AuthorOrEditorIsOneOf(ThomasThuem).and(Is_misc.negate());
	public final static Predicate<Typo3Entry> WithThomasBeforeUlm = WithThomas.and(b -> b.year < 2020);
	public final static Predicate<Typo3Entry> WithThomasAtUlm = WithThomas.and(b -> b.year >= 2020);
	
	public final static Predicate<Typo3Entry> WithPaul = Filters.AuthorOrEditorIsOneOf(Filters.PaulMBittner);
	public final static Predicate<Typo3Entry> WithPaulAtICG = WithPaul.and(t -> t.source.getAttribute("pb-tags").contains("ICG"));
	public final static Predicate<Typo3Entry> WithPaulAtUlm =
			WithPaul
			.and(WithPaulAtICG.negate())
			.and(Is_mastersthesis.negate())
			.and(t -> t.year >= 2020);
	public final static Predicate<Typo3Entry> WithPaulBeforeOrNotAtUlm = WithPaul.and(WithPaulAtUlm.negate());

	public final static Predicate<Typo3Entry> SoftVarE = Filters.AuthorOrEditorIsOneOf(ThomasThuem, ChicoSundermann, TobiasHess, PaulMBittner).and(b -> b.year >= 2020);

	public final static Predicate<Typo3Entry> BelongsToVariantSync = b -> {
		if (b.tags == null) return false;
		return b.tags.stream().anyMatch(IsOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
	};
	
	private Filters() {}
	
	/**
	 * The predicate returns true iff the entry's key matches one of the given keys.
	 */
	public static Predicate<Typo3Entry> KeyIsOneOf(String... keys) {
		return b -> Arrays.asList(keys)
				.stream()
				.anyMatch(b.key::equals);
	}
	
	/**
	 * @return A predicate that returns true iff the entry's author list contains at least one of the given authors or if the editor list does so.
	 * An author string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
	 */
	public static Predicate<Typo3Entry> AuthorOrEditorIsOneOf(String... authors) {
		return AuthorIsOneOf(authors).or(EditorIsOneOf(authors));
	}

	
	/**
	 * @return A predicate that returns true iff the entry's author list contains at least one of the given authors.
	 * An author string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
	 */
	public static Predicate<Typo3Entry> AuthorIsOneOf(String... authors) {
		return b -> AnyMatch(b.authors::contains, authors);
	}

	
	/**
	 * @return A predicate that returns true iff the entry's editor list contains at least one of the given authors.
	 * An editor string should be in the format "firstname lastname" such as in the fields ThomasThuem, ChicoSundermann, ... in this class.
	 */
	public static Predicate<Typo3Entry> EditorIsOneOf(String... editors) {
		return b -> AnyMatch(b.editors::contains, editors);
	}
	
	/**
	 * @return A predicate that returns true iff the argument passed to that predicate is equal to at least one of the given elements (in terms of Object.equals).
	 */
	@SafeVarargs
	public static <T> Predicate<T> IsOneOf(T... elements) {
		return s -> AnyMatch(s::equals, elements);
	}
	
	/**
	 * @return True iff at least one of the given elements satisfies the given condition.
	 */
	@SafeVarargs
	public static <T> boolean AnyMatch(Predicate<T> condition, T... elements) {
		return Arrays.asList(elements).stream().anyMatch(condition);
	}
	
	/**
	 * Returns a predicate that will always evaluate to true.
	 * This is a neutral filter in streams that does not filter any elements but keep the collection as is.
	 */
	public static <T> Predicate<T> Any() {
		return x -> true;
	}
}
