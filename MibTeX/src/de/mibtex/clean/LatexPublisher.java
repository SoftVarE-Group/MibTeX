package de.mibtex.clean;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A class to prepare LaTeX documents for publishing. It removes all generated files and comments.
 * 
 * @author Thomas Thuem
 * 
 */
public class LatexPublisher {

	public static void main(String[] args) {
		File dir = new File("C:\\Users\\tthuem\\Desktop\\tex");
		processDirectory(dir);
	}

	private static void processDirectory(File dir) {
		for (File file : dir.listFiles()) {
			if (file.getName().equals(".svn")
					|| file.getName().endsWith(".pdf")
					|| file.getName().endsWith(".toc")
					|| file.getName().endsWith(".tps")
					|| file.getName().endsWith(".tcp")
					|| file.getName().endsWith(".aux")
					|| file.getName().endsWith(".out")
					|| file.getName().endsWith(".bbl")
					|| file.getName().endsWith(".blg")
					|| file.getName().endsWith(".synctex")
					|| file.getName().endsWith(".log")) {
				if (file.delete())
					System.out.println(file + " deleted");
				else
					System.err.println(file + " could not be deleted");
			} else if (file.isDirectory())
				processDirectory(file);
			else if (file.getName().endsWith(".tex"))
				processLatexFile(file);
		}
	}

	private static void processLatexFile(File file) {
		File temp = new File(file + "~");
		file.renameTo(temp);
		try {
			BufferedReader in = new BufferedReader(new FileReader(temp));
			BufferedWriter out = new BufferedWriter(new FileWriter(file));
			String line = null;
			while ((line = in.readLine()) != null) {
				int pos = line.indexOf('%');
				while (pos > 0 && line.charAt(pos - 1) == '\\')
					pos = line.indexOf('%', pos + 1);
				if (pos < 0)
					out.write(line + "\r\n");
				else {
					// System.out.print(line + " > ");
					line = line.substring(0, line.indexOf("%") + 1);
					// System.out.println(line);
					out.write(line + "\r\n");
				}
			}
			out.close();
			in.close();
			temp.delete();
			System.out.println(file + " processed");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
