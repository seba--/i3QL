package saere.database.index;

import static saere.database.Utils.isGround;

import java.util.IdentityHashMap;
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
	// Must be > 1, using hash tries for all nodes is not supported.
	protected final int mapThreshold;
	
	public static int replaceCounter = 0; // XXX Remove
	
	protected final TermFlattener flattener;
	protected final boolean noCollision;
	
	protected LabelStack stack;
	protected Trie current;
	
	// To remember...
	protected Trie lastChild;
	protected int childrenNumber;
	
	public TrieBuilder(TermFlattener flattener, int mapThreshold) {
		this.flattener = flattener;
		this.mapThreshold = mapThreshold;
		
		if (flattener instanceof FullFlattener) {
			noCollision = true;
		} else {
			noCollision = false;
		}
	}
	
	/**
	 * Inserts the specified {@link Term}.
	 * 
	 * @param term The term to insert.
	 * @param root The root node..
	 * @return The insertion {@link Trie} node.
	 */
	public abstract Trie insert(Term term, Trie root);
	
	/**
	 * Removes the specified {@link Term} if it can be found.
	 * 
	 * @param term The term to remove.
	 * @param root The root node.
	 */
	public abstract void remove(Term term, Trie root);
	
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
	public Iterator<Term> iterator(Trie start) {
		return new TermIterator(start);
	}
	
	/**
	 * Creates a node iterator that iterates over all nodes that are part of 
	 * the specified trie.
	 * 
	 * @param start The trie node where the iteration starts.
	 * @return A trie node iterator for the (sub-)trie.
	 */
	public Iterator<Trie> nodeIterator(Trie start) {
		return new NodeIterator(start);
	}
	
	public static Trie getPredicateSubtrie(Trie root, Label label) {
		if (root.isHashNode()) {
			return root.getMap().get(label);
		} else {
			Trie child = root.getFirstChild();
			while (child != null) {
				if (child.getLabel().sameAs(label)) {
					return child;
				} else {
					child = child.getNextSibling();
				}
			}
		}
		
		return null;
	}

	public TermFlattener flattener() {
		return flattener;
	}

	/**
	 * Replaces the first specified trie with the second by copying all 
	 * references of the first to the second and by setting references to the 
	 * first to the second.<br>
	 * <br>
	 * <b>Does not copy any stored terms or cares for hash maps.</b>
	 * 
	 * @param original The trie to replace.
	 * @param replacement The replacement.
	 */
	protected static void replace(Trie original, Trie replacement) {
		assert !original.isRoot() : "Cannot replace root";
		assert !original.isHashNode() : "Cannot replace inner hash node";
		
		replaceCounter++;
		
		// Copy all references OF the old trie to the replacement
		replacement.setLabel(original.getLabel()); // actually not necessary
		replacement.setParent(original.getParent()); // actually not necessary
		replacement.setFirstChild(original.getFirstChild());
		replacement.setNextSibling(original.getNextSibling());
		
		// Switch all references TO the old trie to the replacement
		
		// Set replacement as new parent for all children
		Trie child = original.getFirstChild();
		while (child != null) {
			child.setParent(replacement);
			child = child.getNextSibling();
		}
		
		// Set the first child / next sibling relation to the replacement
		if (original.getParent() != null) {
			Trie origParent = original.getParent();
			
			if (origParent.getFirstChild() == original) {
				// Replacement is first child
				origParent.setFirstChild(replacement);
			} else {
				// Replacement is a(ny) sibling, iterate through all children (maybe expensive)
				Trie sibling = origParent.getFirstChild();
				boolean set = false;
				while (sibling != null) {
					if (sibling.getNextSibling() == original) {
						sibling.setNextSibling(replacement);
						set = true;
						break;
					} else {
						sibling = sibling.getNextSibling();
					}
				}
				assert set : "Unable to replace " + original + " with " + replacement + " as next sibling";
			}
			
			// Care of additional fields if parent is a hash trie
			if (origParent.isHashNode()) {
				
				// Replace in parent's hash map if necessary
				origParent.getMap().put(original.getLabel(), replacement);
				
				// Update parent's last child field if necessary
				if (origParent.getLastChild() == original) {
					origParent.setLastChild(replacement);
				}
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
		childrenNumber = 0;
		
		if (parent.isHashNode()) {
			// Setting children number is not necessary
			lastChild = parent.getLastChild();
			return parent.getMap().get(label);
		} else {
			Trie child = parent.getFirstChild();
			while (child != null) {
				childrenNumber++;
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
	
	protected void removeChild(Trie parent, Trie child) {		
		removeChildFromList(parent, child);
		if (parent.isHashNode()) {
			parent.getMap().remove(child.getLabel());
			
			if (parent.getMap().size() < mapThreshold) {
				replace(parent, new InnerNode(parent.getParent(), parent.getLabel()));
			}
		}
	}
	
	protected static void removeChildFromList(Trie parent, Trie child) {
		if (child == parent.getFirstChild()) {
			child.getParent().setFirstChild(child.getNextSibling());
		} else {
			Trie lastSomeChild = parent.getFirstChild(); 
			Trie someChild = parent.getFirstChild().getNextSibling();
			while (someChild != null) {
				if (child == someChild) {
					lastSomeChild.setNextSibling(child.getNextSibling());
					return;
				} else {
					lastSomeChild = someChild;
					someChild = someChild.getNextSibling();
				}
			}
			
			//assert false : "Unable to remove child " + child + " from parent " + parent;
		}
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
		assert parent.isRoot() || parent.isInnerNode() || parent.isHashNode() : "Cannot add a child to trie type " + parent.getClass().getName();
		
		childrenNumber++;
		
		if (parent.isRoot()) {
			if (parent.getLastChild() != null) {
				parent.getLastChild().setNextSibling(child);
			} else {
				parent.setFirstChild(child);
			}
			parent.setLastChild(child);
			
			// Activate or use hash map if necessary (won't happen in BAT because only 15 different predicates)
			if (childrenNumber == mapThreshold) { // Don't create hash trie node for a root
				IdentityHashMap<Label, Trie> rootMap = new IdentityHashMap<Label, Trie>();
				parent.setMap(rootMap);
				
				// Fill the hash map as replace() doesn't care for this
				Trie trie = parent.getFirstChild();
				while (trie != null) {
					parent.getMap().put(trie.getLabel(), trie);
					trie = trie.getNextSibling();
				}
			} else if (childrenNumber > mapThreshold) {
				parent.getMap().put(child.getLabel(), child);
			}
			
		} else if (parent.isHashNode()) {
			parent.getLastChild().setNextSibling(child);
			parent.setLastChild(child);
			parent.getMap().put(child.getLabel(), child);
		} else {
			
			// Normal inner node...
			if (lastChild != null) {
				// This is the planned case
				lastChild.setNextSibling(child);
				lastChild = child;
			} else {
				assert parent.getFirstChild() == null : "Illegal state";
				
				parent.setFirstChild(child);
				lastChild = child;
			}
			
			// Transform parent to hash node?
			if (childrenNumber == mapThreshold) {
				InnerHashNode hashParent = new InnerHashNode(parent.getParent(), parent.getLabel(), lastChild);
				replace(parent, hashParent);
				
				// Fill the hash map as replace() doesn't care for this
				Trie trie = hashParent.getFirstChild();
				while (trie != null) {
					hashParent.getMap().put(trie.getLabel(), trie);
					trie = trie.getNextSibling();
				}
			}
		}
		
		lastChild = null;
	}
	
	protected void addTerm(Trie trie, Term term) {
		assert trie.isSingleStorageLeaf() || trie.isMultiStorageLeaf() : "Trie is not a storage trie: " + trie;
		assert isGround(term) : "Term is not ground: " + term;
		
		if (noCollision) {
			trie.setTerm(term);
		} else if (trie.getTerms() == null) {
			trie.setTerms(new TermList(term, null));
		} else {
			TermList last = null;
			TermList list = trie.getTerms();
			while (list != null) {
				// Store facts only, i.e., unification changes nothing
				if (list.term().unify(term)) {
					return;
				} else {
					last = list;
					list = list.next();
				}
			}
			last.setNext(new TermList(term, null));
		}
	}
	
	protected void removeTerm(Trie trie, Term term) {
		assert trie.isSingleStorageLeaf() : "Trie is not a storage trie: " + trie;
		assert isGround(term) : "Term is not ground: " + term;
		
		if (noCollision) {
			trie.setTerm(null);
		} else {
			
			// Removal in a shallow trie requires a check the the stored term is really the term we want to remove because of collision
			TermList list = trie.getTerms();
			TermList last = null;
			while (list != null) {
				if (list.term().unify(term)) {
					// The term we want to remove (no need to manage states with facts only)
					if (last == null) {
						// Term was first
						trie.setTerms(list.next());
						return;
					} else {
						// Term was somewhere in list
						last.setNext(list.next());
						return;
					}
				} else {
					last = list;
					list = list.next();
				}
			}
		}
	}
}
