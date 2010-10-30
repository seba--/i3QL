package saere.database.index;

import java.util.Iterator;

import saere.Atom;
import saere.StringAtom;
import saere.Term;
import saere.database.Utils;


/**
 * Class that is used to create {@link Term} indices based on tries (prefix 
 * trees). Normal tries are string-based while this implementation uses string 
 * and integer atoms (Prolog terms).<br />
 * <br />
 * The structure of an {@link Trie} is determined by an insertion policy, 
 * either <i>simple</i> or <i>complex</i> which is implemented by a 
 * {@link TrieBuilder} (i.e., {@link SimpleTermInserter} or 
 * {@link ComplexTermInserter}).<br />
 * <br />
 * Every {@link Trie} (node) has a {@link Label} that determines what this node 
 * represents.
 * 
 * @author David Sullivan
 * @version 0.7, 10/14/2010
 */
// XXX How much difference is there between strings and string/integer atoms in the end?
public final class Trie {
	
	private static TermFlattener flattener = new ShallowTermFlattener();
	private static TrieBuilder inserter = new SimpleTermInserter();
	
	/** The parent of this node (<tt>null</tt> iff this is the root). */
	private Trie parent;
	
	/** The label of this node, an {@link Atom}. */
	// We use the Label class now (more flexibility but more abstraction)
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
		this(new AtomLabel(null), null);
	}
	
	/**
	 * Creates a trie node with the specified {@link Label} and parent.
	 * 
	 * @param label The label of this node.
	 * @param parent The parent of this node.
	 */
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
		return inserter.insert(new InsertStack(flattener.flattenInsertion(term)), term);
	}
	
	// XXX Remove this later...
	@Override
	public String toString() {
		String tStr = termList != null ? Utils.termToString(getTermList().getTerm()) : "null";
		String lStr = label != null ? label.toString() : "root"; // only the root has no label
		String pStr = parent != null && parent.label != null ? parent.label.toString() : (parent != null && parent.label == null ? "<root>" : "null");
		String fStr = firstChild != null && firstChild.label != null ? firstChild.label.toString() : "null";
		String nStr = nextSibling != null && nextSibling.label != null ? nextSibling.label.toString() : "null";
		return "Trie={label=" + lStr + ", parent=" + pStr + ", firstChild=" + fStr + ", nextSibling=" + nStr + ", term=" + tStr + "}";
	}
	
	/**
	 * @see TrieTermIterator#TrieIterator(Trie, Term...)
	 */
	public Iterator<Term> iterator(Term[] terms) {
		return inserter.iterator(this, terms);
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
	
	// XXX Actually not needed here...
	public Iterator<Trie> nodeIterator() {
		return new TrieNodeIterator(this);
	}

	public static void setTermFlattener(TermFlattener flattener) {
		Trie.flattener = flattener;
	}
	
	// Actually only for screening purposes...
	public static TermFlattener getTermFlattener() {
		return Trie.flattener;
	}
	
	// Instead of returning the TermFlattener and flatten then, we provide the service directly.
	public Term[] flattenForQuery(Term[] terms) {
		return flattener.flattenQuery(terms);
	}
	
	public static void setTermInserter(TrieBuilder inserter) {
		Trie.inserter = inserter;
	}
	
	public static TrieBuilder getTermInserter() {
		return Trie.inserter;
	}
	
	// XXX Also not needed...
	public Trie getPredicateSubtrie(StringAtom functor) {
		assert isRoot() : "Can get predicate subtries from root only";
		Trie child = firstChild;
		while (child != null) {
			if (Matcher.match(label, functor)) {
				return child;
			}
			child = child.nextSibling;
		}
		
		return null;
	}
	
	public int getLabelLength() {
		return label.length();
		
		/*
		if (label == null) {
			throw new UnsupportedOperationException("The root trie has no label");
		} else {
			return label.length();
		}
		*/
	}
}
