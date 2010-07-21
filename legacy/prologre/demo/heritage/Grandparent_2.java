package prologre.demo.heritage;

import prologre.GoalIterator;
import prologre.State;
import prologre.Term;
import prologre.Variable;

// grandparent (X,Z) :- parent (X,Y) , parent(Y,Z).
public class Grandparent_2 {

    private static Grandparent_2 instance = new Grandparent_2();

    public static Grandparent_2 getInstance() {
	return instance;
    }

    public GoalIterator unify(final Term x, final Term z) {

	return new GoalIterator() {

	    private final State xState = x.manifestState();
	    private final State zState = z.manifestState();

	    private final Variable Y = new Variable();

	    private final GoalIterator firstParent_2BindingIterator = Parent_2.getInstance()
		    .unify(x, Y);
	    private GoalIterator secondParent_2BindingIterator = null;

	    private boolean iterateFirstParent_2 = true;

	    @Override
	    public boolean next() {

		if (iterateFirstParent_2) {
		    x.setState(xState);
		    Y.clear();

		    if (!firstParent_2BindingIterator.next()) {
			// the state of the parameters is reset (no partial bindings exist)
			return false;
		    } else {
			// we first want to iterate over all bindings for parent(Y,Z)
			secondParent_2BindingIterator = Parent_2.getInstance().unify(Y, z);
			iterateFirstParent_2 = false;
		    }
		}

		z.setState(zState);
		if (secondParent_2BindingIterator.next()) {
		    return true;
		} else {
		    secondParent_2BindingIterator = null; // no longer required
		    iterateFirstParent_2 = true;
		    return next();
		}
	    }
	};
    }
}
