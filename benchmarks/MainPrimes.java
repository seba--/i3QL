//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.complexTerm;
import predicates.integers3Factory;
import predicates.primes2Factory;
import predicates.remove3Factory;
import predicates.sift2Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Variable;

public class MainPrimes {

	public static void main(String[] args) throws Exception {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		primes2Factory.registerWithPredicateRegistry(registry);
		integers3Factory.registerWithPredicateRegistry(registry);
		sift2Factory.registerWithPredicateRegistry(registry);
		remove3Factory.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...");
		for (int i = 1; i <= 75; i++) {
			Variable solution = new Variable();
			Goal s = complexTerm(atomic("primes"),atomic(1000),solution).call();
			if (!s.next()) {
				throw new Error("Evaluation failed.");
			}
		}

		System.out
				.println("Waiting for five seconds to enable the attachement of a profiler");
		Thread.sleep(5000);

		Thread t = new Thread(new Runnable() {
			public void run() {

				long startTime = System.nanoTime();
				for (int i = 1; i <= 10; i++) {
					Variable solution = new Variable();
					Goal s = complexTerm(atomic("time"),complexTerm(atomic("primes"),atomic(1000),solution)).call();
					if (s.next()) {
						System.out.println(" ; " + i + " => "
								+ solution.toProlog());
					} else {
						throw new Error("Evaluation failed.");
					}		
				}
				long duration = System.nanoTime() - startTime;
				System.out.printf("%7.4f", new Double(
						duration / 1000.0 / 1000.0 / 1000.0));
			}
		});
		t.start();
		t.join();
	}

}
