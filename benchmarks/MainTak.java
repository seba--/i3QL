//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.tak4Factory;
import saere.PredicateRegistry;
import saere.Goal;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class MainTak {

	public static void main(String[] args) throws Throwable {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		tak4Factory.registerWithPredicateRegistry(registry);

		// Welcome to SWI-Prolog (Multi-threaded, 64 bits, Version 5.10.2)
		// 3.06Ghz Core2Duo
		// 4GB DDR3 Ram
		//
		// ?- time(tak(32,12,6,R)).
		// % 10,440,011 inferences, 1.130 CPU in 1.145 seconds (99% CPU, 9238948
		// Lips)
		// R = 7.
		//
		// ?- time(tak(32,14,6,R)).
		// % 182,748,891 inferences, 20.010 CPU in 20.190 seconds (99% CPU,
		// 9132878 Lips)
		// R = 7.

		System.out.println("Warm up...");
		{

			Variable result = new Variable();
			Term t = compoundTerm(
					StringAtom.get("time"),
					compoundTerm(atomic("tak"), atomic(18), atomic(14),
							atomic(6), result));
			Goal s = t.call();
			if (!s.next()) {
				throw new Error("internal programming error");
			} else {
				System.out.println("\nResult=" + result.toProlog());
			}
		}

		System.out
				.println("Sleeping for five seconds...");
		Thread.sleep(5000);
		Thread t = new Thread(new Runnable() {
			public void run() {
				Variable result = new Variable();
				Term term = compoundTerm(
						atomic("time"),
						compoundTerm(atomic("tak"), atomic(32), atomic(12),
								atomic(6), result));
				Goal s = term.call();
				if (!s.next()) {
					throw new Error("internal programming error");
				} else {
					System.out.println("\nResult=" + result.toProlog());
				}
			}
		});
		t.start();
		t.join();
	}

}
