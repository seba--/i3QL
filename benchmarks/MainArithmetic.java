//package predicates;

import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.factorial2Factory;
import predicates.num_to_binary2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Variable;

public class MainArithmetic {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		num_to_binary2Factory.registerWithPredicateRegistry(registry);
		factorial2Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		long duration = 0l;
		for (int i = 1; i <= 20; i++) {
			Variable binary = new Variable();
			Variable solution = new Variable();
			Goal s = and(compoundTerm(atomic("num_to_binary"), atomic(14), binary),
					compoundTerm(atomic("factorial"), binary, solution)).call();
			long startTime = System.nanoTime();
			boolean succeeded = s.next();
			long last_duration = System.nanoTime() - startTime;

			All.writeToPerformanceLog("arithmetic finished in: "
					+ new Double(last_duration / 1000.0 / 1000.0 / 1000.0) + "\n");
			duration += last_duration;
			if (succeeded) {
				System.out.println(i + " => " + solution.toProlog() + "\n");
			} else {
				System.out.println();
			}
		}
		Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
		System.out.printf("Finished in %10.4f secs.\n", time);
	}

}
