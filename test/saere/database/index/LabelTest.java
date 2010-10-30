package saere.database.index;

import junit.framework.TestCase;

import org.junit.Test;

import saere.Atom;
import saere.StringAtom;
import saere.Term;

/**
 * Tests for the abstract {@link Label} class and the two implementing classes 
 * {@link AtomLabel} and {@link CompoundLabel}.
 * 
 * @author David Sullivan
 * @version 0.1, 10/6/2010
 */
public class LabelTest extends TestCase {

	private static final Atom F = StringAtom.StringAtom("f");
	private static final Atom A = StringAtom.StringAtom("a");
	private static final Atom B = StringAtom.StringAtom("b");
	private static final Atom C = StringAtom.StringAtom("c");
	
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
		
		fabc = new CompoundLabel(new Atom[] { F, A, B, C });
		bc = fabc.split(1);
		fa = fabc;
		
		fabc = new CompoundLabel(new Atom[] { F, A, B, C });
		abc = fabc.split(0);
		f = fabc;

		fabc = new CompoundLabel(new Atom[] { F, A, B, C });
		c = fabc.split(2);
	
		fabc = new CompoundLabel(new Atom[] { F, A, B, C });
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
