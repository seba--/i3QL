package saere;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static saere.term.TermFactory.atomic;

import org.junit.Test;

import saere.term.ListElement2;

public class TestVariableBinding {

	@Test
	public void testNoBinding() {
		Variable v = new Variable();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

	@Test
	public void testBindingToNumber() {
		Variable v = new Variable();
		v.bind(atomic(1));
		assertTrue(v.isInstantiated());
		assertEquals(atomic(1), v.binding());
		assertEquals(atomic("1"), v.functor());
		assertEquals(0, v.arity());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

	@Test
	public void testBindingToStringAtom() {
		Variable v = new Variable();
		v.bind(atomic("test"));
		assertTrue(v.isInstantiated());
		assertEquals(atomic("test"), v.binding());
		assertEquals(atomic("test"), v.functor());
		assertEquals(0, v.arity());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

	@Test
	public void testBindingToCompoundTerm() {
		Variable v = new Variable();
		Variable l1 = new Variable();
		Variable l2 = new Variable();
		Term innerList = new ListElement2(StringAtom
				.instance("demo"), new ListElement2(l2,
						StringAtom.EMPTY_LIST_FUNCTOR));
		// [_,demo,_]
		v.bind(new ListElement2(l1,innerList ));
		assertTrue(v.isInstantiated());
		assertEquals(2, v.arity());
		assertEquals(StringAtom.LIST_FUNCTOR, v.functor());
		assertSame(l1, v.arg(0));
		assertSame(innerList, v.arg(1));
		// [_,demo|_]
		State s1 = v.manifestState();
		Variable tail = new Variable();

		assertTrue(new ListElement2(new Variable(), new ListElement2(StringAtom
				.instance("demo"), tail)).unify(v.binding()));
		assertEquals(new ListElement2(l2, StringAtom.EMPTY_LIST_FUNCTOR), tail
				.binding());
		v.setState(s1);

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

	
	
	
	
	
	
	
	
	@Test
	public void testNoBindingButSharing() {
		Variable v = new Variable();
		v.share(new Variable());
		assertFalse(v.isInstantiated());
		assertNull(v.binding());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}
	
	
	@Test
	public void testNoBindingButReverseSharing() {
		Variable v = new Variable();
		new Variable().share(v);
		assertFalse(v.isInstantiated());
		assertNull(v.binding());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}
	

	@Test
	public void testBindingToNumberWithSimpleSharing() {
		Variable v = new Variable();
		v.share(new Variable());
		v.bind(atomic(1));
		assertTrue(v.isInstantiated());
		assertEquals(atomic(1), v.binding());
		assertEquals(atomic("1"), v.functor());
		assertEquals(0, v.arity());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

	@Test
	public void testBindingToNumberWithLongChaingSharing() {
		Variable v = new Variable();
		Variable v1 = new Variable();
		Variable v2 = new Variable();
		Variable v3 = new Variable();
		v.share(v1);
		v1.share(v2);
		v2.share(v3);
		v.bind(atomic(1));
		assertTrue(v.isInstantiated());
		assertEquals(atomic(1), v.binding());
		assertEquals(atomic("1"), v.functor());
		assertEquals(0, v.arity());
		assertTrue(v1.isInstantiated());
		assertEquals(atomic(1), v1.binding());
		assertEquals(atomic("1"), v1.functor());
		assertEquals(0, v1.arity());
		assertTrue(v2.isInstantiated());
		assertEquals(atomic(1), v2.binding());
		assertEquals(atomic("1"), v2.functor());
		assertEquals(0, v2.arity());
		assertTrue(v3.isInstantiated());
		assertEquals(atomic(1), v3.binding());
		assertEquals(atomic("1"), v3.functor());
		assertEquals(0, v3.arity());

		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}
	
	
	@Test
	public void testBindingToNumberWithMultipleVariablesSharing() {
		Variable v = new Variable();
		Variable v1 = new Variable();
		Variable v2 = new Variable();
		Variable v3 = new Variable();
		v.share(v3);
		v1.share(v2);
		v2.share(v);
		v.bind(atomic(1));
		assertTrue(v.isInstantiated());
		assertEquals(atomic(1), v.binding());
		assertEquals(atomic("1"), v.functor());
		assertEquals(0, v.arity());
		assertTrue(v1.isInstantiated());
		assertEquals(atomic(1), v1.binding());
		assertEquals(atomic("1"), v1.functor());
		assertEquals(0, v1.arity());
		assertTrue(v2.isInstantiated());
		assertEquals(atomic(1), v2.binding());
		assertEquals(atomic("1"), v2.functor());
		assertEquals(0, v2.arity());
		assertTrue(v3.isInstantiated());
		assertEquals(atomic(1), v3.binding());
		assertEquals(atomic("1"), v3.functor());
		assertEquals(0, v3.arity());
		
		v.clear();
		assertFalse(v.isInstantiated());
		assertNull(v.binding());
	}

}
