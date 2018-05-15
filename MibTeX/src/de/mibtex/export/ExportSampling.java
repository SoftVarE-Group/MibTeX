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

	private static final String NAME_PREFIX = "name=";

	public ExportSampling(String path, String file) throws Exception {
		super(path, file);
	}

	public final static String SEP = ",";

	public final static String ESC = "\"";

	@Override
	public void writeDocument() {
		File file = new File(BibtexViewer.CITATION_DIR, "sampling.csv");
		System.out.print("Updating " + file.getName() + "... ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			out.append("Authors" + SEP + "Venue" + SEP + "Year" + SEP + "Title" + SEP + "Algorithm" + SEP
					+ "Algorithm Category" + SEP + "Input Data" + SEP + "Coverage" + SEP + "Evaluation" + SEP
					+ "Application" + SEP + "Further Tags");
			out.append(System.lineSeparator());
			for (BibtexEntry entry : entries.values()) {
				for (List<String> tags : entry.tagList.values()) {
					out.append(getEntry(entry, tags));
				}
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
	}

	private String getEntry(BibtexEntry entry, List<String> tags) {
		if (!isEntryInScope(tags)) {
			return "";
		}
		StringBuilder b = new StringBuilder();
		b.append(ESC + entry.author + ESC + SEP);
		b.append(entry.venue + SEP);
		b.append(entry.year + SEP);
		b.append(ESC + entry.title + ESC + SEP);
		b.append(ESC + getName(tags) + ESC + SEP);
		tags = filterTags(tags);
		for (int i = 0; i < TAGS.length; i++)
			b.append(ESC + getTags(tags, i) + ESC + SEP);
		b.append(ESC);
		if (!tags.isEmpty())
			b.append(tags.remove(0));
		for (String tag : tags)
			b.append(", " + tag);
		b.append(ESC);
		b.append(System.lineSeparator());
		return b.toString();
	}

	private boolean isEntryInScope(List<String> tags) {
		for (String tag : tags) {
			if ("SPLC18".equals(tag)) {
				return true;
			}
		}
		return true; //ignore SPLC18 tag for now
	}

	private String getName(List<String> tags) {
		for (String tag : tags) {
			if (tag.startsWith(NAME_PREFIX)) {
				return tag.substring(NAME_PREFIX.length());
			}
		}
		return "";
	}

	private List<String> filterTags(List<String> allTags) {
		List<String> tags = new ArrayList<String>();
		for (String tag : allTags)
			if (!"SPLC18".equals(tag) && !tag.startsWith("classified by") && !tag.startsWith("subsumed by") && !tag.startsWith(NAME_PREFIX))
				tags.add(tag);
		return tags;
	}

	public final static String[][] TAGS = { { "greedy", "evolutionary", "manual selection" },
			{ "feature model", "domain knowledge", "code artifacts", "test artifacts", "product set" },
			{ "feature-wise coverage", "pair-wise coverage", "3-wise coverage", "4-wise coverage", "5-wise coverage",
					"6-wise coverage", "t-wise coverage", "statement coverage", "block coverage", "requirements coverage", 
					"no coverage guarantee" },
			{ "sampling efficiency", "testing efficiency", "effectiveness", "no tool", "tool unavailable",
					"available tool", "open-source tool", "evaluation", "no evaluation" },
			{ "testing", "type checking", "data-flow analysis", "non-functional properties" } };

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
