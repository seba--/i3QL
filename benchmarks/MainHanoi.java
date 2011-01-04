//package predicates;

import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.hanoi5Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.StringAtom;
import saere.Variable;

public class MainHanoi {

	public static void main(String[] args) throws Exception {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		hanoi5Factory.registerWithPredicateRegistry(registry);


		System.out.println("Warm up...");
		for (int i = 1; i <= 13; i++) {
			StringAtom time = StringAtom.get("time");
			Variable a = new Variable();
			Variable b = new Variable();
			Variable c = new Variable();
			Variable solution = new Variable();
			Goal s = compoundTerm(time,
					compoundTerm(atomic("hanoi"),atomic(i),a,b,c, solution)).call();
			if (s.next()) {
				System.out.println(" ; " + i + " => " + solution.toProlog());
			} else {
				System.out.println();
			}
		}

		System.out
				.println("Waiting for five seconds...");
		Thread.sleep(5000);

		Thread t = new Thread(new Runnable() {
			public void run() {

				long startTime = System.nanoTime();
				for (int i = 1; i <= 20; i++) { // TODO improve the SAE such, that we can solve the problem for larger towers
					StringAtom time = StringAtom.get("time");
					Variable a = new Variable();
					Variable b = new Variable();
					Variable c = new Variable();
					Variable solution = new Variable();
					Goal s = compoundTerm(time,
							compoundTerm(atomic("hanoi"),atomic(i),a,b,c, solution)).call();
					if (s.next()) {
						System.out.println(" ; " + i + " => " + solution.toProlog());
					} else {
						System.out.println();
					}
				}				long duration = System.nanoTime() - startTime;
				System.out.printf("%7.4f", new Double(
						duration / 1000.0 / 1000.0 / 1000.0));
			}
		});
		t.start();
		t.join();
	}

}
