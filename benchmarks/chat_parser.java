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

public class chat_parser {

	static {
		PredicateRegistry registry = PredicateRegistry.predicateRegistry();
		input1Factory.registerWithPredicateRegistry(registry);
		determinate_say2Factory.registerWithPredicateRegistry(registry);
	}

	public static void main(String[] args) {

		{
			for (int i = 0; i < 100; i++) {
				
				Goal g;
				Variable Input = new Variable();
				Variable Answer = new Variable();
				Term term = and(compoundTerm(atomic("input"), Input),
						compoundTerm(atomic("determinate_say"), Input, Answer));
				g = term.call();
				long startTime = System.nanoTime();
				while (g.next()) { /*nothing to do*/ }
				long duration = System.nanoTime() - startTime;

				Utils.writeToPerformanceLog("run \t"+i+"\t chat_parser (findall) finished in: \t" + duration/1000 + "\t microsecs.\n");
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
			Utils.writeToPerformanceLog("chat_parser finished in: " + time + "\n");

		}

	}

}
