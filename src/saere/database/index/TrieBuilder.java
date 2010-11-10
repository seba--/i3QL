package saere.database.index;

import java.util.Iterator;

import saere.Term;

/**
 * A trie builder...
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 */
public abstract class TrieBuilder {
	
	/** Threshold for using hash maps. */
	protected final int mapThreshold;
	
	protected final TermFlattener flattener;
	
	protected LabelStack stack;
	protected Trie lastChild;
	protected Trie current;
	
	public TrieBuilder(TermFlattener flattener, int mapThreshold) {
		this.flattener = flattener;
		this.mapThreshold = mapThreshold;
	}
	
	/**
	 * Inserts the specified {@link Term}.
	 * 
	 * @param term The term to insert.
	 * @param start The node where the insertion process starts (the root in most cases).
	 * @return The insertion {@link Trie} node.
	 */
	public abstract Trie insert(Term term, Trie start);
	
	/**
	 * Removes the specified {@link Term} if it can be found.
	 * 
	 * @param term The term to remove.
	 * @param start The node where the removal process starts (the root in most cases).
	 * @return <tt>true</tt> if the term was found and removed.
	 */
	public abstract boolean remove(Term term, Trie start);
	
	/**
	 * Creates a term iterator that starts the iteration process at the 
	 * specified start node and with regard to the specified query (as 
	 * {@link Term} array). Only terms that satisfy the query are returned.<br> 
	 * <br> 
	 * Note that this does <b>not</b> mean that the original term behind the 
	 * query must unify with all returned results. It may unify with some or 
	 * all terms and all terms in the trie that will unify with the original 
	 * term are part of the iteration.
	 * 
	 * @param start The start node (usually the root)
	 * @param query The query, whereas the first element at index 0 is the functor, the second element is the first argument and so on.
	 * @return An iterator for the query.
	 */
	public abstract Iterator<Term> iterator(Trie start, Term query);
	
	/**
	 * Creates a term iterator that starts the iteration process at the 
	 * specified start node. Every term of the (sub-)trie is part of the 
	 * iteration.
	 * 
	 * @param start The start node (usually the root).
	 * @return A term iterator for the (sub-)trie.
	 */
	public final Iterator<Term> iterator(Trie start) {
		return new TermIterator(start);
	}
	
	/**
	 * Creates a node iterator that iterates over all nodes that are part of 
	 * the specified trie.
	 * 
	 * @param start The trie node where the iteration starts.
	 * @return A trie node iterator for the (sub-)trie.
	 */
	public final Iterator<Trie> nodeIterator(Trie start) {
		return new NodeIterator(start);
	}
	
	/**
	 * Replaces the first specified trie with the second by copying all 
	 * references of the first to the second and by setting references to the 
	 * first to the second.<br>
	 * <br>
	 * <b>Does not copy any stored terms or hash maps.</b>
	 * 
	 * @param trie The trie to replace.
	 * @param replacement The replacement.
	 */
	// TODO Track with AspectJ how often does this occur!
	// Replacement is ofcourse very expensive for hash tries. (Worst case: A hash trie turns into a storage trie.)
	protected static void replace(Trie trie, Trie replacement) {	
		
		// First, simply copy all references OF the old trie
		//replacement.setLabel(trie.getLabel());
		//replacement.setParent(trie.getParent());
		replacement.setFirstChild(trie.getFirstChild());
		replacement.setNextSibling(trie.getNextSibling());
		replacement.setChildrenNumber(trie.getChildrenNumber());
		
		// Now, replace all references TO the old trie
		Trie child = replacement.getFirstChild();
		while (child != null) {
			// Set replacement as new parent for all children
			child.setParent(replacement);
			child = child.getNextSibling();
		}
		if (replacement.getParent() != null) {
			// Set the first child / next sibling relation to the replacement
			if (replacement.getParent().getFirstChild() == trie) {
				// Replacement is first child
				replacement.getParent().setFirstChild(replacement);
			} else {
				// Replacement is a(ny) sibling, iterate through all children (maybe expensive)
				Trie sibling = replacement.getParent().getFirstChild();
				while (sibling != null && sibling.getNextSibling() != trie) {
					sibling = sibling.getNextSibling();
				}
				if (sibling != null) {
					sibling.setNextSibling(replacement);
				}
			}
			
			// Replace in parent's hash map if necessary
			if (replacement.getParent().hashes()) {
				replacement.getParent().getMap().put(replacement.getLabel(), replacement);
			}
		}
	}
	
	/**
	 * Gets the child with the specified label. As a side-effect the 
	 * {@link #lastChild} is updated which enables fast appending of a new 
	 * child/sibling even if the parent doesn't uses hash maps (and thus has no
	 * field for its last child).
	 * 
	 * @param parent The parent trie.
	 * @param label The label of the child.
	 * @return The child or <tt>null</tt>.
	 */
	protected Trie getChild(Trie parent, Label label) {
		if (parent.hashes()) {
			lastChild = parent.getLastChild();
			return parent.getMap().get(label);
		} else {
			Trie child = parent.getFirstChild();
			while (child != null) {
				if (child.getLabel().sameAs(label)) {
					return child;
				} else {
					lastChild = child;
					child = child.getNextSibling();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Adds the child to parent. Tries to use the {@link #lastChild} field to 
	 * enable fast adding even for parents without a hash map (and no field for 
	 * last child).
	 * 
	 * @param parent The parent trie.
	 * @param child The child to add.
	 */
	protected void addChild(Trie parent, Trie child) {
		assert lastChild != null : "The last child field is not set";
		
		parent.setChildrenNumber(parent.getChildrenNumber() + 1);
		
		if (parent.hashes()) {
			parent.getLastChild().setNextSibling(child);
			parent.setLastChild(child);
			parent.getMap().put(child.getLabel(), child);
		} else {
			if (lastChild != null) {
				// This is the planned case
				lastChild.setNextSibling(child);
			} else {
				// Actually an error (see assertion above)
				lastChild = parent.getFirstChild();
				if (lastChild != null) {
					while (lastChild != null && lastChild.getNextSibling() != null) {
						lastChild = lastChild.getNextSibling();
					}
					lastChild.setNextSibling(child);
				} else {
					parent.setFirstChild(child);
				}
			}
			
			if (parent.getChildrenNumber() == mapThreshold) {
				HashTrie hashTrie = new HashTrie(parent, current.getLabel(), lastChild);
				replace(current, hashTrie);
			}
		}
		
		lastChild = null;
	}
}
