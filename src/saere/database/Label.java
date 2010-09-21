package saere.database;

import saere.Atom;
import saere.Term;
import saere.Variable;

/**
 * Abstract base class for {@link AtomLabel} and {@link CompoundLabel}. 
 * The {@link Label} class serves as sophisticated container for label(s) which 
 * can be atoms or variables. Every trie node has a label and determines what 
 * the node <i>represents</i>.
 * 
 * @author David Sullivan
 * @version 0.1, 9/21/2010
 */
public abstract class Label {
		
	/**
	 * Checks wether <tt>this</tt> is an {@link AtomLabel} or not.
	 * 
	 * @return <tt>true</tt> if so.
	 */
	public boolean isAtomLabel() {
		return false;
	}
	
	/**
	 * Checks wether <tt>this</tt> is an {@link CompoundLabel} or not.
	 * 
	 * @return <tt>true</tt> if so.
	 */
	public boolean isCompoundLabel() {
		return false;
	}
	
	/**
	 * The length of the underlying array of labels. Note that this is always 1 
	 * for an {@link AtomLabel}.
	 * 
	 * @return The length of the underlying array of labels.
	 */
	public abstract int length();
	
	/**
	 * Checks wether and how <tt>this</tt> {@link Label} matches the <tt>other</tt>.
	 * Matching is done from the left to the right.
	 * 
	 * @param other The other {@link Label}.
	 * @return The length of the matching prefix.
	 */
	public abstract int match(Label other);
	
	/**
	 * Checks wether the (single) {@link Atom} / free {@link Variable} of this 
	 * {@link Label} matches the specified {@link Atom} / free {@link Variable}.
	 * <br/>
	 * <br/>
	 * Note that a single {@link Atom} / free {@link Variable} can never match 
	 * the underlying array of a {@link CompoundLabel}.
	 * 
	 * @param other An {@link Atom} or a free {@link Variable}.
	 * @return <tt>true</tt> if so.
	 */
	public abstract boolean match(Term other);
	
	/**
	 * Checks wether <tt>this</tt> {@link Label} is the <i>same</i> as the <tt>other</tt>.
	 * Two {@link Label}s are the same if they match completely and if they have the same length.
	 * 
	 * @param other The other {@link Label}.
	 * @return <tt>true</tt> if the {@link Label}s are the same.
	 * @see Label#match(Label)
	 * @see Label#length()
	 */
	public abstract boolean same(Label other);
	
	/**
	 * Gets the underlying {@link Atom} / free {@link Variable} at the 
	 * specified <tt>index</tt>.<br/>
	 * <br/>
	 * The only valid index for {@link AtomLabel}s is <tt>0</tt>.
	 * 
	 * @param index The index of the wanted {@link Atom} / free {@link Variable}.
	 * @return The {@link Atom} / free {@link Variable}.
	 */
	public abstract Term getLabel(int index);
	
	/**
	 * Checks wether <tt>t0</tt> is the same as <tt>t1</tt> (i.e., if they <i>would unify</i>). 
	 * Both specified terms must be eather an atom or a variable.<br/>
	 * <br/>
	 * <b>Must yield the same result as {@link Term#unify(Term)} iff <tt>t0</tt> and <tt>t1</tt> are atoms or variables.</b>
	 * 
	 * @param t0 The first atom/variable.
	 * @param t1 The second atom/variable.
	 * @return <tt>true</tt> if so.
	 */
	// Move to a better place...
	protected boolean same(Term t0, Term t1) {
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
				return true; // for this purpose two variables are the same, if they are not instantiated (XXX ?)
			} else if (v0.isInstantiated() && v1.isInstantiated()) {
				return same(v0.binding(), v1.binding()); // loop of doom with cyclic bindinds?
			}
		}
		
		return false;
	}
	
	/**
	 * Creates a new {@link Label} (i.e., {@link AtomLabel}) with the specified atom / free variable.
	 * 
	 * @param term An atom or a free variable.
	 * @return A new {@link AtomLabel}.
	 */
	public static Label makeLabel(Term term) {
		return new AtomLabel(term);
	}
	
	/**
	 * Creates a new {@link Label} with the specified atom(s) / free variable(s).
	 * 
	 * @param terms An array of atoms and/or free variables.
	 * @return A new {@link Label}.
	 */
	public static Label makeLabel(Term[] terms) {
		if (terms.length > 1) {
			return new CompoundLabel(terms);
		} else {
			return new AtomLabel(terms[0]);
		}
	}
	
	/**
	 * Splits a {@link CompoundLabel} at the specified <tt>index</tt> so that the atom 
	 * / free variable at <tt>index</tt> is the last atom of the first new {@link Label}.<br/>
	 * <br/>
	 * <b>This operation is not supported for {@link AtomLabel}s.</b>
	 * 
	 * @param index The split index.
	 * @return Two new {@link Label}s: the first (at <tt>0</tt>) and the second (at <tt>1</tt>).
	 */
	public abstract Label[] split(int index);
}
