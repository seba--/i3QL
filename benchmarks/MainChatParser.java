//package predicates;

import static saere.term.Terms.and;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;
import predicates.determinate_say2Factory;
import predicates.input1Factory;
import saere.Goal;
import saere.PredicateRegistry;
import saere.Term;
import saere.Variable;

public class MainChatParser {

	public static void main(String[] args) throws Throwable {

		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		input1Factory.registerWithPredicateRegistry(registry);
		determinate_say2Factory.registerWithPredicateRegistry(registry);

		System.out.println("Warm up...(will take ~1 Minute)");
		{
			long startTime = System.nanoTime();
			do{
				Goal g;
				Variable Input = new Variable();
				Variable Answer = new Variable();
				Term term = and(compoundTerm(atomic("input"), Input),
						compoundTerm(atomic("determinate_say"), Input, Answer));
				g = term.call();
				int count = 0;
				while (g.next()) {
					count++;
				}
				if (count < 10)
					throw new Error("internal programming error");
			} while ((System.nanoTime() - startTime) < 60l*1000l*1000l*1000l);
			long duration = System.nanoTime() - startTime;
			System.out.println("Finished in " + duration / 1000.0 / 1000.0
					/ 1000.0 + "seconds");
		}

		System.out.println("Sleeping for five seconds...");
		Thread.sleep(5000);
		Runnable r = new Runnable() {
			public void run() {
				long startTime = System.nanoTime();
				Goal g;
				Variable Input = new Variable();
				Variable Answer = new Variable();
				Term term = compoundTerm(
						atomic("time"),
						and(compoundTerm(atomic("input"), Input),
								compoundTerm(atomic("determinate_say"), Input,
										Answer)));
				g = term.call();
				int count = 0;
				while (g.next()) {
					count++;
					System.out.println(" ; " + Input.toProlog() + " => "
							+ Answer.toProlog());
				}
				System.out.println(" ; no more solutions.");
				if (count < 10)
					throw new Error("internal programming error");
				long duration = System.nanoTime() - startTime;
				System.out.println("Finished in " + duration / 1000.0 / 1000.0
						/ 1000.0 + "seconds");
			}
		};
		Thread t1 = new Thread(r);
		t1.start();
		t1.join();
	}

}
