import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.nrev_range2Factory;
import predicates.run_nrev2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Variable;

public class nrev {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		run_nrev2Factory.registerWithPredicateRegistry(registry);
		nrev_range2Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		for (long i = 1; i <= 5; i++) {
			Variable solution = new Variable();
			StringAtom run_nrev = StringAtom.get("run_nrev");
			Goal s = compoundTerm(run_nrev, atomic(i * 20), solution).call();
			while (s.next()) {
				System.out.println(i + " => " + solution.toProlog());
			}
		}

		for (int i = 1; i <= 20; i++) {
			Variable solution = new Variable();
			StringAtom run_nrev = StringAtom.get("run_nrev");
			Goal g = compoundTerm(run_nrev, atomic(2000), solution).call();
			long startTime = System.nanoTime();
			if (!(g.next() && !g.next())) {
				throw new Error("internal programming error");
			}
			long duration = System.nanoTime() - startTime;
			Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
			System.out.printf("Finished in %10.4f secs.\n", time);
			Utils.writeToPerformanceLog("nrev run " + i
					+ " (find all solutions) finished in: " + time + "\n");
		}
	}

}
