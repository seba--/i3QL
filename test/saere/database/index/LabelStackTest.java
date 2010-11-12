package saere.database.index;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static saere.database.DatabaseTermFactory.ia;
import static saere.database.DatabaseTermFactory.sa;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link QueryStack}.
 * 
 * @author David Sullivan
 * @version 0.2, 11/11/2010
 */
public class LabelStackTest {
	
	private static final Label A = AtomLabel.AtomLabel(sa("a"));
	private static final Label B = AtomLabel.AtomLabel(sa("b"));
	private static final Label C = AtomLabel.AtomLabel(sa("c"));
	private static final Label I1 = AtomLabel.AtomLabel(ia(1));
	private static final Label I2 = AtomLabel.AtomLabel(ia(2));
	private static final Label I3 = AtomLabel.AtomLabel(ia(3));
	
	private LabelStack stack;
	private Label[] expecteds;
	private Label[] actuals;
	private Label expected;
	private Label actual;
	
	/**
	 * Captures the current state of the stack as array.
	 * 
	 * @return The stack as array.
	 */
	// Ironically this method assumes that the stack works right...
	private Label[] asArray(LabelStack stack) {
		int size = stack.size();
		Label[] array = new Label[size];
		int i = 0;
		while (stack.peek() != null) {
			array[i++] = stack.pop();
		}
		stack.back(size);
		return array;
	}
	
	@Before
	public void initialize() {
		stack = new LabelStack(new Label[] { A, B, C, I1, I2, I3 });
	}
	
	@Test
	public void test1() {
		stack.pop();
		expecteds = new Label[] { B, C, I1, I2, I3 };
		actuals = asArray(stack);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test2() {
		stack.pop(2);
		expecteds = new Label[] {C, I1, I2, I3 };
		actuals = asArray(stack);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test3() {
		stack.pop(2);
		expected = C;
		actual = stack.peek();
		assertEquals(expected, actual);
	}
	
	@Test
	public void test3b() {
		stack.pop(2);
		stack.peek(2); // should not change the stack
		expecteds = new Label[] {C, I1, I2, I3 };
		actuals = asArray(stack);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test4() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		expecteds = new Label[] { I3 };
		actuals = asArray(stack);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test5() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		expecteds = new Label[] { I1, I2, I3 };
		actuals = asArray(stack);
		assertArrayEquals(expecteds, actuals);
	}
	
	@Test
	public void test6() {
		stack.pop(2);
		stack.peek(2);
		stack.pop(3);
		stack.back(2);
		stack.back(10);
		expecteds = new Label[] { A, B, C, I1, I2, I3 };
		actuals = asArray(stack);
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
		expecteds = new Label[] {};
		actuals = asArray(stack);
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
