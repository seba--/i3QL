package saere.database.index;

import junit.framework.TestCase;

import org.junit.Test;

import saere.IntegerAtom;
import saere.StringAtom;
import saere.Term;
import saere.Variable;

/**
 * Tests for the {@link Matcher} class.
 * 
 * @author David Sullivan
 * @version 0.1, 10/6/2010
 */
public class MatcherTest extends TestCase {
	
	int expected, actual;
	Term X, invoke, instr, i0, i1, m_1, m_2, anything;
	Term[] term0, term1, query0, query1, query2, query3;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		X = new Variable();
		invoke = StringAtom.StringAtom("invoke");
		instr = StringAtom.StringAtom("instr");
		i0 = IntegerAtom.IntegerAtom(0);
		i1 = IntegerAtom.IntegerAtom(1);
		m_1 = StringAtom.StringAtom("m_1");
		m_2 = StringAtom.StringAtom("m_2");
		anything = StringAtom.StringAtom("__anything__"); // = ?
		
		term0 = new Term[] { instr, m_1, i0, anything };
		term1 = new Term[] { instr, m_2, i1, invoke };
		
		query0 = new Term[] { instr };
		query1 = new Term[] { instr, X, X, X };
		query2 = new Term[] { X, m_1 };
		query3 = new Term[] { X, X, X, invoke };
	}
	
	@Test
	public void testMatch1() {
		assertTrue(Matcher.match(X, X)); // test takes rather long if both are variables!
	}
	
	@Test
	public void testMatch2a() {
		assertTrue(Matcher.match(X, invoke));
	}
	
	@Test
	public void testMatch2b() {
		assertTrue(Matcher.match(invoke, X));
	}
	
	@Test
	public void testMatch3a() {
		assertTrue(Matcher.match(X, i0));
	}
	
	@Test
	public void testMatch3b() {
		assertTrue(Matcher.match(i0, X));
	}
	
	public void testMatch4() {
		assertTrue(Matcher.match(i0, i0));
	}
	
	public void testMatch5() {
		assertTrue(Matcher.match(instr, instr));
	}
	
	public void testMatch6a() {
		assertFalse(Matcher.match(i0, i1));
	}
	
	public void testMatch6b() {
		assertFalse(Matcher.match(i1, i0));
	}
	
	public void testMatch7a() {
		assertFalse(Matcher.match(instr, invoke));
	}
	
	public void testMatch7b() {
		assertFalse(Matcher.match(invoke, instr));
	}
	
	/**
	 * instr(m_1, 0, ?) has to match [instr] with 1.
	 */
	@Test
	public void testMatchArrays1() {
		expected = 1;
		actual = Matcher.match(term0, query0);
		assertEquals(expected, actual);
	}
	
	/**
	 * instr(m_1, 0, ?) has to match [instr, X, X, X] with 4.
	 */
	@Test
	public void testMatchArrays2() {
		expected = 4;
		actual = Matcher.match(term0, query1);
		assertEquals(expected, actual);
	}
	
	/**
	 * instr(m_1, 0, ?) has to match [X, m_1] with 2.
	 */
	@Test
	public void testMatchArrays3() {
		expected = 2;
		actual = Matcher.match(term0, query2);
		assertEquals(expected, actual);
	}
	
	/**
	 * instr(m_2, 1, invoke) has to match [X, m_1] with 1.
	 */
	@Test
	public void testMatchArrays4() {
		expected = 1;
		actual = Matcher.match(term1, query2);
		assertEquals(expected, actual);
	}
	
	/**
	 * instr(m_2, 1, invoke) has to match [X, X, X, invoke] with 4.
	 */
	@Test
	public void testMatchArrays5() {
		expected = 4;
		actual = Matcher.match(term1, query3);
		assertEquals(expected, actual);
	}
}
