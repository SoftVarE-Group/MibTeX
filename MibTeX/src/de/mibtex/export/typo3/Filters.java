package de.mibtex.export.typo3;

import java.util.Arrays;
import java.util.function.Predicate;

public class Filters {
	public final static String ThomasThuem = "Thomas Thüm";
	public final static String ChicoShundermann = "Chico Sundermann";
	public final static String TobiasHess = "Tobias Heß";
	public final static String PaulMBittner = "Paul Maximilan Bittner";
	
	public final static Predicate<Typo3Entry> Is_misc = b -> b.type.equals("misc");
	public final static Predicate<Typo3Entry> Is_proceedings = b -> b.type.equals("proceedings");
	public final static Predicate<Typo3Entry> Is_techreport = b -> b.type.equals("techreport");
	public final static Predicate<Typo3Entry> Is_bachelorsthesis =
			b -> b.type.equals("mastersthesis") && b.typeAttrib.toLowerCase().startsWith("bachelor");
	public final static Predicate<Typo3Entry> Is_mastersthesis = b -> b.type.equals("mastersthesis");
	public final static Predicate<Typo3Entry> Is_phdthesis = b -> b.type.equals("phdthesis");

	public final static Predicate<Typo3Entry> WithThomas = Filters.AuthorOrEditorIsOneOf(ThomasThuem);
	public final static Predicate<Typo3Entry> WithThomasBeforeUlm = WithThomas.and(b -> b.year < 2020);
	public final static Predicate<Typo3Entry> WithThomasAtUlm = WithThomas.and(b -> b.year >= 2020);

	public final static Predicate<Typo3Entry> SoftVarE = Filters.AuthorOrEditorIsOneOf(ThomasThuem, ChicoShundermann, TobiasHess, PaulMBittner).and(b -> b.year >= 2020);

	public final static Predicate<Typo3Entry> BelongsToVariantSync = b -> {
		if (b.tags == null) return false;
		return b.tags.stream().anyMatch(IsOneOf("VariantSyncPub", "VariantSyncPre", "VariantSyncMT"));
	};
	
	private Filters() {}
	
	public static Predicate<Typo3Entry> KeyIsOneOf(String... keys) {
		return b -> Arrays.asList(keys)
				.stream()
				.anyMatch(b.key::equals);
	}
	
	public static Predicate<Typo3Entry> AuthorOrEditorIsOneOf(String... authors) {
		return AuthorIsOneOf(authors).or(EditorIsOneOf(authors));
	}
	
	public static Predicate<Typo3Entry> AuthorIsOneOf(String... authors) {
		return b -> AnyMatch(b.authors::contains, authors);
	}
	
	public static Predicate<Typo3Entry> EditorIsOneOf(String... editors) {
		return b -> AnyMatch(b.editors::contains, editors);
	}
	
	@SafeVarargs
	public static <T> Predicate<T> IsOneOf(T... elements) {
		return s -> AnyMatch(s::equals, elements);
	}
	
	public static <T> boolean AnyMatch(Predicate<T> condition, T... elements) {
		return Arrays.asList(elements).stream().anyMatch(condition);
	}
	
	public static <T> Predicate<T> Any() {
		return x -> true;
	}
}
