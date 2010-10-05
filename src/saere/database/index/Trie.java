package saere.database.index;

import java.util.Iterator;

import saere.StringAtom;
import saere.Term;
import saere.Variable;
import saere.database.Utils;
import de.tud.cs.st.prolog.Atom;

/*
 * Iterators do not work with ComplexTermInserter...
 */
public class Trie {
	
	private static TermFlattener flattener = new ShallowTermFlattener();
	private static TermInserter inserter = new SimpleTermInserter();
	
	/** The parent of this node (<tt>null</tt> iff this is the root). */
	private Trie parent;
	
	/** The label of this node. Either a {@link Variable} or an {@link Atom}. */
	// XXX We don't handle variables as of now(? -- untested)
	// XXX Need to store variables?
	// We use the Label class now (more flexibility but slower and memory consuming)
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
	// XXX We assume only one root, code this!
	public Trie() {
		this(null, null);
		inserter.setRoot(this);
	}
	
	// public now, as the TermInserter creates these...
	public Trie(Label label, Trie parent) {
		this.label = label;
		this.parent = parent;
		termList = null;
		firstChild = nextSibling = null;
	}

	/**
	 * Checks wether this is a root node (i.e., its parent is <tt>null</tt>).
	 * 
	 * @return <tt>true</tt> if this is a root node.
	 */
	public boolean isRoot() {
		return parent == null;
	}
	
	/**
	 * Sets a new label.<br/>
	 * <br/>
	 * <b>Unless the old label is not the same as the new label 
	 * this method forces a trie restructuring. Otherwise this may 
	 * lead to an invalid trie structure with unexpected retrieval behavior.</b>
	 * 
	 * @param label The new label.
	 */
	public void setLabel(Label label) {
		this.label = label;
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
	
	/**
	 * Adds a term to this {@link Trie}.
	 * 
	 * @param term The {@link Term} to add.
	 */
	public void addTerm(Term term) {
		if (termList == null) {
			termList = new TermList(term);
		} else {
			TermList last = termList;
			while (termList.getNext() != null) {
				last = termList;
				termList = termList.getNext();
			}
			last.setNext(new TermList(term));
		}
	}
	
	public Trie getParent() {
		return parent;
	}
	
	public void setParent(Trie parent) {
		this.parent = parent;
	}

	public Trie getFirstChild() {
		return firstChild;
	}
	
	public void setFirstChild(Trie firstChild) {
		this.firstChild = firstChild;
	}

	public Trie getNextSibling() {
		return nextSibling;
	}
	
	public void setNextSibling(Trie nextSibling) {
		this.nextSibling = nextSibling;
	}

	public Trie insert(Term term) {
		assert isRoot() : "Can add to root only";
		inserter.setCurrent(this);
		return inserter.insert(new TermStack(flattener.flatten(term)), term);
	}
	
/*	
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
				addTerm(t);
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
*/
	
	// XXX Remove this later...
	@Override
	public String toString() {
		String tStr = termList != null ? Utils.termToString(getTermList().getTerm()) : "null";
		String lStr = label != null ? label.toString() : "root"; // only the root has no label
		String pStr = parent != null && parent.label != null ? parent.label.toString() : (parent != null && parent.label == null ? "<root>" : "null");
		String fStr = firstChild != null && firstChild.label != null ? firstChild.label.toString() : "null";
		String nStr = nextSibling != null && nextSibling.label != null ? nextSibling.label.toString() : "null";
		return "[Trie " + lStr + ", parent=" + pStr + ", firstChild=" + fStr + ", nextSibling=" + nStr + ", term=" + tStr + "]";
	}
	
	/**
	 * @see TrieTermIterator#TrieIterator(Trie, Term...)
	 */
	public Iterator<Term> iterator(Term[] terms) {
		if (inserter instanceof SimpleTermInserter) {
			return new SimpleTrieTermIterator(this, terms);
		} else if (inserter instanceof ComplexTermInserter) {
			return new ComplexTrieTermIterator(this, terms);
		} else {
			return EmptyTermIterator.getInstance();
		}
	}
	
	public Iterator<Term> iterator() {
		assert isRoot() : "Can create iterator from root only";
 		
		return new TrieTermIterator(this);
		
		/*
		// skip root and begin with with first child (removes an isRoot() check from the iterator)
		if (firstChild != null) {
			return new TrieTermIterator(firstChild); // XXX this shouldn't work as we see the first child as 'root' (which is looked at if it had no siblings)
		} else {
			return EmptyTermIterator.getInstance(); // root has not first child, trie is empty
		}
		*/
	}
	
	public Iterator<Trie> nodeIterator() {
		return new TrieNodeIterator(this);
	}
	
	
	public static void setTermFlattener(TermFlattener flattener) {
		Trie.flattener = flattener;
	}
	
	public static TermFlattener getTermFlattener() {
		return Trie.flattener;
	}
	
	public static void setTermInserter(TermInserter inserter) {
		Trie.inserter = inserter;
	}
	
	public static TermInserter getTermInserter() {
		return Trie.inserter;
	}
	
	public Trie getPredicateSubtrie(StringAtom functor) {
		assert isRoot() : "Can get predicate subtries from root only";
		Trie child = firstChild;
		while (child != null) {
			if (labelMatches(functor)) {
				return child;
			}
			child = child.nextSibling;
		}
		
		return null;
	}
	
	/**
	 * Checks if this {@link Label} matches the specified {@link saere.Atom} or 
	 * free {@link Variable}.<br/>
	 * <br/>
	 * Convenience method that behaves (wraps) exactly the same as 
	 * {@link Label#match(Term)}.
	 * 
	 * @param term An {@link saere.Atom} or free {@link Variable}.
	 * @return <tt>true</tt> if so.
	 * @see Label#match(Term)
	 */
	// to abstract from the horrible Label implementations...
	public boolean labelMatches(Term term) {
		assert term.isIntegerAtom() || term.isStringAtom() || (term.isVariable() && !term.asVariable().isInstantiated()) : "Invalid term paremeter";
		return label != null && label.match(term);
	}
	
	public int labelLength() {
		if (label == null) {
			// this was 1 before! 10/5/2010 3:45 PM
			return 0; // XXX Hmm, actually ONLY the root should have no label, and we don't pop sth for the root...
		} else {
			return label.length();
		}
	}
}
