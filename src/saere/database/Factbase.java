package saere.database;

import java.util.LinkedList;
import java.util.List;

import saere.Term;

/**
 * Class to read and store facts ({@link Term}s) in a linked list. The 
 * {@link Factbase} singleton is used as data source by the 
 * {@link ListDatabase} and {@link TrieDatabase}.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public class Factbase {
	
	private static final Factbase INSTANCE = new Factbase();
	
	private final List<Term> facts;
	
	private Factbase() {
		facts = new LinkedList<Term>();
	}
	
	public static Factbase getInstance() {
		return INSTANCE;
	}
	
	public void add(Term fact) {
		facts.add(fact);
	}
	
	/**
	 * Gets all facts.
	 * 
	 * @return All facts.
	 */
	public List<Term> getFacts() {
		return facts;
	}
	
	/**
	 * Reads facts from a specified file (.zip, .jar or .class) using BAT.
	 * 
	 * @param filename The filename.
	 */
	public void read(String filename) {
		FactbaseBytecodeReader reader = new FactbaseBytecodeReader();
		if (filename.endsWith(".zip") || filename.endsWith(".jar")) {
			reader.processFile(filename);
		} else if (filename.endsWith(".class")) {
			reader.processClass(filename);
		} else {
			throw new UnsupportedOperationException("Can only read .zip, .jar or .class files");
		}
	}
	
	/**
	 * Drops all facts.
	 */
	public void drop() {
		DatabaseTermFactory.getInstance().resetIdCounter();
		facts.clear();
	}
}
