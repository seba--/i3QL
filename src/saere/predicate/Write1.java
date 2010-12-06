package saere.predicate;

import saere.Solutions;
import saere.StringAtom;
import saere.Term;

// TODO implement the "real" Prolog semantics
public class Write1 implements Solutions {

	static void registerWithPredicateRegistry(PredicateRegistry predicateRegistry){

		predicateRegistry.registerPredicate(StringAtom.StringAtom("write"), 1,
				new PredicateInstanceFactory() {

					@Override
					public Solutions createPredicateInstance(Term[] args) {
						return new Write1(args[0]);
					}
				});

	}

	private final Term t;
		
	private boolean called = false;
	
	public Write1(final Term t) {
		this.t = t;
	}

	public boolean next() {
		if (!called){
			called = true;
			System.out.print(t.toString());
			return true;
		}
		else {
			return false;	
		}
		
	}
	
	
}
