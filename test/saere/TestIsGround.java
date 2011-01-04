/* License (BSD Style License):
 * Copyright (c) 2010
 * Department of Computer Science
 * Technische Universität Darmstadt
 * All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische 
 *    Universität Darmstadt nor the names of its contributors may be used to 
 *    endorse or promote products derived from this software without specific 
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package saere;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static saere.term.Terms.atomic;

import org.junit.Test;

import saere.term.ListElement2;

public class TestIsGround {

	@Test
	public void testAtomicIsGround() {
		assertTrue(atomic(1).isGround());
		assertTrue(atomic(2.1).isGround());
		assertTrue(atomic("demo").isGround());
	}

	@Test
	public void testGroundCompoundTermIsGround() {
		assertTrue(new ListElement2(atomic(0), StringAtom.EMPTY_LIST)
				.isGround());
	}

	@Test
	public void testGroundnessOfVariables() {
		assertFalse(new Variable().isGround());

		{
			Variable v1 = new Variable();
			v1.share(new Variable());
			new Variable().share(v1);
			assertFalse(v1.isGround());
		}

		{
			Variable v1 = new Variable();
			v1.setValue(atomic(1));
			assertTrue(v1.isGround());
		}

		{
			Variable v1 = new Variable();
			v1.setValue(atomic(1.0));
			assertTrue(v1.isGround());
		}

		{
			Variable v1 = new Variable();
			v1.setValue(atomic("demo"));
			assertTrue(v1.isGround());
		}

		{
			Variable v1 = new Variable();
			v1.share(new Variable());
			Variable v2 = new Variable();
			v2.share(v1);
			Variable v3 = new Variable();
			v3.share(v2);
			v2.bind(new ListElement2(atomic("demo"), StringAtom.EMPTY_LIST));
			assertTrue(v1.isGround());
			assertTrue(v2.isGround());
			assertTrue(v3.isGround());
		}

		{
			Variable v1 = new Variable();
			v1.share(new Variable());
			Variable v2 = new Variable();
			v2.share(v1);
			Variable v3 = new Variable();
			State s2 = v2.manifestState();
			State s3 = v3.manifestState();
			v3.share(v2);
			v2.bind(new ListElement2(atomic("demo"), StringAtom.EMPTY_LIST));
			assertTrue(v1.isGround());
			assertTrue(v2.isGround());
			assertTrue(v3.isGround());
			s3.reincarnate();
			s2.reincarnate();
			assertFalse(v1.isGround());
			assertFalse(v2.isGround());
			assertFalse(v3.isGround());
		}
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
		Term innerList = new ListElement2(StringAtom.get("demo"),
				new ListElement2(l2, StringAtom.EMPTY_LIST));
		// [_,demo,_]
		v.bind(new ListElement2(l1, innerList));
		assertTrue(v.isInstantiated());
		assertEquals(2, v.arity());
		assertEquals(StringAtom.LIST, v.functor());
		assertSame(l1, v.arg(0));
		assertSame(innerList, v.arg(1));
		// [_,demo|_]
		State s1 = v.manifestState();
		Variable tail = new Variable();

		assertTrue(new ListElement2(new Variable(), new ListElement2(
				StringAtom.get("demo"), tail)).unify(v.binding()));
		assertEquals(new ListElement2(l2, StringAtom.EMPTY_LIST),
				tail.binding());
		s1.reincarnate();

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
	public void testBindingToNumberWithLongChainSharing() {
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
