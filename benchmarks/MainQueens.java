//package predicates;

import predicates.not_attack2;
import predicates.not_attack3;
import predicates.queens2;
import predicates.queens3;
import predicates.range3;
import predicates.select3;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.StringAtom;
import saere.Variable;
import static saere.term.TermFactory.*;

public class MainQueens {

	public static void main(String[] args) throws Exception {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		not_attack2.registerWithPredicateRegistry(registry);
		not_attack3.registerWithPredicateRegistry(registry);
		queens2.registerWithPredicateRegistry(registry);
		queens3.registerWithPredicateRegistry(registry);
		range3.registerWithPredicateRegistry(registry);
		select3.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...");
		for (int i = 1; i <= 18; i++) {
			Variable solution = new Variable();
			StringAtom time = StringAtom.instance("time");
			StringAtom queens = StringAtom.instance("queens");
			Solutions s = compoundTerm(time,
					compoundTerm(queens, atomic(i), solution)).call();
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
					StringAtom time = StringAtom.instance("time");
					StringAtom queens = StringAtom.instance("queens");
					Solutions s = compoundTerm(time,
							compoundTerm(queens, atomic(i), solution)).call();
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
