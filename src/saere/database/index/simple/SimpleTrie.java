package saere.database.index.simple;

import saere.Atom;
import saere.Term;

/**
 * A simple representation of a trie node.
 * 
 * @author David Sullivan
 * @version 0.1, 10/18/2010
 */
public final class SimpleTrie {

	protected Atom label;
	protected SimpleTrie parent;
	protected SimpleTrie firstChild;
	protected SimpleTrie nextSibling;
	protected SimpleTermList termList;
	
	/**
	 * Creates a new root.
	 */
	public SimpleTrie() {
		this.parent = null;
		this.label = null; // XXX Label null or is an 'empty' label better?
		firstChild = nextSibling = null;
	}
	
	protected SimpleTrie(Atom label, SimpleTrie parent) {
		assert label != null && parent != null : "Invalid paramter(s)";
		
		this.parent = parent;
		this.label = label;
		firstChild = nextSibling = null;
	}
	
	protected void addTerm(Term term) {
		SimpleTermList head = new SimpleTermList(term);
		head.next = termList;
		termList = head;
	}
	
	@Override
	public String toString() {
		if (label == null) {
			return "<root>";
		} else {
			return label.toString();
		}
	}
}
