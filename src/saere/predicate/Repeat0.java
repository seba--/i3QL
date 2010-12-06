package saere.predicate;

import saere.Solutions;
import saere.StringAtom;
import saere.Term;

// TODO implement the "real" Prolog semantics
public class Repeat0 implements Solutions {

	// ?- repeat,write(x),fail.
	// xxxxxxxx.....xxxxxx

	// ?- repeat,write(x),!,fail.
	// x
	// false.

	static void registerWithPredicateRegistry(
			PredicateRegistry predicateRegistry) {

		predicateRegistry.registerPredicate(StringAtom.StringAtom("repeat"), 0,
				new PredicateInstanceFactory() {

					@Override
					public Solutions createPredicateInstance(Term[] args) {
						return new Repeat0();
					}
				});

	}

	public Repeat0() {
		// nothing to do
	}

	public boolean next() {
		return true;
	}

}
