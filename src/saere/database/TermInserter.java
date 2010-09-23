package saere.database;

import saere.Term;

/**
 * A term inserter inserts a {@link Term} into a trie using its representation 
 * created by a {@link TermFlattener}. New {@link Trie} nodes may be created 
 * and the original {@link Trie} may be restructured during this process as 
 * required.
 * 
 * @author David Sullivan
 * @version 0.1, 9/22/2010
 */
public abstract class TermInserter {
	
	/** The root {@link Trie} where the insertion process always starts. */
	protected Trie root = null;
	
	/** The current {@link Trie} (not the inserted {@link Term}) during the insertion process. */
	protected Trie current = null;
	
	/** The last inserted {@link Trie} node. */
	protected Trie last = null;
	
	public Trie getRoot() {
		return root;
	}
	
	public void setRoot(Trie root) {
		this.root = root;
	}
	
	public Trie getCurrent() {
		return current;
	}
	
	public void setCurrent(Trie current) {
		this.current = current;
	}
	
	/**
	 * Inserts the specified {@link Term} using the flattend term 
	 * representation in the specified {@link TermStack} and starts the 
	 * insertion process at the momentary {@link #current}.
	 *  
	 * @param stack The flattened term representation in a stack.
	 * @param term The term to insert.
	 * @return The insertion {@link Trie} node.
	 */
	/*
	 *  TODO Maybe use the returned insertion node for bulk insertions to speed things up a little...
	 */
	public abstract Trie insert(TermStack stack, Term term);
}
