package saere.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import saere.StringAtom;
import saere.Term;
import saere.Variable;

/*
 * TODO Remove code overlaps with inheritance...
 */
public class Trie {

	private final Term label; // will be an atom or a variable
	private final Trie parent; // required for trie iterator
	private Term term;
	private Trie firstChild;
	private Trie nextSibling;

	/**
	 * Creates a root trie (label is <tt>null</tt>, parent is <tt>null</tt>).
	 * 
	 * @param label
	 */
	public Trie() {	
		this(null, null);
	}
	
	private Trie(Term label, Trie parent) {
		assert label != null && (label.isIntegerAtom() || label.isStringAtom()) : "Invalid label";
		
		this.label = label;
		this.parent = parent;
		term = null;
		firstChild = nextSibling = null;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public Term getLabel() {
		return label;
	}

	public Term getTerm() {
		return term;
	}
	
	public Trie getParent() {
		return parent;
	}

	public Trie getFirstChild() {
		return firstChild;
	}

	public Trie getNextSibling() {
		return nextSibling;
	}

	public Trie add(Term term) {
		assert isRoot() : "Can add to root only";
		
		return add(new TermStack(breakDown(term).toArray(new Term[0])), term);
	}

	private Trie add(TermStack ts, Term t) {
		Term first = ts.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (isRoot()) {
			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(first, this);
			}
			return firstChild.add(ts, t);
		} else if (same(first, label)) {
			// the labels match
			ts.pop();

			// anything left to process?
			if (ts.size() == 0) {
				term = t;
				return this;
			}

			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(ts.peek(), this);
			}
			return firstChild.add(ts, t);
		} else {
			// add to sibling subtrie
			if (nextSibling == null) {
				nextSibling = new Trie(first, parent);
			}
			return nextSibling.add(ts, t);
		}
	}

	// or unify(Term ... term)
	/**
	 * This method should get relevant subtries wie "queries", e.g. query(m_1, new Variable(), return)...
	 */
	// store queries and answer to queries?
	// FIXME Rather than store them, return results immediately
	public List<Term> query(Term... terms) {
		//assert root : "Can query root only";

		List<Term> result = new LinkedList<Term>();
		Trie startTrie = firstChild;
		
		if (startTrie != null) {
			List<Term> dq = new LinkedList<Term>();
			for (Term term : terms) {
				dq.addAll(breakDown(term)); // break down to atoms
			}
			startTrie.query(new TermStack(dq.toArray(new Term[0])), result);
			return result;
		} else {
			return result; // nothing can be found
		}
	}
	
	private void query(TermStack ts, List<Term> result) {
		assert ts.size() > 0 : "Term stack size is 0";
		
		Term first = ts.peek();
		if (same(first, label) || isFreeVar(first)) {
			
			// the last element matched...
			if (ts.size() == 1) {
				if (term != null) {
					// add this term to result...
					result.add(term);
				} else {
					// or add all terms of this trie to result
					collectLeafTerms(this, result);
				}
				// nothing more to process
				return;
			}
			
			// search in subtrie...
			if (firstChild != null) {
				
				// the original term stack may be needed if first is a variable
				TermStack tsMinusOne;
				if (first.isVariable()) {
					tsMinusOne = ts.copy();
				} else {
					tsMinusOne = ts;
				}
				tsMinusOne.pop();
				firstChild.query(tsMinusOne, result);
			}
			
			// and (start) search also in sibling tries if variable
			if (first.isVariable() && nextSibling != null) {
				nextSibling.query(ts.copy(), result);
			}
			
		} else if (nextSibling != null) {
			nextSibling.query(ts, result);
		}
	}

	public List<Term> getAllTerms() {
		assert isRoot() : "Only call to root allowed";
		
		List<Term> result = new ArrayList<Term>();
		collectLeafTerms(this, result);
		return result;
	}
	
	// breaks a term down to atoms and variables
	// TODO Maybe a way without lists
	private List<Term> breakDown(Term term) {
		assert term != null : "Term is null";
		
		List<Term> terms = new LinkedList<Term>();
		
		// add functor as first term (or self if variable)
		if (term.isVariable()) {
			terms.add(term);
		} else {
			terms.add(term.functor());
		}

		// add other terms (recursively)
		if (term.isCompoundTerm()) {
			for (int i = 0; i < term.arity(); i++) {
				terms.addAll(breakDown(term.arg(i)));
			}
		}

		return terms;
	}
	
	/**
	 * Should yield the same results as {@link Term#unify(Term)} but <b>without 
	 * unification</b> and <b>only if the specified terms are either atoms or 
	 * variables</b>.
	 *  
	 * @param t0 The first term.
	 * @param t1 The second term.
	 * @return <tt>true</tt> if so.
	 */
	private boolean same(Term t0, Term t1) {
		if (t0 == null) {
			return t1 == null; // null == null
		} else if (t1 == null) {
			return false; // a1 != null
		} else if (t0.isStringAtom() && t1.isStringAtom()) {
			return t0.asStringAtom().sameAs(t1.asStringAtom());
		} else if (t0.isIntegerAtom() && t1.isIntegerAtom()) {
			return t0.asIntegerAtom().sameAs(t1.asIntegerAtom());
		} else if (t0.isVariable() && t1.isVariable()) {
			// variables...
			Variable v0 = t0.asVariable();
			Variable v1 = t1.asVariable();
			if (!v0.isInstantiated() && !v1.isInstantiated()) {
				// for this purpose two variables are the same, if they are not instantiated (XXX ?)
				return true;
			} else if (v0.isInstantiated() && v1.isInstantiated()) {
				return same(v0.binding(), v1.binding()); // loop of doom with cyclic bindinds?
			}
		}
		
		return false;
	}
	
	private boolean isFreeVar(Term term) {
		return term.isVariable() && !term.asVariable().isInstantiated();
	}

	private void collectLeafTerms(Trie trie, List<Term> result) {
		if (trie.term != null) {
			result.add(trie.term);
		} else {
			Trie child = trie.firstChild;
			while (child != null) {
				collectLeafTerms(child, result);
				child = child.nextSibling;
			}
		}
	}
	
	@Override
	public String toString() {
		String tStr = term != null ? Utils.termToString(term) : "null";
		String lStr = label != null ? label.toString() : "null";
		String pStr = parent != null && parent.label != null ? parent.label.toString() : "null";
		String fStr = firstChild != null && firstChild.label != null ? firstChild.label.toString() : "null";
		String nStr = nextSibling != null && nextSibling.label != null ? nextSibling.label.toString() : "null";
		return "[Trie " + lStr + ", parent=" + pStr + ", firstChild=" + fStr + ", nextSibling=" + nStr + ", term=" + tStr + "]";
	}
	
	public Trie getPredicateSubtrie(StringAtom functor) {
		assert isRoot() : "Can get predicate subtries only from root";

		// precidate functors are direct root children
		Trie child = firstChild;
		while (child != null) {
			if (same(child.label, functor)) {
				break;
			}
			child = child.nextSibling;
		}
		
		assert child != null : "No predicate for specified functor";
		return child;
	}
	
	/**
	 * @see TrieIterator#TrieIterator(Trie, Term...)
	 */
	public Iterator<Term> iterator(Term ... terms) {
		return new TrieIterator(this, terms);
	}
	
	public Iterator<Term> iterator() {
		return new SimpleTrieIterator(this);
	}
	
	private class TrieIterator implements Iterator<Term> {
		
		private Trie start; // start is our (temporary) root
		private Trie currTrie;
		private int currLevel;
		private Term next;
		private TermStack stack;
		private Iterator<Term> subiterator;
		
		/**
		 * Creates a new trie iterator that starts from <tt>start</tt> and 
		 * returns only terms that match the term represented by <tt>terms</tt>.
		 * 
		 * @param start The start trie, e.g., a functor.
		 * @param terms A query represented by an array of terms.
		 */
		public TrieIterator(Trie start, Term ... terms) {
			
			// break down terms to atoms/variables
			List<Term> list = new LinkedList<Term>();
			list.add(label);
			for (Term term : terms)
				list.addAll(breakDown(term));
			stack = new TermStack(list.toArray(new Term[0]));
			
			this.start = start;
			currTrie = start;
			currLevel = 0; // the start level is the 'relative' level 0
			next = null;
			findNext(); // find first next
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}
		
		@Override
		public Term next() {
			if (hasNext()) {
				Term ret = next;
				findNext();
				return ret;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		/**
		 * Finds the next term, i.e., sets {@link TrieIterator#next}.
		 */
		private void findNext() {
			next = null;
			
			// as long as we haven't found a new next and are not at an end point, i.e., current is null
			while (next == null && currTrie != null) { // (or break)
				
				boolean subiter = false;
				Term first = stack.peek();
				if (stack.size() == 0 || (stack.size() == 1 && match(first))) {
					subiter = true;
				}
				
				// subiteration mode
				if (/*stack.size() == 0*/subiter) {
					if (subiterator == null) {
						subiterator = new SimpleTrieIterator(currTrie);
					}
					
					if (subiterator.hasNext()) {
						next = subiterator.next();
						break;
					} else {
						subiterator = null;
						goRight();
					}
				} else { // normal mode (non-subiteration mode)
					// get next atom/variable
					/*Term*/ first = stack.peek();
					
					if (match(first)) {
						
						// try to "return" the current trie's term (and advance!)
						if (currTrie.term != null) {
							next = currTrie.term;
							nextNode();
							break; // break while ("return")
						}
						
						nextNode();
					} else {
						
						 // no match, go right
						goRight();
					}
				}
			}
		}
		
		private Trie nextNode() {
			if (currTrie.firstChild != null) { // if we can go deeper, we do this
				goDown();
			} else {
				goRight();
			}
			return currTrie;
		}
		
		/**
		 * Goes to the current node's child.
		 */
		private void goDown() {
			currTrie = currTrie.firstChild;
			currLevel++;
			stack.pop();
		}
		
		private void goUp() {
			currTrie = currTrie.parent;
			currLevel--;
			stack.back();
		}
		
		private void goRight() {
			while (currTrie.nextSibling == null && currTrie != start) {
				goUp();
			}
			
			// treat start as root (a root has no siblings)
			if (currTrie != start) { 
				currTrie = currTrie.nextSibling;
			} else {
				currTrie = null; // to terminate
			}
		}
		
		private boolean match(Term term) {
			return isFreeVar(term) || same(currTrie.label, term);
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}	
	}
	
	/**
	 * A simple trie iterator that iterates over all terms in a (sub-)trie. The 
	 * iteration is done is a lazy way. That is, a new element is only 
	 * searched for after a call to {@link Iterator#next()}. (Actually one step
	 * ahead.)
	 * 
	 * @author David Sullivan
	 * @version $Id$
	 */
	private class SimpleTrieIterator implements Iterator<Term> {
		Trie start; // start is our root
		Trie current; // saves the current state, i.e., the current node in the iterated trie
		Term next; // the next term, is set by findNext()
		
		/**
		 * Creates a new simple trie iterater that treats the specified 
		 * <tt>start</tt> as root.
		 * 
		 * @param start The start, i.e., the root for the iteration.
		 */
		private SimpleTrieIterator(Trie start) {
			this.start = start;
			current = start;
			next = null;
			findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Term next() {
			if (hasNext()) {
				Term ret = next;
				findNext(); // set the new next (may be null at the end) ...
				return ret; // but return the old next
			} else {
				throw new NoSuchElementException();
			}
		}
		
		private void findNext() {
			next = null;
			Trie node = nextNode();
			
			// we arrived at the start/root again --> terminate
			if (node == start) {
				return;
			}
			
			// the usual case
			while (node.term == null) {
				
				node = nextNode();
				//System.out.println("node = " + node);
				
				// we arrived at the start/root again --> terminate
				if (node == start) {
					return;
				}
			}
			next = node.term;
		}
		
		/**
		 * Returns the 'next' node of the trie (regardless if this node stores a
		 * term or not). A trie is iterated depth-first and from the left to 
		 * the right.
		 * 
		 * @return The next trie node.
		 */
		private Trie nextNode() {
			if (current.firstChild != null) { // if we can go deeper, we do this
				goDown();
			} else {
				goRight();
			}
			return current;
		}
		
		/**
		 * Moves the current position one level deeper into the trie. (That is, 
		 * from the root to the leafs direction the tree gets 'deeper'.)
		 */
		private void goDown() {
			current = current.firstChild;
		}
		
		/**
		 * Goes to the parent.
		 */
		private void goUp() {
			current = current.parent;
		}
		
		/**
		 * Moves the current position to the next right trie node. Depending on 
		 * the current position this could be the direct right sibling or the 
		 * root of the subtrie to the right (if the current position has no 
		 * right sibling).
		 */
		private void goRight() {			
			while (current != start && current.nextSibling == null) {
				goUp();
			}
			
			if (current != start) // treat start as root (and a root has no siblings)
				current = current.nextSibling;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
