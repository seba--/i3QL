package prologre.demo.heritage;

import prologre.GoalIterator;
import prologre.State;
import prologre.Term;
import prologre.Variable;

// sibling (X,Y) :- mother(M,X) , mother(M,Y), father(F,X), father(F,Y), X \= Y.
public class Sibling_2 {

    private static Sibling_2 instance = new Sibling_2();

    public static Sibling_2 getInstance() {
	return instance;
    }

    public GoalIterator unify(final Term x, final Term y) {

	return new GoalIterator() {

	    private final State xState = x.manifestState();
	    private final State yState = y.manifestState();

	    private final Variable M = new Variable(); // a local variable!
	    private final Variable F = new Variable();

	    private final GoalIterator firstMother_2BindingIterator = Mother_2.getInstance().unify(
		    M, x);
	    private GoalIterator secondMother_2BindingIterator = null; // eager initialization is
								       // not possible; the wrong
								       // state information would be
								       // manifested...
	    private GoalIterator firstFather_2BindingIterator = null;
	    private GoalIterator secondFather_2BindingIterator = null;

	    // BETTER NAME(?): doCallFirstGoal, doCallSecondGoal,...
	    private boolean iterateFirstMother_2 = true; // iterateFirstTerm
	    private boolean iterateSecondMother_2 = true; // iterateSecondTerm
	    private boolean iterateFirstFather_2 = true; // iterateThirdTerm

	    @Override
	    public boolean next() {

		if (iterateFirstMother_2) {
		    M.clear();
		    x.setState(xState);

		    if (!firstMother_2BindingIterator.next()) {
			// the state of the parameters is reset (no partial bindings exist)
			return false;
		    } else {
			secondMother_2BindingIterator = Mother_2.getInstance().unify(M, y);
			iterateFirstMother_2 = false;
		    }
		}

		if (iterateSecondMother_2) {
		    // first occurrence of y in the body of the rule
		    // "y" may be a variable and we have to search for further bindings
		    y.setState(yState);

		    if (!secondMother_2BindingIterator.next()) {
			// the state of the parameters is reset (no partial bindings exist)
			secondMother_2BindingIterator = null;
			iterateFirstMother_2 = true;
			return next();
		    } else {
			firstFather_2BindingIterator = Father_2.getInstance().unify(F, x);
			iterateSecondMother_2 = false;
		    }
		}

		if (iterateFirstFather_2) {
		    F.clear(); // first occurrence of F in the body of the rule

		    if (!firstFather_2BindingIterator.next()) {
			// the state of the parameters is reset (no partial bindings exist)
			firstFather_2BindingIterator = null;
			iterateSecondMother_2 = true;
			return next();
		    } else {
			secondFather_2BindingIterator = Father_2.getInstance().unify(F, y);
			iterateFirstFather_2 = false;
		    }
		}

		if (secondFather_2BindingIterator.next()) {

		    // Inlined the code for the not unifiable check:
		    State tempXState = x.manifestState();
		    State tempYState = y.manifestState();
		    if (!x.unify(y)) {
			return true;
		    } else {
			x.setState(tempXState);
			y.setState(tempYState);
			return next();
		    }
		} else {
		    secondFather_2BindingIterator = null; // no longer required
		    iterateFirstFather_2 = true;
		    return next();
		}
	    }
	};
    }
}
