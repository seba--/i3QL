//package predicates;

import static saere.term.Terms.and;
import static saere.term.Terms.complexTerm;
import predicates.list1Factory;
import predicates.partition4Factory;
import predicates.qsort3Factory;
import saere.PredicateRegistry;
import saere.Goal;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class MainQSort {

	public static void main(String[] args) throws Throwable {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		qsort3Factory.registerWithPredicateRegistry(registry);
		partition4Factory.registerWithPredicateRegistry(registry);
		list1Factory.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...");
		{
			long startTime = System.nanoTime();
			for (int i = 0; i < 10000; i++) {

				Variable list = new Variable();
				Variable result = new Variable();
				Term t = and(
						complexTerm(StringAtom.get("list"), list),
						complexTerm(StringAtom.get("qsort"), list, result,
								StringAtom.EMPTY_LIST));
				Goal s = t.call();
				if (!s.next()) {
					throw new Error("internal programming error");
				}
			}
			long duration = System.nanoTime() - startTime;
			System.out.println("Finished in " + duration / 1000.0 / 1000.0
					/ 1000.0 + "seconds");
		}

		System.out.println("Sleeping for five seconds...");
		Thread.sleep(5000);
		Thread t = new Thread(new Runnable() {
			public void run() {

				System.out
						.println("Evaluating... (sorting 50 values, 1000 times)");
				long startTime = System.nanoTime();
				for (int i = 0; i < 1000; i++) {
					Variable list = new Variable();
					Variable result = new Variable();
					Term term = and(
							complexTerm(StringAtom.get("list"), list),
							complexTerm(StringAtom.get("qsort"), list, result,
									StringAtom.EMPTY_LIST));
					Goal s = term.call();
					if (!s.next()) {
						throw new Error("internal programming error");
					}
					System.out.println(result.toProlog());
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
