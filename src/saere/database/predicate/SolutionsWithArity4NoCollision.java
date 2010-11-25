package saere.database.predicate;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity4NoCollision implements Solutions, NoCollision {

	private final Term arg0;
	private final Term arg1;
	private final Term arg2;
	private final Term arg3;

	private final State s0;
	private final State s1;
	private final State s2;
	private final State s3;
	
	// Free variables
	private final boolean var0;
	private final boolean var1;
	private final boolean var2;
	private final boolean var3;
	
	private Iterator<Term> iterator;

	public SolutionsWithArity4NoCollision(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 10 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		arg1 = query.arg(1);
		arg2 = query.arg(2);
		arg3 = query.arg(3);

		// save original states
		s0 = arg0.manifestState();
		s1 = arg1.manifestState();
		s2 = arg2.manifestState();
		s3 = arg3.manifestState();
		
		if (hasFreeVariable(arg0)) var0 = true;
		else var0 = false;
		
		if (hasFreeVariable(arg1)) var1 = true;
		else var1 = false;
		
		if (hasFreeVariable(arg2)) var2 = true;
		else var2 = false;
		
		if (hasFreeVariable(arg3)) var3 = true;
		else var3 = false;
		
		iterator = adapter.query(query);
	}
	
	public boolean next() {
		if (iterator.hasNext()) {
			if (var0) arg0.setState(s0);
			if (var1) arg1.setState(s1);
			if (var2) arg2.setState(s2);
			if (var3) arg3.setState(s3);
			
			Term fact = iterator.next();
			
			// We get only terms that'll unify (no set of 'maybe' candidates)
			if (var0) arg0.unify(fact.arg(0));
			if (var1) arg1.unify(fact.arg(1));
			if (var2) arg2.unify(fact.arg(2));
			if (var3) arg3.unify(fact.arg(3));
			
			return true;
		} else {
			return false;
		}
	}
}
