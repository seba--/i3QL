package saere.database.predicate;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity1NoCollision implements Solutions, NoCollision {

	private final Term arg0;

	private final State s0;
	
	// Free variables
	private final boolean var0;
	
	private Iterator<Term> iterator;

	public SolutionsWithArity1NoCollision(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 1: "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);

		// save original states
		s0 = arg0.manifestState();
		
		if (hasFreeVariable(arg0)) var0 = true;
		else var0 = false;
		
		iterator = adapter.query(query);
	}
	
	public boolean next() {
		if (iterator.hasNext()) {
			if (var0) arg0.setState(s0);
			
			Term fact = iterator.next();
			
			// We get only terms that'll unify (no set of 'maybe' candidates)
			if (var0) arg0.unify(fact.arg(0));
			
			return true;
		} else {
			return false;
		}
	}
}
