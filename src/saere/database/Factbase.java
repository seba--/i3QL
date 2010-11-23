package saere.database;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import saere.Term;

/**
 * Class to read and store facts ({@link Term}s) in a linked list. The 
 * {@link Factbase} singleton is used as data source by the 
 * {@link ListDatabase} and {@link TrieDatabase}.
 * 
 * @author David Sullivan
 * @version 0.21, 10/14/2010
 */
public final class Factbase {
	
	private static final Factbase INSTANCE = new Factbase();
	
	private final Deque<Term> facts;
	
	private Factbase() {
		facts = new LinkedList<Term>(); // must also support list interface
	}
	
	public static Factbase getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Adds a fact to the {@link Factbase}.
	 * 
	 * @param fact The fact to add.
	 */
	public void add(Term fact) {
		facts.push(fact); // reverses the original order
	}
	
	/**
	 * Gets all facts.
	 * 
	 * @return All facts.
	 */
	@SuppressWarnings("unchecked")
	public List<Term> getFacts() {
		return (List<Term>) facts;
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
		DatabaseTermFactory.getInstance().reset();
		facts.clear();
	}
	
	/**
	 * The size of the {@link Factbase}.
	 * 
	 * @return The size of the {@link Factbase}.
	 */
	public int size() {
		return facts.size();
	}
}
