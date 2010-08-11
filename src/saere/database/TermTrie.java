package saere.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import saere.Atom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class TermTrie implements Serializable {
	
	private static final long serialVersionUID = 1L;

	public static final Atom ROOT_ATOM = null;

	private Term label; // will be an atom or a variable
	private Term term;
	private TermTrie firstChild;
	private TermTrie nextSibling; // right sibling
	private final boolean root;

	public TermTrie(Term label) {
		assert label == null || label.isIntegerAtom() || label.isStringAtom() || label.isVariable() : "Invalid label";
		
		this.label = label;
		term = null;
		firstChild = nextSibling = null;
		root = label == null ? true : false;
	}

	public boolean isRoot() {
		return root;
	}

	public Term getLabel() {
		return label;
	}

	public Term getTerm() {
		return term;
	}

	public TermTrie getFirstChild() {
		return firstChild;
	}

	public TermTrie getNextSibling() {
		return nextSibling;
	}

	public TermTrie add(Term term) {
		assert root : "Can add to root only";
		
		return add(new TermStack(breakDown(term).toArray(new Term[0])), term);
	}

	private TermTrie add(TermStack ts, Term t) {
		Term first = ts.peek();
		
		assert first != null && (first.isIntegerAtom() || first.isStringAtom() || first.isVariable()) : "Invalid first";
		
		if (root) {
			// add to own subtrie
			if (firstChild == null) {
				firstChild = new TermTrie(first);
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
				firstChild = new TermTrie(ts.peek());
			}
			return firstChild.add(ts, t);
		} else {
			// add to sibling subtrie
			if (nextSibling == null) {
				nextSibling = new TermTrie(first);
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
		TermTrie startTrie = firstChild;
		
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
		if (same(first, label) || (first.isVariable() && !first.asVariable().isInstantiated())) {
			
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
		assert root : "Only call to root allowed";
		
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

	private void collectLeafTerms(TermTrie trie, List<Term> result) {
		if (trie.term != null) {
			result.add(trie.term);
		} else {
			TermTrie child = trie.firstChild;
			while (child != null) {
				collectLeafTerms(child, result);
				child = child.nextSibling;
			}
		}
	}
	
	@Override
	public String toString() {
		return label.toString();
	}
	
	public TermTrie getPredicateSubtrie(StringAtom functor) {
		assert root : "Can get predicate subtries only from root";

		// precidate functors are direct root children
		TermTrie child = firstChild;
		while (child != null) {
			if (same(child.label, functor)) {
				break;
			}
			child = child.nextSibling;
		}
		
		assert child != null : "No predicate for specified functor";
		return child;
	}
}
