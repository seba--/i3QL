package saere.predicate;

import org.junit.Assert;
import org.junit.Test;

import saere.GenericCompoundTerm;
import saere.Solutions;
import saere.Term;
import saere.Variable;
import static saere.StringAtom.StringAtom;
import static saere.IntegerAtom.IntegerAtom;

public class And2Test {

	
	@Test
	public void testDeterministicEvaluation(){
		
		PredicateRegistry instance = PredicateRegistry.instance();
		And2.registerWithPredicateRegistry(instance);
		Write1.registerWithPredicateRegistry(instance);
		Unify2.registerWithPredicateRegistry(instance);
		
		Variable v = new Variable();
		Term t1 = new GenericCompoundTerm(StringAtom("="), new Term[]{v,IntegerAtom(1)});
		Term t2 = new GenericCompoundTerm(StringAtom("write"),new Term[]{v} );
		Solutions solutions = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t2}).call();
		int solutionsCount = 0;
		while (solutions.next()) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
	
	@Test
	public void testNonDeterministicEvaluation(){
		
		PredicateRegistry instance = PredicateRegistry.instance();
		Repeat0.registerWithPredicateRegistry(instance);
		
		
		Term t1 = new GenericCompoundTerm(StringAtom("repeat"), Term.NO_TERMS);
		Term t2 = new GenericCompoundTerm(StringAtom("write"),new Term[]{IntegerAtom(1)} );
		Solutions solutions = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t2}).call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1000,solutionsCount);
	}
	
}
