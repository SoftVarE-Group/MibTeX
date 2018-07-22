/* MibTeX - Minimalistic tool to manage your references with BibTeX
 *
 * Distributed under BSD 3-Clause License, available at Github
 *
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.citationservice;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A class that reads all BibTeX entries from a .csv file, gets the citations
 * and put the entries back in the .csv file
 *
 * @author Christopher Sontag, Thomas Thuem
 */
public class ScholarService extends Thread {

	/*
	 * Delay in minutes - Standard 18 min
	 */
	private static final int MIN_DELAY = 18;

	private static final int EXTRA_DELAY = 2;
	
	private static final int MIN_ROBOT = 1440;

	private Random rand = new Random();

	private File citationsFile;

	private File problemsFile;

	public ScholarService(File citationsFile, File problemsFile) {
		this.citationsFile = citationsFile;
		this.problemsFile = problemsFile;
	}

	@Override
	public void run() {
		while (true) {
			List<CitationEntry> entries = readFromFile(citationsFile);
			CitationEntry entry = nextEntry(entries);
			int oldCitations = entry.getCitations();
			boolean success = entry.updateCitations();
			if (entry.getCitations() != CitationEntry.PROBLEM_OCCURED) {
				writeToFile(citationsFile, entries);
			}
			if (oldCitations > 0 && entry.getCitations() <= 0) {
				List<CitationEntry> problems = readFromFile(problemsFile);
				problems.add(entry);
				writeToFile(problemsFile, problems);
			}
			try {
				if (!success) System.out.println("Will wait for "+ MIN_ROBOT + " minutes to avoid robot detection");
				sleep((success ? MIN_DELAY : MIN_ROBOT) * 60 * 1000 + rand.nextInt(EXTRA_DELAY * 1000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected CitationEntry nextEntry(List<CitationEntry> entries) {
		if (entries.size() > 0) {
			CitationEntry next = entries.get(0);
			for (CitationEntry entry : entries) {
				if (entry.getCitations() == CitationEntry.UNINITIALIZED)
					return entry;
				if (entry.getLastUpdate() < next.getLastUpdate())
					next = entry;
			}
			return next;
		}
		return null;
	}

	protected List<CitationEntry> readFromFile(File file) {
		System.out.print("Reading " + file.getName() + "... ");
		List<CitationEntry> entries = new ArrayList<CitationEntry>();
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			for (String line; (line = br.readLine()) != null;) {
				entries.add(CitationEntry.getFromCSV(line));
			}
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
		return entries;
	}

	protected void writeToFile(File file, List<CitationEntry> entries) {
		System.out.print("Updating " + file.getName() + "... ");
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			for (CitationEntry entry : entries) {
				out.append(entry.getCSVString());
			}
			out.close();
		} catch (FileNotFoundException e) {
			System.out.println("Not found " + file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("IOException for " + file.getAbsolutePath());
		}
		System.out.println("done.");
	}

}
