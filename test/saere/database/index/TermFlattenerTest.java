package saere.database.index;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.meta.GenericCompoundTerm;

/**
 * Test for the {@link TermFlattener}s ({@link ShallowFlattener} and 
 * {@link FullFlattener}).
 * 
 * @author David Sullivan
 * @version 0.1, 10/17/2010
 */
public class TermFlattenerTest {
	
	private static final TermFlattener SHALLOW = new ShallowFlattener();
	private static final TermFlattener RECURSIVE = new FullFlattener();
	
	private static final StringAtom F = StringAtom.StringAtom("f");
	private static final StringAtom A = StringAtom.StringAtom("a");
	private static final IntegerAtom I1 = IntegerAtom.IntegerAtom(1);
	private static final StringAtom C = StringAtom.StringAtom("c");
	
	// a(1)
	private static final Term A1 = new GenericCompoundTerm(A, new Term [] { I1 });
	
	// f(a, 1, c)
	private static final Term FA1C_FLAT = new GenericCompoundTerm(F, new Term[] { A, I1, C});
	
	// f(a(1), c)
	private static final Term FA1C_COMP = new GenericCompoundTerm(F, new Term[] { A1, C });
	
	@BeforeClass
	public static void initialize() {
		
	}
	
	@Test
	public void testShallow0a() {
		Term[] expecteds = new Term[] { F };
		Term[] actuals = SHALLOW.flatten(F);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testShallow0b() {
		Term[] expecteds = new Term[] { I1 };
		Term[] actuals = SHALLOW.flatten(I1);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testShallow0c() {
		Term[] expecteds = new Term[] { A, I1 };
		Term[] actuals = SHALLOW.flatten(A1);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testRecursive0a() {
		Term[] expecteds = new Term[] { F };
		Term[] actuals = SHALLOW.flatten(F);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testRecursive0b() {
		Term[] expecteds = new Term[] { I1 };
		Term[] actuals = SHALLOW.flatten(I1);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testRecursive0c() {
		Term[] expecteds = new Term[] { A, I1 };
		Term[] actuals = SHALLOW.flatten(A1);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testShallow1() {
		Term[] expecteds = new Term[] { F, A, I1, C};
		Term[] actuals = SHALLOW.flatten(FA1C_FLAT);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testShallow2() {
		Term[] expecteds = new Term[] { F, A, C};
		Term[] actuals = SHALLOW.flatten(FA1C_COMP);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testRecursive1() {
		Term[] expecteds = new Term[] { F, A, I1, C};
		Term[] actuals = RECURSIVE.flatten(FA1C_FLAT);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void testRecursive2() {
		Term[] expecteds = new Term[] { F, A, I1, C};
		Term[] actuals = RECURSIVE.flatten(FA1C_COMP);
		assertArrayEquals(expecteds, actuals);
	}
}
