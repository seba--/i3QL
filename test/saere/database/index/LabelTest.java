package saere.database.index;

import junit.framework.TestCase;

import org.junit.Test;

import saere.Term;
import saere.database.DatabaseTermFactory;

/**
 * Tests for the abstract {@link Label} class and the two implementing classes 
 * {@link AtomLabel} and {@link CompoundLabel}.
 * 
 * @author David Sullivan
 * @version 0.1, 10/6/2010
 */
public class LabelTest extends TestCase {

	private static final Term F = DatabaseTermFactory.makeStringAtom("f");
	private static final Term A = DatabaseTermFactory.makeStringAtom("a");
	private static final Term B = DatabaseTermFactory.makeStringAtom("b");
	private static final Term C = DatabaseTermFactory.makeStringAtom("c");
	
	private Label fabc;
	private Label fa;
	private Label bc;
	private Label f;
	private Label abc;
	private Label c;
	
	private int expected;
	private int actual;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		fabc = Label.makeLabel(new Term[] { F, A, B, C });
		Label[] labels = fabc.split(1);
		fa = labels[0];
		bc = labels[1];
		labels = fabc.split(0);
		f = labels[0];
		abc = labels[1];
		labels = fabc.split(2);
		c = labels[1];
	}
	
	@Test
	public void testSplit1() {
		expected = 2;
		actual = Matcher.match(fabc.asArray(), fa.asArray());
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSplit2() {
		expected = 0;
		actual = Matcher.match(fabc.asArray(), bc.asArray());
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSplit3() {
		expected = 0;
		actual = Matcher.match(fabc.asArray(), bc.asArray());
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSplit4() {
		expected = 1;
		actual = Matcher.match(fabc.asArray(), f.asArray());
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSplit5() {
		expected = 1;
		actual = Matcher.match(c.asArray(), new Term[] { C });
		assertEquals(expected, actual);
	}
	
	@Test
	public void testSplit6() {
		expected = 3;
		actual = Matcher.match(abc.asArray(), new Term[] { A, B, C });
		assertEquals(expected, actual);
	}
}
