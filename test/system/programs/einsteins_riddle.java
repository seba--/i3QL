//package predicates;

import static saere.term.Terms.compoundTerm;
import predicates.einstein2Factory;
import predicates.nextTo3Factory;
import predicates.rightTo3Factory;
import saere.PredicateRegistry;
import saere.Goal;
import saere.StringAtom;
import saere.Variable;

public class einsteins_riddle {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		einstein2Factory.registerWithPredicateRegistry(registry);
		nextTo3Factory.registerWithPredicateRegistry(registry);
		rightTo3Factory.registerWithPredicateRegistry(registry);
	}
	
	public static void main(String[] args) throws Throwable {

	

		{
			System.out.println("Warm up...");
			for (int i = 0; i < 50; i++) {
				// StringAtom time = StringAtom.get("time");
				Variable houses = new Variable();
				Variable fishOwner = new Variable();

				StringAtom einstein = StringAtom.get("einstein");
				Goal s = // compoundTerm(time,
				compoundTerm(einstein, houses, fishOwner)
				// )
						.call();
				if (!s.next()) {
					throw new Error();
				}
				// else {
				// System.out.println(" houses=" + houses.toProlog()
				// + "fishOwner=" + fishOwner.toProlog());
				// }
				System.out.print(".");
			}
			System.out.println();
		}

		// System.out.println("Sleeping for five seconds...");
		// Thread.sleep(5000);
		Thread t = new Thread(new Runnable() {
			public void run() {

				System.out.println("Evaluating...");
				long startTime = System.nanoTime();
				for (int i = 0; i < 20; i++) {

					StringAtom time = StringAtom.get("time");
					Variable houses = new Variable();
					Variable fishOwner = new Variable();

					StringAtom einstein = StringAtom.get("einstein");
					Goal s = compoundTerm(time, compoundTerm(einstein, houses, fishOwner)).call();
					if (s.next()) {
						System.out.println(" houses=" + houses.toProlog() + "fishOwner="
								+ fishOwner.toProlog());
					}
				}
				long duration = System.nanoTime() - startTime;
				double time = duration / 1000.0d / 1000.0d / 1000.0d;
				System.out.println("Finished in " + time + "seconds");
				Utils.writeToPerformanceLog("einsteins riddle finished in: " + time + "\n");
			}
		});
		t.start();
		t.join();
	}

}
