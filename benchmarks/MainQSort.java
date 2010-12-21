//package predicates;

import static saere.term.TermFactory.*;
import predicates.list1;
import predicates.partition4;
import predicates.qsort3;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

public class MainQSort {

    public static void main(String[] args) throws Throwable {

	PredicateRegistry registry = PredicateRegistry.predicateRegistry();
	qsort3.registerWithPredicateRegistry(registry);
	partition4.registerWithPredicateRegistry(registry);
	list1.registerWithPredicateRegistry(registry);

	System.out.println("Warm up...");
	long startTime = System.nanoTime();
	for (int i = 0; i < 10000; i++) {

	    Variable list = new Variable();
	    Variable result = new Variable();
	    Term t = and(
		    compoundTerm(StringAtom.instance("list"), list),
		    compoundTerm(StringAtom.instance("qsort"), list, result,
			    StringAtom.EMPTY_LIST_FUNCTOR));
	    Solutions s = t.call();
	    if (!s.next()) {
		throw new Error("internal programming error");
	    }
	}
	long duration = System.nanoTime() - startTime;
	System.out.println("Finished in " + duration / 1000.0 / 1000.0 / 1000.0 + "seconds");

	System.out.println("Sleeping for five seconds, to enable the attachement of profilers.");
	Thread.sleep(5000);
	Thread t = new Thread(new Runnable() {
	    public void run() {

		System.out.println("Evaluating... (sorting 50 values, 1000 times)");
		long startTime = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
		    Variable list = new Variable();
		    Variable result = new Variable();
		    Term t = and(
			    compoundTerm(StringAtom.instance("list"), list),
			    compoundTerm(StringAtom.instance("qsort"), list, result,
				    StringAtom.EMPTY_LIST_FUNCTOR));
		    Solutions s = t.call();
		    if (!s.next()) {
			throw new Error("internal programming error");
		    }
		}
		long duration = System.nanoTime() - startTime;
		System.out
			.println("Finished in " + duration / 1000.0 / 1000.0 / 1000.0 + "seconds");
	    }
	});
	t.start();
	t.join();
    }

}
