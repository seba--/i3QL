package saere.database.index;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;

/**
 * Test for {@link QueryStack}.
 * 
 * @author David Sullivan
 * @version 0.1, 10/14/2010
 */
public class TermStackTest {
	
	private static final Term A = StringAtom.StringAtom("a");
	private static final Term B = StringAtom.StringAtom("b");
	private static final Term C = StringAtom.StringAtom("c");
	private static final Term I1 = IntegerAtom.IntegerAtom(1);
	private static final Term I2 = IntegerAtom.IntegerAtom(2);
	private static final Term I3 = IntegerAtom.IntegerAtom(3);
	
	private QueryStack stack;
	private Term[] expecteds;
	private Term[] actuals;
	private Term expected;
	private Term actual;
	
	@Before
	public void initialize() {
		stack = new QueryStack(new Term[] { A, B, C, I1, I2, I3 });
	}
	
	@Test
	public void test1() {
		stack.pop();
		expecteds = new Term[] { B, C, I1, I2, I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test2() {
		stack.pop(2);
		expecteds = new Term[] {C, I1, I2, I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test3() {
		stack.pop(2);
		expected = I1;
		actual = stack.peek(2);
		assertEquals(expected, actual);
	}
	
	@Test
	public void test3b() {
		stack.pop(2);
		stack.peek(2); // should not change the stack
		expecteds = new Term[] {C, I1, I2, I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test4() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		expecteds = new Term[] { I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test5() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		expecteds = new Term[] { I1, I2, I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test6() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		stack.back(10);
		expecteds = new Term[] { A, B, C, I1, I2, I3 };
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test7() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		stack.back(10);
		stack.pop(10);
		expecteds = new Term[] {};
		actuals = stack.asArray();
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test8() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		stack.back(10);
		stack.pop(10);
		expected = null;
		actual = stack.peek(7);
		assertEquals(expected, actual);
	}
}
