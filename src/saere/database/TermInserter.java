package saere.database;

import saere.Term;

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
	
	public abstract Trie insert(TermStack ts, Term t);
}
