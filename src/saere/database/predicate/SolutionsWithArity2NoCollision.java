package saere.database.predicate;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity2NoCollision implements Solutions, NoCollision {

	private final Term arg0;
	private final Term arg1;

	private final State s0;
	private final State s1;
	
	// Free variables
	private final boolean var0;
	private final boolean var1;
	
	private Iterator<Term> iterator;

	public SolutionsWithArity2NoCollision(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 2 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		arg1 = query.arg(1);

		// save original states
		s0 = arg0.manifestState();
		s1 = arg1.manifestState();
		
		if (hasFreeVariable(arg0)) var0 = true;
		else var0 = false;
		
		if (hasFreeVariable(arg1)) var1 = true;
		else var1 = false;
		
		iterator = adapter.query(query);
	}
	
	public boolean next() {
		if (iterator.hasNext()) {
			if (var0) arg0.setState(s0);
			if (var1) arg1.setState(s1);
			
			Term fact = iterator.next();
			
			// We get only terms that'll unify (no set of 'maybe' candidates)
			if (var0) arg0.unify(fact.arg(0));
			if (var1) arg1.unify(fact.arg(1));
			
			return true;
		} else {
			return false;
		}
	}
}
