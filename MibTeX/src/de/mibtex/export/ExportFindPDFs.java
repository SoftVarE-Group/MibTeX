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

public class ExportFindPDFs extends Export {

	public ExportFindPDFs(String path, String file) throws Exception {
		super(path, file);
	}

	@Override
	public void writeDocument() {
		final boolean dryRun = true;
		
		final Scanner scan = new Scanner(System.in);
		
		final JFileChooser chooser = new JFileChooser();
		final String[] validExtensions = new String[] { "pdf", "rtf", "doc", "docx"};
		chooser.setFileFilter(new FileNameExtensionFilter("Document " + Arrays.toString(validExtensions), validExtensions));
		chooser.setCurrentDirectory(new File(BibtexViewer.OUTPUT_DIR));

		BiFunction<File, File, Boolean> dry = (from, to) -> {
			System.out.println("  > Performing dry run! Nothing happened!");
			return true;
		};
    	BiFunction<File, File, Boolean> move = (from, to) -> from.renameTo(to);
    	BiFunction<File, File, Boolean> copy = (from, to) -> {
			try {
				Files.copy(from.toPath(), to.toPath());
				return true;
			} catch (IOException e) {
				System.out.println("Error on copying file: [" + from + "] -> [" + to + "]");
				e.printStackTrace();
			}
			
			return false;
		};
    	
		BiFunction<File, File, Boolean> action = dry;
    	if (!dryRun) {
    		action = copy;
    	}

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

}
