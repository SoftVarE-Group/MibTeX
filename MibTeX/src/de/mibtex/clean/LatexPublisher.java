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
 * @author Thomas Thuem
 * 
 */
public class LatexPublisher {
	private final static char COMMENT_BEGIN = '%';
	
	private static class Blacklists {
		/// Wrap in ArrayList to make mutable. Otherwise the list is immutable.
		final static List<String> FILES = new ArrayList<>(Arrays.asList(
				".svn"
				));
	
		final static List<String> FILE_ENDINGS = new ArrayList<>(Arrays.asList(
				".pdf",
				".toc",
				".tps",
				".tcp",
				".aux",
				".out",
				//".bbl", // required by ACM
				".blg",
				".synctex",
				".synctex.gz",
				".log"
				));
		static {
			assert !FILE_ENDINGS.contains(".tex");
		}
		
		static boolean isBlackListed(String filename) {
			return FILES.stream().anyMatch(filename::equals)
					|| FILE_ENDINGS.stream().anyMatch(filename::endsWith);
		}
	}
	
	private static void jankyBlackListUpdateForFTR() {
		Blacklists.FILE_ENDINGS.removeAll(Arrays.asList(
				".pdf",
				".bbl"
				));
		Blacklists.FILE_ENDINGS.addAll(Arrays.asList(
				".sh",
				".bat"
				));
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Expected exactly 1 argument: Path to latex project.");
			return;
		}
		
		jankyBlackListUpdateForFTR();
		
		processDirectory(new File(args[0]));
	}

	private static void processDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (Blacklists.isBlackListed(file.getName())) {
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
				if (pos < 0 || isDocumentationComment(line, pos))
					out.write(line + "\r\n");
				else {
					//System.out.print(putInQuotes(line) + " with pos = " + pos + " > ");
					line = line.substring(0, pos).trim();
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
		return charactersAfterBeginIndex > 2
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex)
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex + 1)
				&& COMMENT_BEGIN == line.charAt(commentBeginIndex + 2);
	}
	
	private static String putInQuotes(String s) {
		return "\"" + s + "\"";
	}
}
