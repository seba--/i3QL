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
package saere.predicate;

import static org.junit.Assert.*;
import org.junit.Test;

import saere.ComplexTerm;
import saere.State;
import saere.StringAtom;
import saere.Variable;
import static saere.term.Terms.*;

public class TestCompoundTermStateManifestation {

	@Test
	public void testStateManifestationOfGroundCompoundTerm() {
		{
			ComplexTerm ct = complexTerm(StringAtom.AND, StringAtom.EMPTY_LIST);
			State state = ct.manifestState();
			if (state != null)
				state.reincarnate();

			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
		}

		{
			ComplexTerm ct = complexTerm(StringAtom.AND, StringAtom.EMPTY_LIST,
					atomic(2), atomic(3.0));
			State state = ct.manifestState();
			if (state != null)
				state.reincarnate();

			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
			assertEquals(atomic(2), ct.arg(1));
			assertEquals(atomic(3.0), ct.arg(2));
		}

		{
			ComplexTerm ct = complexTerm(
					StringAtom.AND,
					StringAtom.EMPTY_LIST,
					atomic(2),
					atomic(3.0),
					complexTerm(StringAtom.AND, StringAtom.get("demo"),
							atomic(1)));
			State state = ct.manifestState();
			if (state != null) {
				fail("unexpected result; the term is ground");
				state.reincarnate();
			}

			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
			assertEquals(atomic(2), ct.arg(1));
			assertEquals(atomic(3.0), ct.arg(2));
			assertSame(StringAtom.get("demo"), ct.arg(3).arg(0));
			assertEquals(atomic(1), ct.arg(3).arg(1));
		}
	}

	@Test
	public void testStateManifestationOfNonGroundUninstantiatedCompoundTermNoChanges() {
		{
			Variable v1 = new Variable();

			ComplexTerm ct = complexTerm(StringAtom.AND, v1);
			State state = ct.manifestState();
			state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();

			ComplexTerm ct = complexTerm(StringAtom.AND, StringAtom.EMPTY_LIST,
					v1, v1);
			State state = ct.manifestState();
			state.reincarnate();

			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();

			ComplexTerm ct = complexTerm(StringAtom.AND, v1,
					StringAtom.EMPTY_LIST, atomic(2),
					complexTerm(StringAtom.AND, v1, v2));
			State state = ct.manifestState();
			state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
			assertSame(StringAtom.EMPTY_LIST, ct.arg(1));
			assertSame(v1, ct.arg(3).arg(0));
			assertSame(v2, ct.arg(3).arg(1));
			assertNull(v2.binding());
		}
	}

	@Test
	public void testStateManifestationOfNonGroundInstantiatedCompoundTermNoChanges() {
		{
			Variable v1 = new Variable();
			v1.bind(complexTerm(StringAtom.OR, atomic(1), atomic(2)));

			ComplexTerm ct = complexTerm(StringAtom.AND, v1);
			State state = ct.manifestState();
			if (state != null) // state may be null.... v1 is bound to an atomic
				// value
				state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertEquals(complexTerm(StringAtom.OR, atomic(1), atomic(2)),
					v1.binding());
		}

		{
			Variable v1 = new Variable();
			v1.bind(atomic(2));

			ComplexTerm ct = complexTerm(StringAtom.AND, StringAtom.EMPTY_LIST,
					v1, v1);
			State state = ct.manifestState();
			if (state != null)
				state.reincarnate();

			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertEquals(atomic(2), v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			v1.bind(StringAtom.get("demo"));
			v2.bind(atomic(3.0));

			ComplexTerm ct = complexTerm(StringAtom.AND, v1,
					StringAtom.EMPTY_LIST, atomic(2),
					complexTerm(StringAtom.AND, v1, v2));
			State state = ct.manifestState();
			if (state != null)
				state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertEquals(StringAtom.get("demo"), v1.binding());
			assertSame(StringAtom.EMPTY_LIST, ct.arg(1));
			assertSame(v1, ct.arg(3).arg(0));
			assertSame(v2, ct.arg(3).arg(1));
			assertEquals(atomic(3.0), v2.binding());
		}
	}

	@Test
	public void testStateManifestationOfNonGroundUninstantiatedCompoundTermWithIntermediateBinding() {
		{
			Variable v1 = new Variable();
			ComplexTerm ct = complexTerm(StringAtom.AND, v1);
			State state = ct.manifestState();
			v1.bind(atomic(2.0));
			state.reincarnate();
			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			ComplexTerm ct = complexTerm(StringAtom.AND, StringAtom.EMPTY_LIST,
					v1, v1);
			State state = ct.manifestState();
			v1.bind(complexTerm(StringAtom.CUT, atomic(0)));
			state.reincarnate();
			assertSame(StringAtom.EMPTY_LIST, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			ComplexTerm ct = complexTerm(StringAtom.AND, v1,
					StringAtom.EMPTY_LIST, atomic(2),
					complexTerm(StringAtom.AND, v1, v2));
			State state = ct.manifestState();
			v1.bind(atomic(1));
			v2.bind(complexTerm(StringAtom.get("test"), atomic(1)));
			state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
			assertSame(StringAtom.EMPTY_LIST, ct.arg(1));
			assertSame(v1, ct.arg(3).arg(0));
			assertSame(v2, ct.arg(3).arg(1));
			assertNull(v2.binding());
		}
	}

	@Test
	public void testStateManifestationOfNonGroundInstantiatedCompoundTermWithIntermediateBinding() {
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			v1.bind(complexTerm(StringAtom.OR, v2, atomic(2)));
			ComplexTerm ct = complexTerm(StringAtom.AND, v1);

			State state = ct.manifestState();
			v2.bind(atomic(2.0));
			state.reincarnate();

			assertSame(v1, ct.arg(0));
			assertEquals(complexTerm(StringAtom.OR, v2, atomic(2)),
					v1.binding());
			assertNull(v2.binding());
		}

	}

}
