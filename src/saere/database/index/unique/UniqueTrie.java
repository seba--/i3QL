package saere.database.index.unique;

import java.util.IdentityHashMap;

import saere.Atom;
import saere.Term;
import saere.database.index.Matcher;

/**
 * A very simple representation of a trie node that can store its children in a
 * hash map.
 * 
 * @author David Sullivan
 * @version 0.1, 10/26/2010
 */
public final class UniqueTrie {

	/** Threshold for maximum children size before a hash map is used. */
	private static int mapThreshold = 10;
	
	protected Atom label;
	protected UniqueTrie parent;
	protected UniqueTrie firstChild;
	protected UniqueTrie nextSibling;
	protected Term term;
	
	// Map related fields
	protected IdentityHashMap<Atom, UniqueTrie> childrenMap; // null if not used
	protected int childrenNumber; // the number of children this trie has
	protected UniqueTrie lastChild; // to speed up insertions with many children (=long lists)
	protected int siblingIndex; // the position of this trie in the list of its parent's children
	
	/**
	 * Creates a new root.
	 */
	public UniqueTrie() {
		this.parent = null;
		this.label = null;
		firstChild = nextSibling = null;
		
		childrenMap = null;
		childrenNumber = 0;
		siblingIndex = 0;
	}
	
	protected UniqueTrie(Atom label, UniqueTrie parent) {
		assert label != null && parent != null : "Invalid paramter(s)";
		
		this.parent = parent;
		this.label = label;
		firstChild = nextSibling = null;
		
		childrenMap = null;
		childrenNumber = 0;
		
		this.parent.addChild(this);
	}
	
	protected void addTerm(Term term) {
		// Collision should not happen with the UniqueTermFlattener
		assert term == null : "Cannot add another term to a trie";
		this.term = term;
	}
	
	@Override
	public String toString() {
		if (label == null) {
			return "<root>"; // As only the root shoult habe no label!
		} else {
			return label.toString();
		}
	}
	
	/**
	 * Adds the specified trie as child to this trie. A child is always added
	 * to the list of children (via the {@link #nextSibling} reference) but it 
	 * can be added to a hash map if the number of children of its new parent 
	 * exceeds a threshold.
	 * 
	 * @param child The child to add.
	 */
	public void addChild(UniqueTrie child) {
		child.siblingIndex = childrenNumber;
		childrenNumber++;
		if (firstChild == null) {
			firstChild = child;
		} else {
			lastChild.nextSibling = child;
		}	
		lastChild = child;
		
		// Add  to hash map if one is used
		if (childrenMap != null) {
			childrenMap.put(child.label, child);
		} else if (childrenNumber > mapThreshold) {
			// Create hash map
			childrenMap = new IdentityHashMap<Atom, UniqueTrie>(); // 21?
			UniqueTrie trie = firstChild;
			while (trie != null) {
				childrenMap.put(trie.label, trie);
				trie = trie.nextSibling;
			}
			childrenMap.put(child.label, child);
		}
	}
	
	/**
	 * Returns the child with the specified label or null if none with the 
	 * specified label exists.
	 * 
	 * @param label The label of the child.
	 * @return The child with the label.
	 */
	public UniqueTrie getChild(Atom label) {
		if (childrenMap != null) {
			return childrenMap.get(label);
		} else {
			UniqueTrie child = firstChild;
			while (child != null) {
				if (Matcher.match(child.label, label)) {
					return child;
				}
				child = child.nextSibling;
			}
			return null;
		}
	}
}