package de.mibtex;

import java.io.File;

public class FileUtils {
    public final static String LINEBREAK = "\r\n";

	public static File concat(File dir, File path) {
		return concat(dir, path.toString());
	}
	
	public static File concat(File dir, String path) {
		if (dir.toString().isEmpty()) {
			return new File(path);
		}
		return new File(dir, path);
	}
	
	public static File concat(String dir, String path) {
		return concat(new File(dir), path);
	}
}
