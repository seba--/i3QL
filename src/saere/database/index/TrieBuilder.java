package saere.database.index;

import java.util.Iterator;

import saere.Atom;
import saere.Term;

/**
 * A trie builder...
 * 
 * @author David Sullivan
 * @version 0.2, 10/18/2010
 * @param <T> The label type ({@link Atom} or an array of {@link Atom}).
 */
public abstract class TrieBuilder<T> {
	
	protected TermFlattener flattener = new ShallowTermFlattener(); // Default
	protected Trie<T> current;
	
	public void setTermFlattener(TermFlattener flattener) {
		assert flattener != null : "Cannot set term flattener to null";
		this.flattener = flattener;
	}
	
	/**
	 * Inserts the specified {@link Term}.
	 * 
	 * @param term The term to insert.
	 * @param start The node where the insertion process starts (the root in most cases).
	 * @return The insertion {@link Trie} node.
	 */
	public abstract Trie<T> insert(Term term, Trie<T> start);
	
	/**
	 * Removes the specified {@link Term} if it can be found.
	 * 
	 * @param term The term to remove.
	 * @param start The node where the removal process starts (the root in most cases).
	 * @return <tt>true</tt> if the term was found and removed.
	 */
	public abstract boolean remove(Term term, Trie<T> start);
	
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
	public abstract Iterator<Term> iterator(Trie<T> start, Term[] query);
	
	// The following methods actually don't require context knowledge like 
	// those above, but fit in here quite well.
	
	/**
	 * Creates a term iterator that starts the iteration process at the 
	 * specified start node. Every term of the (sub-)trie is part of the 
	 * iteration.
	 * 
	 * @param start The start node (usually the root).
	 * @return A term iterator for the (sub-)trie.
	 */
	public final Iterator<Term> iterator(Trie<T> start) {
		return new TrieTermIterator<T>(start);
	}
	
	/**
	 * Creates a node iterator that iterates over all nodes that are part of 
	 * the specified trie.
	 * 
	 * @param start The trie node where the iteration starts.
	 * @return A trie node iterator for the (sub-)trie.
	 */
	public final Iterator<Trie<T>> nodeIterator(Trie<T> start) {
		return new TrieNodeIterator<T>(start);
	}
	
	// Other utility methods...
	
	/**
	 * Replaces the specified trie with the replacement. This method assumes 
	 * that parent and label are already set for the replacement (as this is 
	 * done in the constructor of a {@link Trie}). The original trie is not 
	 * modified but no references should point to it anymore (and it is 
	 * dropped).
	 * 
	 * @param trie Trie trie node to replace.
	 * @param replacement The replacement for the trie node.
	 */
	// TODO Track this with AspectJ...
	protected final void replace(Trie<T> trie, Trie<T> replacement) {
		
		// Transfer stored terms
		if (trie.hasTermList()) {
			replacement.setTermList(trie.getTermList());
		} else {
			replacement.setTerm(trie.getTerm());
		}
		
		// Reforge child references
		replacement.setFirstChild(trie.getFirstChild());
		Trie<T> child = replacement.getFirstChild();
		while (child != null) {
			child.setParent(replacement);
			child = child.getNextSibling();
		}
		
		// Reforge sibling references
		replacement.setNextSibling(trie.getNextSibling());
		Trie<T> predecessor = trie.getParent().getFirstChild();
		if (trie != predecessor) { // No need to do anything if first child
			while (predecessor.getNextSibling() != trie) {
				predecessor = predecessor.getNextSibling();
			}
			predecessor.setNextSibling(replacement);
		}
		
		// The actual replacement...
		trie.getParent().setFirstChild(replacement);
	}
}
