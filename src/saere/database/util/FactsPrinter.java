package saere.database.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

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
	private static final Charset ENCODING = Charset.forName("UTF-8");
	private static final String DECLARATIONS = 
		":- discontiguous(class_file/10)." + NEWLINE +
		":- discontiguous(class_file_source/2)." + NEWLINE +
		":- discontiguous(enclosing_method/4)." + NEWLINE +
		":- discontiguous(annotation/4)." + NEWLINE +
		":- discontiguous(annotation_default/2)." + NEWLINE +
		":- discontiguous(parameter_annotations/3)." + NEWLINE +
		":- discontiguous(field/11)." + NEWLINE +
		":- discontiguous(field_value/2)." + NEWLINE +
		":- discontiguous(method/15)." + NEWLINE +
		":- discontiguous(method_exceptions/2)." + NEWLINE +
		":- discontiguous(method_line_number_table/2)." + NEWLINE +
		":- discontiguous(method_local_variable_table/2)." + NEWLINE +
		":- discontiguous(method_exceptions_table/2)." + NEWLINE +
		":- discontiguous(instr/3)." + NEWLINE +
		":- discontiguous(inner_classes/2)." + NEWLINE;
	
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
			file.write(DECLARATIONS.getBytes(ENCODING));
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
		s += "." + NEWLINE;
		return s.getBytes(ENCODING);
	}
}
