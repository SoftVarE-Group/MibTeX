/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export.typo3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Collection of utility functions used in the exporter for Typo3Entries and associated classes.
 * @author Paul Maximilian Bittner
 */
public class Util {
	/**
	 * Creates a branching function for given condition, then and else case.
	 * @param condition The condition upon which'S result 'then' or 'otherwise' will be run.
	 * @param then The function to apply when the given condition is met for a given a.
	 * @param otherwise The function to apply when the given condition is not met for a given a.
	 * @return A function that for a given a, returns then(a) if the given condition is met, and otherwise returns otherwise(a).
	 */
	public static <A, B> Function<A, B> when(Predicate<A> condition, Function<A, B> then, Function<A, B> otherwise) {
		return a -> condition.test(a) ? then.apply(a) : otherwise.apply(a);
	}
	
	/**
	 * The same as @see when but without an else case (i.e., else case function identity).
	 */
	public static <A> Function<A, A> when(Predicate<A> condition, Function<A, A> then) {
		return when(condition, then, Function.identity());
	}
	
	/**
	 * The same as @see when but throws an error when the condition is not met by the argument.
	 */
	public static <A> Function<A, A> whenForced(Predicate<A> condition, Function<A, A> then, String errorMsg) {
		return when(condition, then, a -> {
			throw new IllegalArgumentException("Condition failed on \n" + a + "\n: " + errorMsg);});
	}
	
	/**
	 * @return A predicate that returns true iff the argument passed to that predicate is equal to at least one of the given elements (in terms of Object.equals).
	 */
	@SafeVarargs
	public static <T> Predicate<T> isOneOf(T... elements) {
		return s -> anyMatch(s::equals, elements);
	}
	
	/**
	 * @return True iff at least one of the given elements satisfies the given condition.
	 */
	@SafeVarargs
	public static <T> boolean anyMatch(Predicate<T> condition, T... elements) {
		return Arrays.asList(elements).stream().anyMatch(condition);
	}
	
	/**
	 * Returns a predicate that will always evaluate to true.
	 * This is a neutral filter in streams that does not filter any elements but keep the collection as is.
	 */
	public static <T> Predicate<T> any() {
		return x -> true;
	}
	
	/**
	 * Returns the number of duplicates in the given List 'values' with respect to Object.equals.
	 * The given list will be sorted.
	 * 
	 * @param values The list of objects to check for duplicates.
	 *               This list will be sorted.
	 * @param callback This function is invoked whenever a duplicate is found.
	 *                 The callback will be passed two objects from the list 'values' that are equal to one another with respect to Object.equals.
	 *                 This functions should not manipulate the values list.
	 * @return
	 */
	public static <T extends Comparable<T>> int getDuplicates(List<T> values, BiConsumer<T, T> callback) {
		Collections.sort(values);
		int duplicates = 0;
		for (int i = 0; i < values.size() - 1; ++i) {
			if (values.get(i).equals(values.get(i + 1))) {
				callback.accept(values.get(i), values.get(i + 1));
				++duplicates;
			}
		}
		return duplicates;
	}
	
	/**
	 * Splits all strings in the input list to separate strings.
	 * For example, given l = ["a", "b,c", "d;e,f"] as input, we get
	 * splitAttributeListString(l) = ["a", "b", "c", "d" , "e", "f"].
	 * @param tags
	 * @return
	 */
	public static List<String> splitAttributeListString(List<String> tags) {
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
	
	/**
	 * Find the first character in the given string that meets the given condition.
	 * Return that characters index.
	 * @return The index of the first character in the given string meeting the given condition.
	 */
	public static int indexOfFirstMatch(String string, Predicate<Character> condition) {
		return indexOfFirstMatch(string, condition, 0);
	}
	
	/**
	 * Find the first character at or after the given index in the given string that meets the given condition.
	 * Return that characters index.
	 * @param fromIndex The index in the string from which the search should begin. Characters with indices < fromIndex will not be considered.
	 * @return The index of the first character in the given string meeting the given condition.
	 */
	public static int indexOfFirstMatch(String string, Predicate<Character> condition, int fromIndex) {
		for (int i = fromIndex; i < string.length(); ++i) {
			if (condition.test(string.charAt(i))) {
				return i;
			}
		}
		return string.length();
	}
}
