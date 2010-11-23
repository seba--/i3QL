package saere.database.predicate;

import static saere.database.Utils.hasFreeVariable;

import java.util.Iterator;

import saere.Solutions;
import saere.State;
import saere.Term;
import saere.database.DatabaseAdapter;

public final class SolutionsWithArity15NoCollision implements Solutions, NoCollision {

	private final Term arg0;
	private final Term arg1;
	private final Term arg2;
	private final Term arg3;
	private final Term arg4;
	private final Term arg5;
	private final Term arg6;
	private final Term arg7;
	private final Term arg8;
	private final Term arg9;
	private final Term arg10;

	private final State s0;
	private final State s1;
	private final State s2;
	private final State s3;
	private final State s4;
	private final State s5;
	private final State s6;
	private final State s7;
	private final State s8;
	private final State s9;
	private final State s10;
	
	// Free variables
	private final boolean var0;
	private final boolean var1;
	private final boolean var2;
	private final boolean var3;
	private final boolean var4;
	private final boolean var5;
	private final boolean var6;
	private final boolean var7;
	private final boolean var8;
	private final boolean var9;
	private final boolean var10;
	
	private Iterator<Term> iterator;

	public SolutionsWithArity15NoCollision(DatabaseAdapter adapter, Term query) {
		assert query.arity() == 15 : "Invalid query arity " + query.arity();
		
		arg0 = query.arg(0);
		arg1 = query.arg(1);
		arg2 = query.arg(2);
		arg3 = query.arg(3);
		arg4 = query.arg(4);
		arg5 = query.arg(5);
		arg6 = query.arg(6);
		arg7 = query.arg(7);
		arg8 = query.arg(8);
		arg9 = query.arg(9);
		arg10 = query.arg(10);

		// save original states
		s0 = arg0.manifestState();
		s1 = arg1.manifestState();
		s2 = arg2.manifestState();
		s3 = arg3.manifestState();
		s4 = arg4.manifestState();
		s5 = arg5.manifestState();
		s6 = arg6.manifestState();
		s7 = arg7.manifestState();
		s8 = arg8.manifestState();
		s9 = arg9.manifestState();
		s10 = arg10.manifestState();
		
		if (hasFreeVariable(arg0)) var0 = true;
		else var0 = false;
		
		if (hasFreeVariable(arg1)) var1 = true;
		else var1 = false;
		
		if (hasFreeVariable(arg2)) var2 = true;
		else var2 = false;
		
		if (hasFreeVariable(arg3)) var3 = true;
		else var3 = false;
		
		if (hasFreeVariable(arg4)) var4 = true;
		else var4 = false;

		if (hasFreeVariable(arg5)) var5 = true;
		else var5 = false;
		
		if (hasFreeVariable(arg6)) var6 = true;
		else var6 = false;
		
		if (hasFreeVariable(arg7)) var7 = true;
		else var7 = false;
		
		if (hasFreeVariable(arg8)) var8 = true;
		else var8 = false;
		
		if (hasFreeVariable(arg9)) var9 = true;
		else var9 = false;
		
		if (hasFreeVariable(arg10)) var10 = true;
		else var10 = false;
		
		iterator = adapter.query(query);
	}
	
	public boolean next() {
		if (iterator.hasNext()) {
			if (var0) arg0.setState(s0);
			if (var1) arg1.setState(s1);
			if (var2) arg2.setState(s2);
			if (var3) arg3.setState(s3);
			if (var4) arg4.setState(s4);
			if (var5) arg5.setState(s5);
			if (var6) arg6.setState(s6);
			if (var7) arg7.setState(s7);
			if (var8) arg8.setState(s8);
			if (var9) arg9.setState(s9);
			if (var10) arg10.setState(s10);
			
			Term fact = iterator.next();
			
			// We get only terms that'll unify (no set of 'maybe' candidates)
			if (var0) arg0.unify(fact.arg(0));
			if (var1) arg1.unify(fact.arg(1));
			if (var2) arg2.unify(fact.arg(2));
			if (var3) arg3.unify(fact.arg(3));
			if (var4) arg4.unify(fact.arg(4));
			if (var5) arg5.unify(fact.arg(5));
			if (var6) arg6.unify(fact.arg(6));
			if (var7) arg7.unify(fact.arg(7));
			if (var8) arg8.unify(fact.arg(8));
			if (var9) arg9.unify(fact.arg(9));
			if (var10) arg10.unify(fact.arg(10));
			
			return true;
		} else {
			return false;
		}
	}
}
