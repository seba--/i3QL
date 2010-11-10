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
	 * Creates a trie without a label or parent (e.g., a root).
	 */
	public Trie() { /* empty */ }
	
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
	protected boolean stores() {
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
	 * Adds the specified trie as child to this trie.
	 * 
	 * @param child The trie to add as child.
	 */
	@Deprecated
	protected void addChild(Trie child) {
		childrenNumber++;
		
		// Move to the last added child, this shouldn't take terribly long 
		// as we do this only if we have not many children.
		if (firstChild == null) {
			firstChild = child;
		} else {
			Trie lastChild = firstChild;
			while (lastChild.nextSibling != null) {
				lastChild = lastChild.nextSibling;
			}
			lastChild.nextSibling = child;
		}
		
		if (childrenNumber == TrieBuilder.mapThreshold) {
			throw new Error("I hadn't thougth of that. -Unkown, from Popular Last Words");
		}
	}
	
	/**
	 * Gets the child with the specified label. If none such child exists 
	 * <tt>null</tt> is returned.
	 * 
	 * @param label The label of the child.
	 * @return The child with the label or <tt>null</tt>.
	 */
	@Deprecated
	protected Trie getChild(Label label) {
		Trie child = firstChild;
		while (child != null) {
			if (child.label.sameAs(label)) {
				return child;
			}
		}
		return null;
	}
	
	/**
	 * Add the specified term to this {@link Trie}.
	 * 
	 * @param term The term to add.
	 */
	protected void addTerm(Term term) {
		throw new UnsupportedOperationException("This trie node cannot store terms");
	}
	
	protected TermList getTerms() {
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
	
	protected Trie getFirstChild() {
		return firstChild;
	}
	
	protected void setFirstChild(Trie firstChild) {
		this.firstChild = firstChild;
	}
	
	protected Trie getNextSibling() {
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
		return "Trie-" + this.hashCode() + "/" + (label == null ? "<root>" : label.toString());
	}
}
