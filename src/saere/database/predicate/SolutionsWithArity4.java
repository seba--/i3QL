package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity4 implements Solutions {

	private final Iterator<Term> iterator;
	private final Term arg0;
	private final Term arg1;
	private final Term arg2;
	private final Term arg3;
	private final State s0;
	private final State s1;
	private final State s2;
	private final State s3;
	
	private int progress;
	
	public SolutionsWithArity4(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 4 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		arg1 = query.arg(1);
		arg2 = query.arg(2);
		arg3 = query.arg(3);
		
		// save original states
		s0 = arg0.manifestState();
		s1 = arg1.manifestState();
		s2 = arg2.manifestState();
		s3 = arg3.manifestState();
		
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
				progress++;
				if (arg1.unify(fact.arg(1))) {
					progress++;
					if (arg2.unify(fact.arg(2))) {
						progress++;
						if (arg3.unify(fact.arg(3))) {
							return true;
						}
					}
				}
			}
			
			reset();
		}
		
		return false;
	}
	
	// Reset only where necessary
	private void reset() {
		if (progress > 0) arg0.setState(s0);
		if (progress > 1) arg1.setState(s1);
		if (progress > 2) arg2.setState(s2);
		if (progress > 3) arg3.setState(s3);
		progress = 0;
	}

}
