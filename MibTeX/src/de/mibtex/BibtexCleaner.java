package de.mibtex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class to remove unnecessary entries from Bibtex files.
 * 
 * @author Thomas Thuem
 * 
 */
public class BibtexCleaner {

	public static void main(String[] args) {
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
				if (!line.trim().startsWith("doi") && !line.trim().startsWith("issn") && !line.trim().startsWith("isbn")
						&& !line.trim().startsWith("url") && !line.trim().startsWith("month")
						&& !line.trim().startsWith("location") && !line.trim().startsWith("address")) {
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
