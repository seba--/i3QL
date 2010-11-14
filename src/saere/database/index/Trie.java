package saere.database.index;

import java.util.IdentityHashMap;

import saere.Term;


/**
 * A simple representation of a trie node. (One that cannot store any terms or 
 * use a hash map for many children.)
 * 
 * @author David Sullivan
 * @version 0.9, 11/9/2010
 */
public class Trie {
	
	/** The label of this trie node. */
	private Label label;
	
	/** The parent of this node (<tt>null</tt> iff this is the root). */
	private Trie parent;

	/** The first child of this node. */
	private Trie firstChild;

	/** The next (i.e., <i>right</i>) sibling of this node. */
	private Trie nextSibling;
	
	/** The number of children this trie node has. */
	private int childrenNumber;
	
	/**
	 * Creates a new root. The root must be a hash trie from the beginning. 
	 * Otherwise, if it is replaced later when it has enough children for a 
	 * hash map, references outside the trie structure (i.e., clients) point 
	 * to an old root.
	 */
	public static Trie root() {
		return new HashTrie(null, null, null);
	}
	
	/**
	 * Creates a trie node with the specified parent. Only {@link TrieBuilder} 
	 * should use this constructor.
	 * 
	 * @param parent The parent of this node.
	 */
	protected Trie(Trie parent, Label label) {
		this.parent = parent;
		this.label = label;
	}
	
	/**
	 * Checks wether this {@link Trie} stores at least one term.
	 * 
	 * @return <tt>true</tt> if this {@link Trie} stores at least one term.
	 */
	public boolean stores() {
		return false;
	}
	
	/**
	 * Checks wether this {@link Trie} uses hash maps.
	 * 
	 * @return <tt>true</tt> if this {@link Trie} uses hash maps.
	 */
	protected boolean hashes() {
		return false;
	}
	
	/**
	 * Add the specified term to this {@link Trie}.
	 * 
	 * @param term The term to add.
	 */
	protected void addTerm(Term term) {
		throw new UnsupportedOperationException("This trie node cannot store terms");
	}
	
	public TermList getTerms() {
		throw new UnsupportedOperationException("This trie node cannot store terms");
	}
	
	protected void setTerms(TermList terms) {
		throw new UnsupportedOperationException("This trie node cannot store terms");
	}
	
	protected IdentityHashMap<Label, Trie> getMap() {
		throw new UnsupportedOperationException("This trie node does not hash");
	}
	
	protected void setMap(IdentityHashMap<Label, Trie> map) {
		throw new UnsupportedOperationException("This trie node does not hash");
	}
	
	protected Trie getParent() {
		return parent;
	}
	
	protected void setParent(Trie parent) {
		this.parent = parent;
	}
	
	public Trie getFirstChild() {
		return firstChild;
	}
	
	protected void setFirstChild(Trie firstChild) {
		this.firstChild = firstChild;
	}
	
	public Trie getNextSibling() {
		return nextSibling;
	}
	
	protected void setNextSibling(Trie nextSibling) {
		this.nextSibling = nextSibling;
	}
	
	protected Label getLabel() {
		return label;
	}
	
	protected void setLabel(Label label) {
		this.label = label;
	}
	
	protected int getChildrenNumber() {
		return childrenNumber;
	}
	
	protected void setChildrenNumber(int childrenNumber) {
		this.childrenNumber = childrenNumber;
	}

	protected Trie getLastChild() {
		throw new UnsupportedOperationException("This trie node cannot remember the last child");
	}

	protected void setLastChild(Trie lastChild) {
		throw new UnsupportedOperationException("This trie node cannot remember the last child");
	}

	@Override
	public String toString() {
		return hashCode() + ":" + (label == null ? "<root>" : label.toString());
	}
}
