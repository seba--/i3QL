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
	private TermList termList;
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
		termList = null;
		firstChild = nextSibling = null;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public Term getLabel() {
		return label;
	}

	/**
	 * Gets the <b>first</b> term of this {@link Trie} node.
	 * 
	 * @return The first term.
	 */
	public Term getTerm() {
		if (termList != null) {
			return termList.getTerm();
		} else {
			return null;
		}	
	}
	
	public TermList getTerms() {
		return termList;
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
				termList = new TermList(t);
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
				if (termList != null) {
					// add this term to result...
					result.add(getTerm());
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
		if (trie.termList != null) {
			result.add(trie.getTerm());
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
		String tStr = termList != null ? Utils.termToString(getTerm()) : "null";
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
	 * @see TrieTermIterator#TrieIterator(Trie, Term...)
	 */
	public Iterator<Term> iterator(Term ... terms) {
		return new TrieTermIterator(this, terms);
	}
	
	public Iterator<Term> iterator() {
		return new SimpleTrieTermIterator(this);
	}
	
	public Iterator<Trie> nodeIterator() {
		return new TrieNodeIterator(this);
	}
	
	private class TrieTermIterator extends SimpleTrieTermIterator {
		
		private TermStack stack;
		private Iterator<Term> subiterator;
		
		/**
		 * Creates a new trie iterator that starts from <tt>start</tt> and 
		 * returns only terms that match the term represented by <tt>terms</tt>.
		 * 
		 * @param start The start trie, e.g., a functor.
		 * @param terms A query represented by an array of terms.
		 */
		public TrieTermIterator(Trie start, Term ... terms) {
			super();
			this.start = start;
			current = start;
			
			// break down terms to atoms/variables
			List<Term> list = new LinkedList<Term>();
			list.add(label);
			for (Term term : terms)
				list.addAll(breakDown(term));
			stack = new TermStack(list.toArray(new Term[0]));
			
			// find first next
			findNext(); 
		}
		
		@Override
		protected void findNext() {
			next = null;
			
			// list processing mode
			if (list != null) {
				next = list.getTerm();
				list = list.getNext();
			} else { // normal mode
				
				// as long as we haven't found a new next and are not at an end point, i.e., current is null
				while (next == null && current != null) { // (or break)
					
					boolean subiter = false;
					Term first = stack.peek(); // get next atom/variable
					if (stack.size() == 0 || (stack.size() == 1 && match(first))) {
						subiter = true;
					}
					
					// subiteration mode
					if (subiter) {
						if (subiterator == null) {
							subiterator = new SimpleTrieTermIterator(current);
						}
						
						if (subiterator.hasNext()) {
							next = subiterator.next();
							break; // = return
						} else {
							subiterator = null;
							goRight();
						}
					} else { // normal mode (non-subiteration mode)
						if (match(first)) {
							// try to "return" the current trie's term list
							if (current.termList != null) {
								list = current.termList;
								next = list.getTerm();
								list = list.getNext();
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
		}
		
		@Override
		protected void goUp() {
			super.goUp();
			stack.back();
		}
		
		@Override
		protected void goDown() {
			super.goDown();
			stack.pop();
		}
		
		protected boolean match(Term term) {
			return isFreeVar(term) || same(current.label, term);
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
	private class SimpleTrieTermIterator extends TrieIteratorBase implements Iterator<Term> {
		
		/** The term list of the current position. */
		protected TermList list;
		
		/** The next term. */
		protected Term next;
		
		/**
		 * Creates a new simple trie iterator.
		 * @see TrieIteratorBase#BaseTrieIterator()
		 */
		protected SimpleTrieTermIterator() {
			super();
		}
		
		/**
		 * Creates a new simple trie iterater that treats the specified 
		 * <tt>start</tt> as root. Also, the first <tt>next</tt> is found.
		 * 
		 * @param start The start, i.e., the root for the iteration.
		 */
		protected SimpleTrieTermIterator(Trie start) {
			super(start);
			list = null;
			findNext();
		}

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Term next() {
			if (hasNext()) {
				Term oldNext = next;
				findNext();
				return oldNext;
			} else {
				throw new NoSuchElementException();
			}
		}
		
		@Override
		protected void findNext() {
			next = null;
			
			// list processing mode
			if (list != null) {
				next = list.getTerm();
				list = list.getNext();
			} else { // normal mode
				
				// as long as we have nodes left and have no term list
				while (current != null && list == null) {
					nextNode();
					if (current != null) {
						list = current.termList;
					} else { // current == null
						
						// terminate
						next = null; 
						return;
					}
				}
				
				list = current.getTerms();
				next = list.getTerm();
				list = list.getNext();	
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private class TrieNodeIterator extends TrieIteratorBase implements Iterator<Trie> {

		/**
		 * The next {@link Trie} node. It is set by {@link TrieNodeIterator#findNext()} (only).
		 */
		private Trie next;
		
		/**
		 * Creates a new {@link TrieNodeIterator} and finds the first <tt>next</tt>.
		 * 
		 * @param start The start node for the iteration.
		 */
		protected TrieNodeIterator(Trie start) {
			super(start);
			findNext(); // find first next
		}
		
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Trie next() {
			if (hasNext()) {
				Trie oldNext = next;
				findNext();
				return oldNext;
			} else {
				throw new NoSuchElementException();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();	
		}
		
		@Override
		protected void findNext() {
			nextNode();
			next = current;
		}
	}
	
	private abstract class TrieIteratorBase {
		
		/**
		 * The node from where the iteration begins. From the point of view of the
		 * iterator it works as root. Only the <tt>start</tt> node and its descendants
		 * are considered to be iterated.
		 */
		protected Trie start;
		
		/**
		 * The current position.
		 */
		protected Trie current;
		
		/**
		 * Creates a new {@link TrieIteratorBase} and <b>does not</b> set the <tt>current</tt> 
		 * and <tt>start</tt> fields.
		 */
		protected TrieIteratorBase() {
			start = current = null;
		}
		
		/**
		 * Creates a new {@link TrieIteratorBase} and sets the <tt>current</tt> 
		 * and <tt>start</tt> fields.
		 * 
		 * @param start The start node for the iteration.
		 */
		protected TrieIteratorBase(Trie start) {
			this.start = start;
			current = start;
		}
		
		/**
		 * Finds the next <tt>next</tt>.
		 */
		protected abstract void findNext();
		
		/**
		 * Moves the current position to the next node. The next node is determined 
		 * by a depth-first search from the left to the right. If no node is left, 
		 * <tt>null</tt> is returned.
		 */
		protected void nextNode() {
			assert current != null && start != null : "current or start is/are null";
			
			if (current.firstChild != null) {
				goDown();
			} else {
				goRight();
			}
		}
		
		/**
		 * Moves the current position to the right at the first occuring chance. 
		 * This may be the next (right) sibling or the next subtrie, for example.
		 */
		protected void goRight() {
			assert current != null && start != null : "current or start is/are null";
			
			while (current.nextSibling == null && current != start) { // don't go higher up than start
				goUp();
			}
			
			if (current != start) {
				current = current.nextSibling; // go (directly) right
			} else { // current == start
				current = null; // we treat start as root (and a root has no siblings), me march into the void
			}
		}
		
		/**
		 * Moves the current position one step down, i.e., to its first child.
		 */
		protected void goDown() {
			current = current.firstChild;
		}
		
		/**
		 * Moves the current position one step up, i.e., to its parent.
		 */
		protected void goUp() {
			current = current.parent;
		}
	}
}
