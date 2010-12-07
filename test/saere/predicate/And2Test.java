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
		
		// v = 1, true.
		
		Variable v = new Variable();
		Term t1 = new GenericCompoundTerm(StringAtom("="), new Term[]{v,IntegerAtom(1)});
		Term t2 = new GenericCompoundTerm(StringAtom("true"),Term.NO_TERMS );
		Solutions solutions = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t2}).call();
		int solutionsCount = 0;
		while (solutions.next()) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
	
	@Test
	public void testNonDeterministicEvaluation(){

		// repeat,true.
		
		Term t1 = new GenericCompoundTerm(StringAtom("repeat"), Term.NO_TERMS);
		Term t2 = new GenericCompoundTerm(StringAtom("true"),Term.NO_TERMS );
		Solutions solutions = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t2}).call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1000,solutionsCount);
	}
	
	
	@Test
	public void testCutREvaluation(){
		// (repeat,true),!.
		
		Term t1 = new GenericCompoundTerm(StringAtom("repeat"), Term.NO_TERMS);
		Term t2 = new GenericCompoundTerm(StringAtom("true"),Term.NO_TERMS);
		Term t3 = new GenericCompoundTerm(StringAtom("!"),Term.NO_TERMS );
		Term t4 = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t2});
		Term t5 = new GenericCompoundTerm(StringAtom(","), new Term[]{t4,t3});
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
	
	@Test
	public void testCutLEvaluation(){
			
		// (repeat,!),true.
		Term t1 = new GenericCompoundTerm(StringAtom("repeat"), Term.NO_TERMS);
		Term t2 = new GenericCompoundTerm(StringAtom("true"),Term.NO_TERMS );
		Term t3 = new GenericCompoundTerm(StringAtom("!"),Term.NO_TERMS );
		Term t4 = new GenericCompoundTerm(StringAtom(","), new Term[]{t1,t3});
		Term t5 = new GenericCompoundTerm(StringAtom(","), new Term[]{t4,t2});
		Solutions solutions = t5.call();
		int solutionsCount = 0;
		while (solutions.next() && solutionsCount < 1000) {
			solutionsCount ++;
		}
		Assert.assertEquals(1,solutionsCount);
	}
	
}
