package saere.database.util;

import java.io.FileOutputStream;
import java.io.IOException;

import saere.Term;
import saere.database.Factbase;
import saere.database.Utils;

/**
 * Can print the facts from the {@link Factbase} to a file.
 * 
 * @author David Sullivan
 * @version 1.0, 11/16/2010
 */
public final class FactsPrinter {
	
	private static final String NEWLINE = System.getProperty("line.separator");
	
	/**
	 * Prints the facts of the {@link Factbase} to the specified file.
	 * 
	 * @param filename The name of the file.
	 */
	public static void print(String filename) {
		print(Factbase.getInstance().getFacts(), filename);
	}
	
	public static void print(Iterable<Term> facts, String filename) {
		FileOutputStream file = null;
		try {
			file = new FileOutputStream(filename);
			for (Term fact : facts) {
				file.write(toBytes(fact));
			}
		} catch (IOException e) {
			System.err.println("Unable to print to file " + filename);
		} finally {
			if (file != null)
				try { file.close(); } catch (Exception e) { /* ignored */ }
		}
	}
	
	private static byte[] toBytes(Term term) {
		String s = Utils.termToString(term);
		s = s.replace('\n', ' ');
		s = s.replace('\r', ' ');
		s += NEWLINE;
		return s.getBytes();
	}
}
