//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import static saere.term.Terms.delimitedList;
import predicates.interpret1Factory;
import predicates.meta_nrev_range2Factory;
import predicates.range3Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Variable;

public class MainMetaNRev {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		meta_nrev_range2Factory.registerWithPredicateRegistry(registry);
		interpret1Factory.registerWithPredicateRegistry(registry);
		range3Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Throwable {

		long duration = 0l;
		for (int i = 1; i <= 20; i++) {
			
			long startTime = System.nanoTime();
			Variable binary = new Variable();
			Variable solution = new Variable();			
			Goal g1 =
					compoundTerm(atomic("meta_nrev_range"), atomic(100),
							binary).call();
			g1.next();
			System.out.println(binary.toProlog());
			Goal g2 = 
					compoundTerm(
							atomic("interpret"),
							delimitedList(compoundTerm(atomic("nrev"), binary,
									solution))).call();
			boolean succeeded = g2.next();
			long last_duration = System.nanoTime() - startTime;

			All.writeToPerformanceLog("meta_nrev finished in: "
					+ new Double(last_duration / 1000.0 / 1000.0 / 1000.0)
					+ "\n");
			duration += last_duration;
			if (succeeded) {
				System.out.println(i + " => " + solution.toProlog());
			} else {
				throw new Error();
			}
		}
		Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
		System.out.printf("Finished in %10.4f secs.\n", time);
	}

}
