package saere.database.predicate;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity3 implements Solutions {

	private final Iterator<Term> iterator;
	private final Term arg0;
	private final Term arg1;
	private final Term arg2;
	private final State s0;
	private final State s1;
	private final State s2;
	
	private int progress;
	
	//private int counter = 0;
	
	public SolutionsWithArity3(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 3 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		arg1 = query.arg(1);
		arg2 = query.arg(2);
		
		// save original states
		s0 = arg0.manifestState();
		s1 = arg1.manifestState();
		s2 = arg2.manifestState();
		
		iterator = adapter.query(query);
	}

	public boolean next() {

		// restore old states
		reset();
		
		while (iterator.hasNext()) {
			//counter++;
			Term fact = iterator.next();
			
			// attempt unification...
			progress++;
			if (arg0.unify(fact.arg(0))) {
				progress++;
				if (arg1.unify(fact.arg(1))) {
					progress++;
					if (arg2.unify(fact.arg(2))) {
						return true;
					}
				}
			}
			
			reset();
		}
		
		//System.out.print("[" + counter + " candidates iterated]");
		return false;
	}
	
	// Reset only where necessary
	private void reset() {
		if (progress > 0) arg0.setState(s0);
		if (progress > 1) arg1.setState(s1);
		if (progress > 2) arg2.setState(s2);
		progress = 0;
	}
}
