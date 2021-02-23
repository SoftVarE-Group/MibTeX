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
 * characteristics of sampling.
 * 
 * @author Thomas Thuem
 */
public class ExportSampling extends Export {

	String fileName = "sampling.csv";

	public final static String[] TAG_CATEGORIES = { "Input Data", "Algorithm Category", "Coverage", "Evaluation",
			"Application" };

	// other tags: name=X,compared to Y,subsumed by Z,usage of A,evaluation of B
	public final static String[][] TAGS = {
			{ "feature model", "product set", "expert knowledge", "implementation artifacts", "test artifacts"},
			{ "greedy", "local search", "population-based search", "manual selection", "semi-automatic selection" }, // , "automatic selection"
			{ "feature-wise coverage", "pair-wise coverage", "t-wise coverage", "code coverage", "specification coverage",
					"requirements coverage", "no coverage guarantee" },
			{ "sampling efficiency", "testing efficiency", "effectiveness", "unavailable tool", "available tool", "open-source tool", "evaluation" },
			{ "testing", "type checking", "data-flow analysis", "non-functional properties" } };

	static final String NAME_PREFIX = "name = ";

	public final static String SEP = ",";

	public final static String ESC = "\"";

	public ExportSampling(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		File file = new File(BibtexViewer.CITATION_DIR, fileName);
		System.out.print("Updating " + file.getName() + "... ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			writeHeader(out);
			writeEntries(out);
			writeFooter(out);
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
	}

	void writeHeader(BufferedWriter out) throws IOException {
		out.append("Authors" + SEP + "Venue" + SEP + "Year" + SEP + "Title" + SEP + "Algorithm" + SEP);
		for (String category : TAG_CATEGORIES) {
			out.append(category + SEP);
		}
		out.append("Further Tags" + System.lineSeparator());
	}

	void writeFooter(BufferedWriter out) throws IOException {
		// nothing to do for CSV files
	}

	void writeEntries(BufferedWriter out) throws IOException {
		for (BibtexEntry entry : entries.values()) {
			for (List<String> tags : entry.tagList.values()) {
				if (isEntryInScope(tags)) {
					writeEntry(out, entry, tags);
				}
			}
		}
	}

	void writeEntry(BufferedWriter out, BibtexEntry entry, List<String> tags) throws IOException {
		out.append(ESC + entry.author + ESC + SEP);
		out.append(entry.venue + SEP);
		out.append(entry.year + SEP);
		out.append(ESC + entry.title + ESC + SEP);
		out.append(ESC + getName(tags) + ESC + SEP);
		tags = filterTags(tags);
		for (int i = 0; i < TAGS.length; i++)
			out.append(ESC + getTags(tags, i) + ESC + SEP);
		out.append(ESC);
		if (!tags.isEmpty())
			out.append(tags.remove(0));
		for (String tag : tags)
			out.append(", " + tag);
		out.append(ESC);
		out.append(System.lineSeparator());
	}

	boolean isEntryInScope(List<String> tags) {
//		for (String tag : tags) {
//			if ("SPLC18".equals(tag)) {
				return true;
//			}
//		}
//		return false;
	}

	String getName(List<String> tags) {
		for (String tag : tags) {
			if (tag.startsWith(NAME_PREFIX)) {
				return tag.substring(NAME_PREFIX.length());
			}
		}
		return "";
	}

	List<String> filterTags(List<String> allTags) {
		List<String> tags = new ArrayList<String>();
		for (String tag : allTags)
			if (!tag.startsWith(NAME_PREFIX) && !tag.startsWith("classified by")
//					&& !"SPLC18".equals(tag) && !tag.startsWith("subsumed by") && !tag.startsWith("no tool") && !tag.startsWith("no evaluation")
				)
				tags.add(tag);
		return tags;
	}

	String getTags(List<String> tags, int i) {
		String result = "";
		for (String keyword : TAGS[i])
			if (tags.contains(keyword)) {
				result += ", " + keyword;
				tags.remove(keyword);
			}
		return result.length() <= 2 ? "" : result.substring(2);
	}

}
