//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.hanoi5Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Variable;

public class MainHanoi {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		hanoi5Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Exception {

		System.out.println("Warm up...");
		for (int i = 1; i <= 13; i++) {
			StringAtom time = StringAtom.get("time");
			Variable a = new Variable();
			Variable b = new Variable();
			Variable c = new Variable();
			Variable solution = new Variable();
			Goal s = compoundTerm(time,
					compoundTerm(atomic("hanoi"), atomic(i), a, b, c, solution))
					.call();
			if (s.next()) {
				System.out.println(i + " => " + solution.toProlog() + "\n");
			} else {
				System.out.println();
			}
		}

		// System.out.println("Waiting for five seconds...");
		// Thread.sleep(5000);

		Thread t = new Thread(new Runnable() {
			public void run() {

				long duration = 0l;
				for (int i = 1; i <= 14; i++) { // TODO Figure out the maximum
												// size of towers...
					StringAtom time = StringAtom.get("time");
					Variable a = new Variable();
					Variable b = new Variable();
					Variable c = new Variable();
					Variable solution = new Variable();
					Goal s = compoundTerm(
							time,
							compoundTerm(atomic("hanoi"), atomic(i), a, b, c,
									solution)).call();
					long startTime = System.nanoTime();
					s.next();
					duration += System.nanoTime() - startTime;
					if (i <= 13) {
						System.out.println(" ; " + i + " => "
								+ solution.toProlog());
					} else {
						System.out.println(" ; " + i + " => many...");
					}

				}
				Double time = new Double(duration / 1000.0 / 1000.0 / 1000.0);
				System.out.printf("%7.4f", time);

				All.writeToPerformanceLog("hanoi finished in: " + time + "\n");
			}
		});
		t.start();
		t.join();
	}

}
