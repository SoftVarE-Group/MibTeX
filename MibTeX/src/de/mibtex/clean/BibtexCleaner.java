package de.mibtex.clean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * A class to remove unnecessary entries from Bibtex files.
 * 
 * @author Thomas Thuem
 * 
 */
public class BibtexCleaner {
	private static List<String> attributesToRemove;

	public static void main(String[] args) {
		attributesToRemove = Arrays.asList(
				  "doi",
				  "issn",
				  "isbn",
				  "url",
				  "month",
				  "location",
				  "address"
				);
		
		File file = new File(args[0]);
		processBibtexFile(file);
	}

	public static void processDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.getName().endsWith(".bib"))
				processBibtexFile(file);
		}
	}

	public static void processBibtexFile(File file) {
		String filename = file.getAbsolutePath().replaceAll("[.]bib$", "-cleaned.bib");
		File newFile = new File(filename);
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			BufferedWriter out = new BufferedWriter(new FileWriter(newFile));
			String line = null;
			while ((line = in.readLine()) != null) {
				String trimmedLine = line.trim();
				if (attributesToRemove.stream().noneMatch(trimmedLine::startsWith)) {
					out.write(line + "\r\n");
				}
			}
			in.close();
			out.close();
			System.out.println(file + " processed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
