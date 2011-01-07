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

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		input1Factory.registerWithPredicateRegistry(registry);
		determinate_say2Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) throws Throwable {

		{
			for (int i = 0; i < 20; i++) {
				long startTime = System.nanoTime();
				Goal g;
				Variable Input = new Variable();
				Variable Answer = new Variable();
				Term term = and(compoundTerm(atomic("input"), Input),
						compoundTerm(atomic("determinate_say"), Input, Answer));
				g = term.call();
				do { /* nothing to do */
				} while (g.next());

				long duration = System.nanoTime() - startTime;
				System.out.println("Finished in " + duration / 1000.0 / 1000.0 / 1000.0
						+ "seconds");
			}
		}

		{
			long startTime = System.nanoTime();
			Goal g;
			Variable Input = new Variable();
			Variable Answer = new Variable();
			Term term = compoundTerm(
					atomic("time"),
					and(compoundTerm(atomic("input"), Input),
							compoundTerm(atomic("determinate_say"), Input, Answer)));
			g = term.call();
			int count = 0;
			while (g.next()) {
				count++;
				System.out.println(" ; " + Input.toProlog() + " => " + Answer.toProlog());
			}
			System.out.println(" ; no more solutions.");

			long duration = System.nanoTime() - startTime;
			double time = duration / 1000.0d / 1000.0d / 1000.0d;
			System.out.println("Finished in " + time + "seconds");
			All.writeToPerformanceLog("chat_parser finished in: " + time + "\n");

		}

	}

}
