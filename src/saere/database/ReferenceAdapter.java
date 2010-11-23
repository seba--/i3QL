package saere.database;

import static saere.database.Utils.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import saere.StringAtom;
import saere.Term;
import saere.database.index.EmptyTermIterator;
import saere.database.index.reference.ReferenceDatabase;

public final class ReferenceAdapter implements DatabaseAdapter {

	private final HashMap<StringAtom,LinkedList<Term>> index;
	private final LinkedList<Term> list;
	
	public ReferenceAdapter(ReferenceDatabase database, StringAtom functor) {
		index = database.getIndex(functor);
		list = database.getList(functor);
		
		assert index != null : "Unable to get index for functor " + functor + " from reference database";
		assert list != null : "Unable to get set for functor " + functor + " from reference database";
	}
	
	@Override
	public Iterator<Term> query(Term query) {
		assert query.arity() > 0 : "Compound term as query expected";
		
		if (!hasFreeVariable(query.arg(0))) {
			LinkedList<Term> bucket = index.get(query.arg(0));
			if (bucket != null) {
				return bucket.iterator();
			} else {
				return EmptyTermIterator.getInstance();
			}
		} else {
			return list.iterator();
		}
	}

}
