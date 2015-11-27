/* MibTeX - Minimalistic tool to manage your references with BibTeX
 * 
 * Distributed under BSD 3-Clause License, available at Github
 * 
 * https://github.com/tthuem/MibTeX
 */
package de.mibtex;

/**
 * An abstract class to simplify arbitrary filtering of BibTeX entries.
 * 
 * @author Thomas Thuem
 */
abstract public class BibtexFilter {

	abstract public String getTitle();

	abstract public boolean include(BibtexEntry entry);

}
