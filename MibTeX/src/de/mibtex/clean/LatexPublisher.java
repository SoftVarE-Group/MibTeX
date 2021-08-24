/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex.clean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class to prepare LaTeX documents for publishing. It removes all generated files and comments.
 * 
 * @author Thomas Thuem, Paul Maximilian Bittner
 * 
 */
public class LatexPublisher {
	private final static char COMMENT_BEGIN = '%';
	
	private static class Options {
		// Set to true if comments starting with %%% should remain in the tex files.
		boolean allowDocsComments = true;
		boolean isACM = true;
		boolean removeShellScripts = true;
		boolean keepPDFs = true;

		/// Wrap in ArrayList to make mutable. Otherwise the list is immutable.
		final List<String> BLACKLISTED_FILES = new ArrayList<>(Arrays.asList(
				".svn"
				));
	
		final List<String> BLACKLISTED_FILE_ENDINGS = new ArrayList<>(Arrays.asList(
				".pdf",
				".toc",
				".tps",
				".tcp",
				".aux",
				".out",
				".bbl",
				".blg",
				".synctex",
				".synctex.gz",
				".log"
				));
		
		public Options() {
			validate();
		}
		
		private void validate() {
			/// Do not delete any tex files but clean them!
			assert !BLACKLISTED_FILE_ENDINGS.contains(".tex");
		}
		
		void apply() {
			// ACM requires the .bbl file
			if (isACM) {
				BLACKLISTED_FILE_ENDINGS.removeAll(Arrays.asList(
						".bbl"
						));
			}
			
			if (removeShellScripts)
			{
				BLACKLISTED_FILE_ENDINGS.addAll(Arrays.asList(
						".sh",
						".bat"
						));
			}
			
			if (keepPDFs) {
				BLACKLISTED_FILE_ENDINGS.remove(".pdf");
			} else {
				BLACKLISTED_FILE_ENDINGS.add(".pdf");
			}
			
			validate();
		}
		
		/**
		 * @return True iff a given file should be deleted upon project cleaning.
		 */
		boolean isBlackListed(String filename) {
			return BLACKLISTED_FILES.stream().anyMatch(filename::equals)
					|| BLACKLISTED_FILE_ENDINGS.stream().anyMatch(filename::endsWith);
		}
	}
	
	private static Options options;
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Expected exactly 1 argument: Path to latex project.");
			return;
		}
		
		options = new Options();
		
		/// Adapt options to your project here.
		/// TODO: Create an argument parser or ini file to parse options.
		options.allowDocsComments = true;
		options.isACM = true;
		options.removeShellScripts = true;
		options.keepPDFs = true;
		
		options.apply();
		processDirectory(new File(args[0]));
		
		System.out.println("\nFinished!");
	}

	private static void processDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (options.isBlackListed(file.getName())) {
				if (file.delete()) {
					System.out.println(file + " deleted.");
				} else {
					System.err.println(file + " could not be deleted!");
				}
			} else if (file.isDirectory()) {
				processDirectory(file);
			} else if (file.getName().endsWith(".tex")) {
				processLatexFile(file);
			}
		}
	}

	private static void processLatexFile(File file) {
		final File temp = new File(file + "~");
		
		if (!file.renameTo(temp)) {
			System.err.println("Skipping " + file.toString() + " because it could not be renamed to " + temp + "!");
			return;
		}
		
		try {
			final BufferedReader in = new BufferedReader(new FileReader(temp));
			final BufferedWriter out = new BufferedWriter(new FileWriter(file));
			
			String line = null;
			while ((line = in.readLine()) != null) {
				/// Find begin of inline comment.
				int pos = line.indexOf(COMMENT_BEGIN);
				/// Skip all usages of the percent sign itself (\%) as these do not denote comments.
				while (pos > 0 && line.charAt(pos - 1) == '\\') {
					pos = line.indexOf(COMMENT_BEGIN, pos + 1);
				}
				
				/// Keep the entire line if there is no comment or if the comment
				/// is for documentation purposes.
				if (pos < 0 || (options.allowDocsComments && isDocumentationComment(line, pos))) {
					out.write(line + "\r\n");
				} else {
					/* DEBUG: Show code that was removed.
					{
						final String removedPart = line.substring(pos);
						System.out.println(removedPart);
					}//*/

					//System.out.print(putInQuotes(line) + " with pos = " + pos + " > ");
					/* + 1 to keep the % sign itself.
					 * Removing it can cause side-effects as it also comments out the linebreak itself.
					 */
					line = line.substring(0, pos + 1);
					//System.out.println(putInQuotes(line));
					if (!line.isEmpty()) {
						out.write(line + "\r\n");
					}
				}
			}
			out.close();
			in.close();
			temp.delete();
			System.out.println(file + " processed.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return True iff the comment at commentBeginIndex in line starts with "%%%".
	 */
	private static boolean isDocumentationComment(String line, int commentBeginIndex) {
		final int charactersAfterBeginIndex = line.length() - 1 - commentBeginIndex;
		return charactersAfterBeginIndex >= 2
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex)
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex + 1)
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex + 2);
	}
	
	private static String putInQuotes(String s) {
		return "\"" + s + "\"";
	}
}
