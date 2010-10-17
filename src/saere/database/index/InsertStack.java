package saere.database.index;

import saere.Atom;

/**
 * A query stack can hold atoms (integer/string).
 * 
 * @author David Sullivan
 * @version 0.1, 10/17/2010
 */
public final class InsertStack extends PseudoStack<Atom> {

	public InsertStack(Atom[] atoms) {
		super(atoms);
	}

	@Override
	public Atom[] asArray() {
		Atom[] array = new Atom[size()];
		if (array.length > 0) {
			System.arraycopy(values, position, array, 0, array.length);
		}
		return array;
	}
}
