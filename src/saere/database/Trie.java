package saere.database;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import saere.StringAtom;
import saere.Term;
import saere.Variable;
import de.tud.cs.st.prolog.Atom;

/*
 * TODO Check if eager (with temporary list) query/iterator is faster than lazy query/iterator, if not, kick it...
 */
public class Trie {
	
	private static TermFlattener termFlattener = new RecursiveTermFlattener(); // default
	private static boolean instances = false;
	
	/** The parent of this node (<tt>null</tt> iff this is the root). */
	private final Trie parent;
	
	/** The label of this node. Either a {@link Variable} or an {@link Atom}. */
	// XXX 1) We don't handle variables as of now(? -- untested) 2) Need to store variables?
	// XXX Even more: Use Label class now, but we assume atom labels as of now --> slows down everything
	// instead of "boolean same(x,y)" use sth like "int same(x,y)"
	// --> requires already more memory
	private Label label;
	
	/** The head of the list of terms that this node stores. */
	private TermList termList;
	
	/** The first child of this node. */
	private Trie firstChild;
	
	/** The next (i.e., <i>right</i>) sibling of this node. */
	private Trie nextSibling;

	/**
	 * Creates a root {@link Trie} (label is <tt>null</tt>, parent is <tt>null</tt>).
	 */
	public Trie() {
		this(null, null);
		instances = true; // assuming the root is (always) the first node
	}
	
	// public now, as the TermInserter creates these...
	public Trie(Label label, Trie parent) {
		this.label = label;
		this.parent = parent;
		termList = null;
		firstChild = nextSibling = null;
	}

	public boolean isRoot() {
		return parent == null;
	}

	public Label getLabel() {
		return label;
	}
	
	public TermList getTermList() {
		return termList;
	}
	
	public void setTermList(TermList termList) {
		this.termList = termList;
	}
	
	public Trie getParent() {
		return parent;
	}

	public Trie getFirstChild() {
		return firstChild;
	}
	
	// new
	public void setFirstChild(Trie firstChild) {
		this.firstChild = firstChild;
	}

	public Trie getNextSibling() {
		return nextSibling;
	}
	
	// new
	public void setNextSibling(Trie nextSibling) {
		this.nextSibling = nextSibling;
	}

	public Trie insert(Term term) {
		assert isRoot() : "Can add to root only";
		
		return insert(new TermStack(flatten(term)), term);
	}
	
	// the last created node is returned --> maybe use for bulk insertions?
	private Trie insert(TermStack ts, Term t) {
		Term first = ts.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (isRoot()) {
			
			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(Label.makeLabel(first), this);
			}
			return firstChild.insert(ts, t);
			
		} else if (label.match(first)) {
			
			// the labels match
			ts.pop();

			// this must be the insertion node
			if (ts.size() == 0) {
				if (termList == null) { // no list so far?
					termList = new TermList(t); // create new term list
				} else { // add to tail...
					TermList last = termList;
					while (termList.getNext() != null) {
						last = termList;
						termList = termList.getNext();
					}
					last.setNext(new TermList(t));
				}
				
				return this;
			}

			// add to own subtrie
			if (firstChild == null) {
				firstChild = new Trie(Label.makeLabel(ts.peek()), this);
			}
			return firstChild.insert(ts, t);
		} else { // !root && !same
			
			// add to (a) sibling subtrie
			if (nextSibling == null) {
				nextSibling = new Trie(Label.makeLabel(first), parent);
			}
			return nextSibling.insert(ts, t);
		}
	}
	
	private Term[] flatten(Term term) {
		return termFlattener.flatten(term);
	}
	
	private boolean isFreeVar(Term term) {
		return term.isVariable() && !term.asVariable().isInstantiated();
	}
	
	@Override
	public String toString() {
		String tStr = termList != null ? Utils.termToString(getTermList().getTerm()) : "null";
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
			if (child.label.match(functor)) {
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
	
	
	public static void setTermFlattener(TermFlattener termFlattener) {
		assert !instances : "term flattener can only be set before any instances exist";
		Trie.termFlattener = termFlattener;
	}
	
	/**
	 * Trie term iterator that supports queries...
	 * 
	 * @author David Sullivan
	 * @version $Id$
	 */
	private class TrieTermIterator extends SimpleTrieTermIterator {
		
		private TermStack stack;
		private Iterator<Term> subiterator;
		
		/**
		 * Creates a new trie iterator that starts from <tt>start</tt> and 
		 * returns only terms that match the term represented by <tt>terms</tt>.
		 * 
		 * @param start The start trie, e.g., a functor.
		 * @param terms A query represented by an array of terms (atoms/variables).
		 */
		public TrieTermIterator(Trie start, Term ... terms) {
			super();
			this.start = start;
			current = start;
			
			// break down terms to atoms/variables
			List<Term> list = new LinkedList<Term>();
			list.add(label.getLabel(0));
			for (Term term : terms) {
				Term[] termTerms = flatten(term);
				for (Term termTerm : termTerms) {
					list.add(termTerm);
				}
			}
				
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
					if ((stack.size() == 0 || (stack.size() == 1 && match(first))) && true /*current.termList == null*/) {
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
								break; // = return
							}
							
							nextNode();
						} else {
							goRight();  // no match, go right
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
			return isFreeVar(term) || current.label.match(term);
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
		
		/** The next term. It is set by {@link SimpleTrieTermIterator#findNext()} (only). */
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
				
				
				// FIXME Review (like a do-while)
				if (current != null) {
					list = current.termList; // XXX ...!!!
					if (current.termList != null) {
						list = current.termList;
						next = list.getTerm();
						list = list.getNext(); // --> loop of doom
						nextNode();
						return;
					}
				}	
				
				// FIXME Do we actually get here anymore?
				// as long as we have nodes left and have no term list
				while (current != null && list == null) {
					nextNode(); // with this we skip the first! 
					if (current != null) {
						if (current.termList != null) {
							list = current.termList;
							next = list.getTerm();
							list = list.getNext();
							break;
						}
					} else { // current == null
						break; // terminate
					}
				}				
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
			assert current != null : "current is null";
			current = current.firstChild;
		}
		
		/**
		 * Moves the current position one step up, i.e., to its parent.
		 */
		protected void goUp() {
			assert current != null : "current is null";
			current = current.parent;
		}
	}
}
