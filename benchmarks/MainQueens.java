//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.complexTerm;
import predicates.not_attack2Factory;
import predicates.not_attack3Factory;
import predicates.queens2Factory;
import predicates.queens3Factory;
import predicates.range3Factory;
import predicates.select3Factory;
import saere.PredicateRegistry;
import saere.Goal;
import saere.StringAtom;
import saere.Variable;

public class MainQueens {

	public static void main(String[] args) throws Exception {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		not_attack2Factory.registerWithPredicateRegistry(registry);
		not_attack3Factory.registerWithPredicateRegistry(registry);
		queens2Factory.registerWithPredicateRegistry(registry);
		queens3Factory.registerWithPredicateRegistry(registry);
		range3Factory.registerWithPredicateRegistry(registry);
		select3Factory.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...");
		for (int i = 1; i <= 18; i++) {
			Variable solution = new Variable();
			StringAtom time = StringAtom.get("time");
			StringAtom queens = StringAtom.get("queens");
			Goal s = complexTerm(time,
					complexTerm(queens, atomic(i), solution)).call();
			if (s.next()) {
				System.out.println(" ; " + i + " => " + solution.toProlog());
			} else {
				System.out.println();
			}
			
		}

		System.out
				.println("Waiting for five seconds to enable the attachement of a profiler");
		Thread.sleep(5000);

		Thread t = new Thread(new Runnable() {
			public void run() {

				long startTime = System.nanoTime();
				for (int i = 1; i <= 25; i++) {
					Variable solution = new Variable();
					StringAtom time = StringAtom.get("time");
					StringAtom queens = StringAtom.get("queens");
					Goal s = complexTerm(time,
							complexTerm(queens, atomic(i), solution)).call();
					if (s.next()) {
						System.out.println(" ; " + i + " => "
								+ solution.toProlog());
					} else {
						System.out.println();
					}
				}
				long duration = System.nanoTime() - startTime;
				System.out.printf("%10.4f", new Double(
						duration / 1000.0 / 1000.0 / 1000.0));
			}
		});
		t.start();
		t.join();
	}

}
