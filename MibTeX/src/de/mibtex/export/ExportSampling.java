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
					+ "Further Tags");
			out.append(System.lineSeparator());
			for (BibtexEntry entry : entries.values()) {
				for (List<String> tags : entry.tagList) {
					out.append(getEntry(entry,tags));
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
		StringBuilder b = new StringBuilder();
		b.append(ESC + entry.author + ESC + SEP);
		b.append(entry.venue + SEP);
		b.append(entry.year + SEP);
		b.append(ESC + entry.title + ESC + SEP);
		b.append(ESC + getName(tags) + ESC + SEP);
		tags = readTags(tags);
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

	private List<String> readTags(List<String> allTags) {
		List<String> tags = new ArrayList<String>();
		for (String tag : allTags)
			if (!tag.startsWith("classified by") && !tag.startsWith("subsumed by") && !tag.startsWith(NAME_PREFIX))
				tags.add(tag);
		return tags;
	}

	private String getName(List<String> tags) {
		for (String tag : tags)
			if (tag.startsWith(NAME_PREFIX)) {
				return tag.substring(NAME_PREFIX.length());
			}
		return "";
	}

	public final static String[][] TAGS = { { "greedy", "evolutionary", "non-evalutionary", "manual" },
			{ "feature model", "domain knowledge", "code artifacts", "test artifacts" },
			{ "feature-wise coverage", "pair-wise coverage", "t-wise coverage", "statement coverage", "block coverage",
					"no coverage" },
			{ "sampling efficiency", "testing efficiency", "effectiveness", "no tool", "unavailable tool",
					"available tool", "open-source tool", "evaluation", "no evaluation" } };

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
