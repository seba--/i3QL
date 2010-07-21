package prologre.demo.heritage;

import prologre.GoalIterator;
import prologre.State;
import prologre.Term;

// parent(X,Y) :- mother(X,Y)
// parent(X,Y) :- father(X,Y)
public class Parent_2 {

    private static Parent_2 instance = new Parent_2();

    public static Parent_2 getInstance() {
	return instance;
    }

    public GoalIterator unify(final Term parameter0, final Term parameter1) {

	return new GoalIterator() {

	    private final State parameter0State = parameter0.manifestState();
	    private final State parameter1State = parameter0.manifestState();

	    // It is important to create all BindingIterators (for all "||"ed statements) at the
	    // very beginning to enable the predicates to save the correct state information.

	    private final GoalIterator mother_2BindingIterator = Mother_2.getInstance().unify(
		    parameter0, parameter1);
	    private final GoalIterator father_2BindingIterator = Father_2.getInstance().unify(
		    parameter0, parameter1);

	    @Override
	    public boolean next() {
		// the variables X and Y are free...
		parameter0.setState(parameter0State);
		parameter1.setState(parameter1State);

		// successive calls to this method will first iterate over all solutions of the
		// mother predicate before we iterate over all solutions of the father predicate.

		return mother_2BindingIterator.next() || father_2BindingIterator.next();
	    }
	};
    }
}
