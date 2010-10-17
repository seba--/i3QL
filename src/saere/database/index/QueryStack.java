package saere.database.index;

import saere.Term;

/**
 * A query stack can hold atoms (integer/string) and variables.
 * 
 * @author David Sullivan
 * @version 0.1, 10/17/2010
 */
public final class QueryStack extends PseudoStack<Term> {
	
	public QueryStack (Term[] terms) {
		super(terms);
	}

	@Override
	public Term[] asArray() {
		Term[] array = new Term[size()];
		if (array.length > 0) {
			System.arraycopy(values, position, array, 0, array.length);
		}
		return array;
	}
}
