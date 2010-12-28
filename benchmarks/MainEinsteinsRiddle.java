//package predicates;

import static saere.term.Terms.compoundTerm;
import predicates.einstein2Factory;
import predicates.nextTo3Factory;
import predicates.rightTo3Factory;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.StringAtom;
import saere.Variable;

public class MainEinsteinsRiddle {

	public static void main(String[] args) throws Throwable {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		einstein2Factory.registerWithPredicateRegistry(registry);
		nextTo3Factory.registerWithPredicateRegistry(registry);
		rightTo3Factory.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...");
		for (int i = 0; i < 3; i++) {
			StringAtom time = StringAtom.get("time");
			Variable houses = new Variable();
			Variable fishOwner = new Variable();

			StringAtom einstein = StringAtom.get("einstein");
			Solutions s = compoundTerm(time,
					compoundTerm(einstein, houses, fishOwner)).call();
			if (s.next()) {
				System.out.println(" houses=" + houses.toProlog()
						+ "fishOwner=" + fishOwner.toProlog());
			}
		}

		System.out
				.println("Sleeping for five seconds, to enable the attachement of profilers.");
		Thread.sleep(5000);
		Thread t = new Thread(new Runnable() {
			public void run() {

				System.out.println("Evaluating...");
				long startTime = System.nanoTime();
				for (int i = 0; i < 20; i++) {

					StringAtom time = StringAtom.get("time");
					Variable houses = new Variable();
					Variable fishOwner = new Variable();

					StringAtom einstein = StringAtom.get("einstein");
					Solutions s = compoundTerm(time,
							compoundTerm(einstein, houses, fishOwner)).call();
					if (s.next()) {
						System.out.println(" houses=" + houses.toProlog()
								+ "fishOwner=" + fishOwner.toProlog());
					}
				}
				long duration = System.nanoTime() - startTime;
				System.out.println("Finished in " + duration / 1000.0 / 1000.0
						/ 1000.0 + "seconds");
			}
		});
		t.start();
		t.join();
	}

}
