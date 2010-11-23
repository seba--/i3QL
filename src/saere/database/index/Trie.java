package saere.database.index;

import java.util.IdentityHashMap;


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
	
	/**
	 * Creates a new root that can never turn into a hash trie node.
	 * 
	 * @return A new root.
	 */
	public static Trie newRoot() {
		return new Trie(null, null);
	}
	
	/**
	 * Creates a new root that is a hash trie node from the start.
	 * 
	 * @return A new root.
	 */
	public static Trie newHashRoot() {
		return new HashTrie(null, null, null);
	}
	
	/**
	 * Creates a trie node with the specified parent. Only {@link TrieBuilder} 
	 * should use this constructor.
	 * 
	 * @param parent The parent of this node.
	 */
	public Trie(Trie parent, Label label) {
		setParent(parent);
		setLabel(label);
	}
	
	/**
	 * Checks wether this {@link Trie} stores at least one term.
	 * 
	 * @return <tt>true</tt> if this {@link Trie} stores at least one term.
	 */
	public boolean isStorageTrie() {
		return false;
	}
	
	/**
	 * Checks wether this {@link Trie} uses hash maps.
	 * 
	 * @return <tt>true</tt> if this {@link Trie} uses hash maps.
	 */
	public boolean isHashTrie() {
		return false;
	}
	
	public TermList getTerms() {
		//throw new UnsupportedOperationException("This trie node cannot store terms");
		return null;
	}
	
	public void setTerms(TermList terms) {
		throw new UnsupportedOperationException("This trie node cannot store terms: [" + terms.next() + ",...]");
	}
	
	public IdentityHashMap<Label, Trie> getMap() {
		// throw new UnsupportedOperationException("This trie node does not hash");
		return null;
	}
	
	public void setMap(IdentityHashMap<Label, Trie> map) {
		throw new UnsupportedOperationException("This trie node does not hash");
	}
	
	public Trie getParent() {
		return parent;
	}
	
	public void setParent(Trie parent) {
		assert this != parent : "Parent is the same as this";
		this.parent = parent;
	}
	
	public Trie getFirstChild() {
		return firstChild;
	}
	
	public void setFirstChild(Trie firstChild) {
		assert this != firstChild : "First child is the same as this";
		this.firstChild = firstChild;
	}
	
	public Trie getNextSibling() {
		return nextSibling;
	}
	
	public void setNextSibling(Trie nextSibling) {
		assert this != nextSibling : "Next sibling is the same as this";
		this.nextSibling = nextSibling;
	}
	
	public Label getLabel() {
		return label;
	}
	
	public void setLabel(Label label) {
		this.label = label;
	}

	public Trie getLastChild() {
		throw new UnsupportedOperationException("This trie node cannot remember the last child");
	}

	public void setLastChild(Trie lastChild) {
		throw new UnsupportedOperationException("This trie node cannot remember the last child");
	}

	@Override
	public String toString() {
		return hashCode() + ":" + (label == null ? "<root>" : label.toString());
	}
	
	public boolean isMultiTrie() {
		return false;
	}
	
	public Trie getSubtrie() {
		return null;
	}
	
	public void setSubtrie(Trie subtrie) {
		throw new UnsupportedOperationException("This trie node cannot have a subtrie");
	}
}
