/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Scanner;
import java.util.function.BiFunction;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.mibtex.BibtexEntry;
import de.mibtex.BibtexViewer;

/**
 * Exporter that wraps a small console application to find the pdfs of bibtex entries.
 * ExportFindPDFs will ask you for each publication if you have a pdf.
 * If you have it, you can point the application to the file which will then move the
 * file to your literature directory and name it such that the mibtex website will find it.
 * 
 * @author Paul Maximilian Bittner
 *
 */
public class ExportFindPDFs extends Export {

	public ExportFindPDFs(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		// Specify if this should be a dry run or not.
		// In dry run, no files on disk will be altered.
		// It is used for debugging and to simulate potential changes.
		BiFunction<File, File, Boolean> action =
				ExportFindPDFs::handlefile_dry
//				ExportFindPDFs::handlefile_copy
//				ExportFindPDFs::handlefile_move
				;

		final Scanner scan = new Scanner(System.in);
		final JFileChooser chooser = createFileChooser();

		File path;
		for (BibtexEntry entry : entries.values()) {
			path = entry.getPDFPath();
			boolean fileSkipped = true;
			if (!path.exists())
			{
				System.out.println("Looking for " + path.getName());
				System.out.print("Want to find this file? (y/N)\n  > ");
				String answer = scan.nextLine().toLowerCase();

				if (answer.startsWith("y") || answer.startsWith("t")) {
					if (chooser.showOpenDialog(null /*no parent gui to block*/) == JFileChooser.APPROVE_OPTION) {
						File userSubmittedFile = chooser.getSelectedFile();
						if (action.apply(userSubmittedFile, path)) {
							System.out.print("  > success");
							fileSkipped = false;
						}
					}
				} 

				if (fileSkipped) {
					System.out.println("  > file skipped");
				}

				if (answer.startsWith("e") || answer.startsWith("q")) {
					System.out.println("Exiting");
					break;
				}

				System.out.println();
			}
		}

		scan.close();
	}
	
	private static JFileChooser createFileChooser() {
		final JFileChooser chooser = new JFileChooser();
		final String[] validExtensions = new String[] { "pdf", "rtf", "doc", "docx"};
		chooser.setFileFilter(new FileNameExtensionFilter("Document " + Arrays.toString(validExtensions), validExtensions));
		chooser.setCurrentDirectory(new File(BibtexViewer.OUTPUT_DIR));
		return chooser;
	}

	/// Methods to handle files selected by the user

	/**
	 * Does nothing.
	 * Use this for debugging.
	 * The file from is not altered.
	 * Instead, this method will print a notification that this is a dry run.
	 */
	static boolean handlefile_dry(File from, File to) {
		System.out.println("  > Performing dry run! Nothing happened!");
		return true;
	}

	/**
	 * Moves the file 'from' to the file 'to'.
	 * The file 'to' should not exist when invoking this method.
	 */
	static boolean handlefile_move(File from, File to) {
		return from.renameTo(to);
	}

	/**
	 * Creates a copy of the file 'from' as the file 'to'.
	 * The file 'to' should not exist when invoking this method.
	 */
	static boolean handlefile_copy(File from, File to) {
		try {
			Files.copy(from.toPath(), to.toPath());
			return true;
		} catch (IOException e) {
			System.out.println("Error on copying file: [" + from + "] -> [" + to + "]");
			e.printStackTrace();
		}

		return false;
	};
}
