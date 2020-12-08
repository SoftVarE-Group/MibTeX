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
	 * @return A function that for a given a, returns f(a) if the given condition is met, and otherwise returns a.
	 */
	public static <A> Function<A, A> If(Predicate<A> condition, Function<A, A> f) {
		return a -> condition.test(a) ? f.apply(a) : a;
	}
	
	/**
	 * The same as @see If but throws an error when the condition is not met by the argument.
	 */
	public static <A> Function<A, A> IfForced(Predicate<A> condition, Function<A, A> f, String errorMsg) {
		return a -> {
			if (condition.test(a)) {
				return f.apply(a);
			}
			throw new IllegalArgumentException("Condition failed on \n" + a + "\n: " + errorMsg);
		};
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
