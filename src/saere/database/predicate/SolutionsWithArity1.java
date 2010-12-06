package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity1 implements Solutions {

	private final Iterator<Term> iterator;
	private final Term arg0;
	private final State s0;
	
	private int progress;
	
	public SolutionsWithArity1(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 1 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		
		// save original states
		s0 = arg0.manifestState();
		
		iterator = adapter.query(query);
	}

	public boolean next() {

		// restore old states
		reset();
		
		while (iterator.hasNext()) {
			Term fact = iterator.next();
			
			// attempt unification...
			progress++;
			if (arg0.unify(fact.arg(0))) {
				return true;
			}
			
			reset();
		}
		
		return false;
	}
	
	// Reset only where necessary
	private void reset() {
		if (progress > 0) arg0.setState(s0);
		progress = 0;
	}
}