package prologre.demo.heritage;

import static prologre.Atom.Atom;
import prologre.Atom;
import prologre.GoalIterator;
import prologre.State;
import prologre.Term;

// This class will be "generated" from either a prolog file or some meta-information...
// meta-information: father(Atom,Atom)
public class Father_2 {

    // ... we have to be able to look up predicates...

    private static Father_2 instance = new Father_2();

    public static Father_2 getInstance() {
	return instance;
    }

    // facts are always sorted... by the first, second,... parameter.
    private final Atom[][] data = new Atom[][] { { Atom("Reinhard"), Atom("Alice") },
	    { Atom("Reinhard"), Atom("Thilo") }, { Atom("Werner"), Atom("Michael") } };

    public GoalIterator unify(final Term p0, final Term p1) {

	return new GoalIterator() {

	    private int index = 0;

	    private final State p0State = p0.manifestState();
	    private final State p1State = p1.manifestState();

	    // The binding is implicitly stored in the variables (if any)
	    public boolean next() {

		p0.setState(p0State);
		p1.setState(p1State);

		// here, we have great potential to optimize the code... (use indices, HashSets,...)
		while (index < data.length) {
		    int currentIndex = index++;
		    if (data[currentIndex][0].unify(p0)) {
			if (data[currentIndex][1].unify(p1))
			    return true;
			else
			    // the first parameter was successfully bound... we have to reset the
			    // state before we can search for the next solution
			    p0.setState(p0State);
		    }
		}

		return false;
	    }

	};
    }
}
