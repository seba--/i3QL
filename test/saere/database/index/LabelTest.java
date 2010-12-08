package saere.database.index;

import static org.junit.Assert.*;
import static saere.database.DatabaseTermFactory.*;

import java.util.IdentityHashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import saere.StringAtom;

/**
 * To test {@link AtomLabel}s and {@link FunctorLabel}s.
 * 
 * @author David Sullivan
 * @version 0.2, 12/6/2010
 */
public class LabelTest {
	
	private static SimpleLabel a;
	private static SimpleLabel a2;
	private static SimpleLabel i2;
	private static SimpleLabel i127;
	
	@BeforeClass
	public static void initialize() {
		a = AtomLabel.AtomLabel(sa("a"));
		a2 = FunctorLabel.FunctorLabel(sa("a"), 2);
		i2 = AtomLabel.AtomLabel(ia(2));
		i127 = AtomLabel.AtomLabel(ia(127));
	}
	
	@Test
	public void test1() {
		Label l = AtomLabel.AtomLabel(StringAtom.StringAtom("a"));
		assertTrue(l == a);
	}
	
	@Test
	public void test2() {
		Label l = AtomLabel.AtomLabel(StringAtom.StringAtom("b"));
		assertFalse(l == a);
	}
	
	@Test
	public void test3() {
		Label l = FunctorLabel.FunctorLabel(StringAtom.StringAtom("a"), 2);
		assertTrue(l == a2);
	}
	
	@Test
	public void test4() {
		Label l = FunctorLabel.FunctorLabel(StringAtom.StringAtom("b"), 2);
		assertFalse(l == a2);
	}
	
	// ...
	
	@Test
	public void test7() {
		Label l = AtomLabel.AtomLabel(ia(2));
		assertTrue(l == i2);
	}
	
	@Test
	public void test8() {
		Label l = AtomLabel.AtomLabel(ia(1));
		assertFalse(l == i2);
	}
	
	@Test
	public void test9() {
		IdentityHashMap<Label, Integer> map = new IdentityHashMap<Label, Integer>();
		Label l = AtomLabel.AtomLabel(ia(2));
		map.put(l, 2);
		assertTrue(map.get(i2) == 2);
	}
	
	@Test
	public void test10() {
		Label l = AtomLabel.AtomLabel(ia(127));
		System.out.println(l.hashCode());
		System.out.println(i127.hashCode());
		assertTrue(l == i127);
	}
	
	@Test
	public void test11() {
		IdentityHashMap<Label, Integer> map = new IdentityHashMap<Label, Integer>();
		Label l = AtomLabel.AtomLabel(ia(127));
		map.put(l, 7);
		assertTrue(map.get(i127) == 7);
	}
}
