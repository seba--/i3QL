package saere.database.index;

import saere.Atom;
import saere.Term;

/**
 * A simple representation of a trie node.
 * 
 * @author David Sullivan
 * @version 0.8, 10/18/2010
 */
public class Trie<T> {

	/** The label of this trie node. */
	private T label;
	
	/** The parent of this node (<tt>null</tt> iff this is the root). */
	private Trie<T> parent;

	/** The first child of this node. */
	private Trie<T> firstChild;

	/** The next (i.e., <i>right</i>) sibling of this node. */
	private Trie<T> nextSibling;

	/** The term stored at this trie. Maybe null for an inner trie node (or a node that stores more than one term). */
	// Depending on the trie structure, the majority of trie nodes may have a null value.
	// However, the 4/8 bytes of memory enable a more easy insertion.
	// Must be null if a term list exists.
	private Term term;
	
	/**
	 * Creates a new root.
	 */
	public Trie() {
		this.parent = null;
		this.label = null; // XXX Label null or is an 'empty' label better?
	}
	
	/**
	 * Creates a trie node with the specified parent.
	 * 
	 * @param parent The parent of this node.
	 */
	// Should not be called by any t client, only by subclasses.
	protected Trie(Trie<T> parent, T label) {
		this.parent = parent;
		this.label = label;
		firstChild = nextSibling = null;
	}
	
	public final Trie<T> getParent() {
		return parent;
	}

	public final void setParent(Trie<T> parent) {
		this.parent = parent;
	}

	public final Trie<T> getFirstChild() {
		return firstChild;
	}

	public final void setFirstChild(Trie<T> firstChild) {
		this.firstChild = firstChild;
	}

	public final Trie<T> getNextSibling() {
		return nextSibling;
	}

	public final void setNextSibling(Trie<T> nextSibling) {
		this.nextSibling = nextSibling;
	}
	
	public final T getLabel() {
		return label;
	}
	
	public final void setLabel(T label) {
		this.label = label;
	}
	
	public final Term getTerm() {
		return term;
	}
	
	public final void setTerm(Term term) {
		this.term = term;
	}
	
	// For trie nodes that store multiple terms (collision).
	
	// To avoid any kind of casting...
	public TermList getTermList() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Prepends the specified term list (with a term) to the current term list.
	 * 
	 * @param termList The new head term list.
	 */
	// To avoid any kind of casting...
	public void setTermList(TermList termList) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Checks wether this trie node has a term list, i.e., stores more than one 
	 * term (collision).
	 * 
	 * @return <tt>true</tt> if this node stores more than one term.s
	 */
	// To avoid any kind of casting...
	public boolean hasTermList() {
		return false;
	}
	
	@Override
	public String toString() {
		if (label == null)
			return "<root>";
		
		if (label instanceof Atom)
			return label.toString();
		
		if (label instanceof Atom[]) {
			Atom[] atoms = (Atom[]) label;
			String s = "[";
			boolean first = true;
			for (Atom atom : atoms) {
				if (first) {
					first = false;
					s += atom.toString();
				} else {
					s += ", " + atom.toString();
				}
				return s + "]";
			}
		}
		
		return "?";
	}
}
