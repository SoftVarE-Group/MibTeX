package de.mibtex.export.typo3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Util {
	public static <A> Function<A, A> If(Predicate<A> condition, Function<A, A> f) {
		return a -> condition.test(a) ? f.apply(a) : a;
	}
	
	public static <A> Function<A, A> IfForced(Predicate<A> condition, Function<A, A> f, String errorMsg) {
		return a -> {
			if (condition.test(a)) {
				return f.apply(a);
			}
			throw new IllegalArgumentException("Condition failed on \n" + a + "\n: " + errorMsg);
		};
	}
	
	/**
	 * Splits all strings in the input list to separate strings.
	 * For example, given l = ["a", "b,c", "d;e,f"] as input, we get
	 * splitAttributeListString(l) = ["a", "b", "c", "d" , "e", "f"].
	 * @param tags
	 * @return
	 */
	public static List<String> SplitAttributeListString(List<String> tags) {
		Function<String, Function<List<String>, List<String>>> splitByDivider = div -> l -> {
			return l.stream().collect(
					() -> new ArrayList<String>(),
					(list, kw) -> ((List<String>)list).addAll(
							Arrays.stream(kw.split(div))
							.map(String::trim)
							.collect(Collectors.toList())),
					(list1, list2) -> list1.addAll(list2));
		};
		
		return Arrays.asList(",", ";").stream()
				.map(splitByDivider)
				.reduce(Function.identity(), Function::compose)
				.apply(tags);
	}
}
