/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

/**
 * A method to calculate the edit distance between two strings.
 * 
 * Adapted from: http://mrfoo.de/archiv/1176-Levenshtein-Distance-in-Java.html
 * 
 * @author Thomas Thuem
 */
public class Levenshtein {

	public static int getDistance(String s, String t) {
		int n = s == null ? 0 : s.length();
		int m = t == null ? 0 : t.length();
		if (n == 0)
			return m;
		if (m == 0)
			return n;

		int p[] = new int[n + 1];
		int d[] = new int[n + 1];
		int _d[];
		char t_j;
		int cost;

		for (int i = 0; i <= n; i++)
			p[i] = i;
		for (int j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;
			for (int i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1]
						+ cost);
			}
			_d = p;
			p = d;
			d = _d;
		}
		return p[n];
	}

}