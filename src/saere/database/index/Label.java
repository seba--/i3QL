package saere.database.index;

import saere.Atom;
import saere.Term;
import saere.Variable;

/**
 * Abstract base class for {@link AtomLabel} and {@link CompoundLabel}. 
 * The {@link Label} class serves as sophisticated container for label(s) which 
 * can be atoms or variables. Every trie node has a label that determines what 
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
		assert terms.length > 0 : "Specified terms length is 0";
		
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
	
	/**
	 * Returns the underlying {@link Term}(s) as array.
	 * 
	 * @return The underlying {@link Term}(s) as array.
	 */
	public abstract Term[] asArray();
}
