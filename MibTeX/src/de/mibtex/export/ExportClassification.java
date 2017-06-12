/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

/**
 * A class that generates a .csv file containing entries classified with respect
 * to Thuem's classification.
 * 
 * @author Thomas Thuem
 */
public class ExportClassification extends Export {

	public ExportClassification(String path, String file) throws Exception {
		super(path, file);
	}

	public final static String SEP = ",";

	public final static String ESC = "\"";

	@Override
	public void writeDocument() {
		File file = new File(BibtexViewer.CITATION_DIR, "classification.csv");
		System.out.print("Updating " + file.getName() + "... ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.append("Key" + SEP + "Editor" + SEP);
			out.append("Authors" + SEP + "Venue" + SEP + "Year" + SEP + "Title"
					+ SEP + "Analysis Method" + SEP + "Analysis Strategy" + SEP
					+ "Implementation Strategy" + SEP
					+ "Specification Strategy" + SEP + "SE Layer" + SEP);
			out.append("Further Keywords" + SEP + SEP);
			out.append(System.lineSeparator());
			for (BibtexEntry entry : entries.values()) {
				if (!entry.tagList.isEmpty())
					out.append(toClassification(entry));
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
	}

	private String toClassification(BibtexEntry entry) {
		StringBuilder b = new StringBuilder();
		b.append(entry.key + SEP + SEP);
		b.append(ESC + entry.author + ESC + SEP);
		b.append(entry.venue + SEP);
		b.append(entry.year + SEP);
		b.append(ESC + entry.title + ESC + SEP);
		List<String> tags = new ArrayList<String>();
		for (List<String> tagList : entry.tagList)
			for (String tag : tagList)
				if (!tag.startsWith("classified by")
						&& !tag.startsWith("subsumed by"))
					tags.add(tag);
		for (int i = 0; i < 5; i++)
			b.append(ESC + getTags(tags, i) + ESC + SEP);
		b.append(ESC);
		if (!tags.isEmpty())
			b.append(tags.remove(0));
		for (String tag : tags)
			b.append(", " + tag);
		b.append(ESC + SEP + SEP);
		b.append(System.lineSeparator());
		return b.toString();
	}

	public final static String[][] TAGS = {
			{ "data-flow analysis", "family-specific analysis",
					"fault-tree analysis", "feature-model analysis",
					"model checking", "performance analysis", "refactoring",
					"runtime analysis", "static analysis", "symbolic analysis",
					"syntax checking", "synthesis", "test-case generation",
					"testing", "theorem proving", "type checking",
					"variant-preserving migration",
					"analysis method undefined", "analysis method independent" },
			{ "family-based analysis", "family-product-based analysis",
					"feature-based analysis", "feature-family-based analysis",
					"feature-product-based analysis",
					"product-family-based analysis",
					"regression-based analysis", "sample-based analysis",
					"unoptimized product-based analysis",
					"analysis strategy undefined",
					"optimized product-based analysis",
					"product-based analysis" },
			{ "clone-and-own", "build system", "preprocessor",
					"runtime variability", "components", "services",
					"plug-ins", "feature modules", "aspects", "delta modules",
					"implementation independent", "implementation undefined",
					"product-based implementation",
					"family-based implementation",
					"feature-based implementation",
					"feature-product-based implementation",
					"composition-based implementation",
					"annotation-based implementation" },
			{ "domain-independent specification", "family-wide specification",
					"product-based specification",
					"feature-based specification",
					"feature-product-based specification",
					"family-based specification", "specification independent",
					"specification undefined" },
			{ "requirements", "design", "source code", "program", "theory",
					"source code / program" } };

	private String getTags(List<String> tags, int i) {
		String result = "";
		for (String keyword : TAGS[i])
			if (tags.contains(keyword)) {
				result += ", " + keyword;
				tags.remove(keyword);
			}
		return result.length() <= 2 ? "" : result.substring(2);
	}

}
