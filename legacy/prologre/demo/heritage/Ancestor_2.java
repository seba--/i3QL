package prologre.demo.heritage;

import prologre.GoalIterator;
import prologre.State;
import prologre.Term;
import prologre.Variable;

// ancestor (X,Z) :- parent (X,Z).
// ancestor (X,Z) :- parent (X,Y), ancestor (Y,Z).
public class Ancestor_2 {

    private static Ancestor_2 instance = new Ancestor_2();

    public static Ancestor_2 getInstance() {
	return instance;
    }

    public GoalIterator unify(final Term x, final Term z) {

	return new GoalIterator() {
	    // These are the parameters
	    private final State xState = x.manifestState();
	    private final State zState = z.manifestState();
	    // "local" variable
	    private final Variable Y = new Variable();
	    //

	    private GoalIterator firstRuleParent_2BindingIterator = Parent_2.getInstance().unify(x,
		    z);
	    private GoalIterator secondRuleParent_2BindingIterator = null;
	    private GoalIterator secondRuleAncestor_2BindingIterator = null;
	    private boolean iterateSecondRuleParent_2 = true;

	    private int activeRule = 1;

	    @Override
	    public boolean next() {

		switch (activeRule) {
		case 1:
		    x.setState(xState);
		    z.setState(zState);

		    if (firstRuleParent_2BindingIterator.next()) {
			return true;
		    } else {
			firstRuleParent_2BindingIterator = null; // facilitate GC
			secondRuleParent_2BindingIterator = Parent_2.getInstance().unify(x, Y);
			activeRule = 2;
			return next();
		    }
		case 2:

		    if (iterateSecondRuleParent_2) {
			x.setState(xState);
			Y.clear();
			if (!secondRuleParent_2BindingIterator.next())
			    return false;
			else {
			    secondRuleAncestor_2BindingIterator = Ancestor_2.getInstance().unify(Y,
				    z);
			    iterateSecondRuleParent_2 = false;
			}
		    }

		    z.setState(zState);
		    if (secondRuleAncestor_2BindingIterator.next()) {
			return true;
		    } else {
			secondRuleAncestor_2BindingIterator = null;
			iterateSecondRuleParent_2 = true;
			return next();
		    }
		default:
		    throw new UnknownError();
		}
	    }
	};
    }
}
