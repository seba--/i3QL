package saere.database.index;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

import saere.Atom;

/**
 * Represents labels of {@link Trie}s.
 * 
 * @author David Sullivan
 * @version 0.1, 11/9/2010
 */
public abstract class Label {
	
	/*
	 * Labels are cached so that no two instances of a label exist that would 
	 * be equal/same. Hence, we can use reference equality to check wether a 
	 * label matches another (completely). Also, we save some memory as it is 
	 * to be expected that many equal/same labels appear in different places in 
	 * a trie.
	 */
	protected static final WeakHashMap<Label, WeakReference<Label>> CACHE = new WeakHashMap<Label, WeakReference<Label>>();
	
	/**
	 * Gets the length of the label (number of atoms).
	 * 
	 * @return The length of this label.
	 */
	public abstract int length();
	
	/**
	 * Gets the arity of the label. The arity is only greater-than <tt>0</tt> 
	 * if this label represents a functor of a compound term.
	 * 
	 * @return The arity of this label.
	 */
	public abstract int arity();
	
	/**
	 * Gets the underlying atom.
	 * 
	 * @return The underlying atom.
	 */
	public abstract Atom atom();
	
	public boolean sameAs(Label other) {
		return this == other;
	}
	
	/**
	 * Gets the underlying labels of a complex label.
	 * 
	 * @return The underlying labels.
	 */
	public abstract SimpleLabel[] labels();
}
