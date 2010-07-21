package prologre.demo.heritage;

import static prologre.Atom.Atom;
import prologre.Atom;
import prologre.GoalIterator;
import prologre.State;
import prologre.Term;

// This class will be "generated" from either a prolog file or some meta-information...
public class Mother_2 {

    private static Mother_2 instance = new Mother_2();

    public static Mother_2 getInstance() {
	return instance;
    }

    // facts are always sorted... by the first, second,... parameter.
    private final Atom[][] data = new Atom[][] { { Atom("Christel"), Atom("Michael") },
	    { Atom("Gertrud"), Atom("Christel") }, { Atom("Magdalena"), Atom("Alice") },
	    { Atom("Magdalena"), Atom("Thilo") } };

    // A parameter is either a (Ground)Term, an (instantiated / free) Variable or an Atom...
    public GoalIterator unify(final Term parameter0, final Term parameter1) {

	return new GoalIterator() {

	    int index = 0;

	    private final State parameter0State = parameter0.manifestState();
	    private final State parameter1State = parameter1.manifestState();

	    // The binding is implicitly stored in the variables (if any)
	    public boolean next() {

		parameter0.setState(parameter0State);
		parameter1.setState(parameter1State);

		// here, we have great potential to optimize the code... (use indices, HashSets,...)

		while (index < data.length) {
		    int currentIndex = index++;
		    if (data[currentIndex][0].unify(parameter0)) {
			if (data[currentIndex][1].unify(parameter1))
			    return true;
			else
			    // the first parameter was successfully bound... we have to reset the
			    // state before we can search for the next solution
			    parameter0.setState(parameter0State);
		    }
		}

		return false;
	    }

	};
    }
}
