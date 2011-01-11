import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.crypt1Factory;
import predicates.run_crypt1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Variable;

public class MainCrypt {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		run_crypt1Factory.registerWithPredicateRegistry(registry);
		crypt1Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		{
			Variable solution = new Variable();
			StringAtom run = StringAtom.get("crypt");
			Goal s = compoundTerm(run, solution).call();
			while (s.next()) {
				System.out.println(solution.toProlog());
			}
		}

		for (int i = 1; i <= 20; i++) {
			final int NUMBER_OF_TIMES = 500;
			StringAtom run = StringAtom.get("run_crypt");
			Goal g = compoundTerm(run, atomic(NUMBER_OF_TIMES)).call();
			long startTime = System.nanoTime();
			if (!(g.next() && !g.next())) {
				throw new Error("internal programming error");
			}
			long duration = System.nanoTime() - startTime;
			Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
			System.out.printf("Finished in %10.4f secs.\n", time);
			All.writeToPerformanceLog("executing crypt "+NUMBER_OF_TIMES+" times " + i + " (find all solutions) finished in: "
					+ time + "\n");
		}
	}

}
