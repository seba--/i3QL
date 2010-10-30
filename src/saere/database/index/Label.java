package saere.database.index;

import saere.Atom;
import saere.Term;
import saere.Variable;

/**
 * Abstract base class for {@link AtomLabel} and {@link CompoundLabel}. 
 * The {@link Label} class serves as sophisticated container for label(s) which 
 * can be atoms only. Every trie node has a label that determines what 
 * the node <i>represents</i>.
 * 
 * @author David Sullivan
 * @version 0.2, 10/14/2010
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
	 * Gets the underlying {@link Atom} at the specified <tt>index</tt>.<br/>
	 * <br/>
	 * The only valid index for {@link AtomLabel}s is <tt>0</tt>.
	 * 
	 * @param index The index of the wanted {@link Atom} / free {@link Variable}.
	 * @return The {@link Atom} / free {@link Variable}.
	 */
	public abstract Atom getLabel(int index);
	
	/**
	 * Creates a new {@link Label} (i.e., {@link AtomLabel}) with the specified atom / free variable.
	 * 
	 * @param atom An atom.
	 * @return A new {@link AtomLabel}.
	 */
	@Deprecated
	public static Label makeLabel(Atom atom) {
		return new AtomLabel(atom);
	}
	
	/**
	 * Creates a new {@link Label} with the specified atom(s).
	 * 
	 * @param terms An array of atoms.
	 * @return A new {@link Label}.
	 */
	@Deprecated
	public static Label makeLabel(Atom[] atoms) {
		assert atoms.length > 0 : "Specified terms length is 0";
		
		if (atoms.length > 1) {
			return new CompoundLabel(atoms);
		} else {
			return new AtomLabel(atoms[0]);
		}
	}
	
	/**
	 * Splits a {@link CompoundLabel} at the specified <tt>index</tt> so that 
	 * the atom at <tt>index</tt> is the last atom of the current {@link Label}.<br/>
	 * <br/>
	 * <b>This operation is not supported for {@link AtomLabel}s.</b>
	 * 
	 * @param index The split index.
	 * @return The new label representing the second half of the split.
	 * */
	public abstract Label split(int index);
	
	/**
	 * Returns the underlying {@link Term}(s) as array.
	 * 
	 * @return The underlying {@link Term}(s) as array.
	 */
	public abstract Atom[] asArray();
}
