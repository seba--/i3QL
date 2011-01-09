

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.tak4Factory;
import saere.PredicateRegistry;
import saere.Goal;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class MainTak {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		tak4Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Throwable {

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

		final Variable result = new Variable();
		final long x = 32;
		final long y = 12;
		final long z = 6;
		final Term t = compoundTerm(StringAtom.get("time"),
				compoundTerm(atomic("tak"), atomic(x), atomic(y), atomic(z), result));
		{
			Goal g = t.call();
			while (g.next()) {
				System.out.println("Result=" + result.toProlog());
			}
		}

		for (int i = 2; i <= 10; i++) {
			long startTime = System.nanoTime();
			Goal g = t.call();
			if (!(g.next() && !g.next())) {
				throw new Error("internal programming error");
			}
			long duration = System.nanoTime() - startTime;
			Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
			All.writeToPerformanceLog("tak("+x+","+y+","+z+") run " + i + " (find all solutions) finished in: " + time
					+ "\n");
		}
	}

}
