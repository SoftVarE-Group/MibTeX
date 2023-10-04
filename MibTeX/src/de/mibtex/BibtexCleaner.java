package de.mibtex;

import de.mibtex.args.ArgParser;
import de.mibtex.args.NamedArgument;
import de.mibtex.args.NamelessArgument;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A class to remove unnecessary entries from Bibtex files.
 * 
 * @author Thomas Thuem, Paul Bittner
 * 
 */
public class BibtexCleaner {
	private static final List<String> DEFAULT_ATTRIBUTES_TO_REMOVE = List.of(
			"doi",
			"issn",
			"isbn",
			"url",
			"month",
			"location",
			"address"
	);
	
	public static void main(String[] args) {
		final AtomicReference<File> inputFile = new AtomicReference<>(null);
		final AtomicReference<File> outputFile = new AtomicReference<>(null);
		final List<String> attributesToRemove = new ArrayList<>(DEFAULT_ATTRIBUTES_TO_REMOVE);

		final ArgParser argParser = new ArgParser(
				new NamelessArgument(
						"path to bibtex file to clean",
						filepath -> {
							inputFile.set(new File(filepath));
						}
				),
				new NamedArgument(
						"k", "keep-attributes",
						"Followed by a space-separated list of bibtex attribute names (from " + DEFAULT_ATTRIBUTES_TO_REMOVE + "). These attributes will be kept upon cleaning.",
						NamedArgument.Arity.ANY,
						false,
						null,
						attributesToRemove::removeAll 
				),
				new NamedArgument(
						"o", "out",
						"Explicitly specify the output file. If omitted, a suitable name will be generated, starting with 'literature-cleaned'.",
						NamedArgument.Arity.ONE,
						false,
						null,
						filepath -> {
							outputFile.set(new File(filepath.get(0)));
						}
				)
		);
		argParser.parse(args);

		final StringBuilder suffix = new StringBuilder("-cleaned");

		for (final String attribThatIsUsuallyRemoved : DEFAULT_ATTRIBUTES_TO_REMOVE) {
			if (!attributesToRemove.contains(attribThatIsUsuallyRemoved)) {
				if (!attributesToRemove.contains(attribThatIsUsuallyRemoved)) {
					suffix.append("-with").append(attribThatIsUsuallyRemoved);
				}
			}
		}

		processBibtexFile(inputFile.get(), outputFile.get(), suffix.toString(), attributesToRemove);
	}

	public static void processBibtexFile(final File inputFile, File outputFile, final String suffix, final List<String> attributesToRemove) {
		if (outputFile == null) {
			final String outputFileName = inputFile.getAbsolutePath().replaceAll("[.]bib$", suffix + ".bib");
			outputFile = new File(outputFileName);
		}
		System.out.println("Cleaning \n  " + inputFile.getAbsolutePath() + "\nto\n  " + outputFile.getAbsolutePath());
		System.out.println("Removing attributes " + attributesToRemove + ".");
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(inputFile));
			BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
			String line = null;
			while ((line = in.readLine()) != null) {
				String trimmedLine = line.trim();
				if (attributesToRemove.stream().noneMatch(trimmedLine::startsWith)) {
					out.write(line + "\r\n");
				}
			}
			in.close();
			out.close();
			System.out.println("done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
