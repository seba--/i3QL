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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static saere.term.Terms.atomic;
import static saere.term.Terms.compoundTerm;

import org.junit.Test;

public class TestIsIdentical {

	@Test
	public void testAtomicIsIdentical() {
		assertTrue(Term.isIdentical(atomic(1), atomic(1)));
		assertTrue(Term.isIdentical(atomic(2.1), atomic(2.1)));
		assertTrue(Term.isIdentical(atomic("1"), atomic("1")));
		assertFalse(Term.isIdentical(atomic("1"), atomic("2")));
		
		assertFalse(Term.isIdentical(atomic("2"), atomic("1")));
		assertFalse(Term.isIdentical(atomic(1), atomic("1")));
		assertFalse(Term.isIdentical(atomic("1"), atomic(1)));
		assertFalse(Term.isIdentical(atomic(1.0), atomic(1)));
	}

	@Test
	public void testVariableIsIdentical() {
		{
			Variable v1 = new Variable();
			assertTrue(Term.isIdentical(v1,v1));
		}
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			assertFalse(Term.isIdentical(v1,v2));
			assertFalse(Term.isIdentical(v2,v1));
		}
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			v1.setValue(v2);
			assertTrue(Term.isIdentical(v1,v2));
			assertTrue(Term.isIdentical(v2,v1));
		}
		{
			Variable v1 = new Variable();
			v1.setValue(atomic(1));
			assertTrue(Term.isIdentical(v1,atomic(1)));
			assertTrue(Term.isIdentical(atomic(1),v1));
		}
	}

	@Test
	public void testCompoundTermIsIdentical() {
		Variable singleton = new Variable();
		Variable unified_1 = new Variable();
		Variable unified_2 = new Variable();
		unified_1.setValue(unified_2);
		CompoundTerm ct1 = compoundTerm(atomic("a"),atomic("b"),singleton);
		CompoundTerm ct2 = compoundTerm(atomic("a"),atomic("b"),singleton);
		assertTrue(Term.isIdentical(ct1, ct2));
		assertTrue(Term.isIdentical(ct2, ct1));
		
		CompoundTerm ct3 = compoundTerm(atomic("a"),atomic("b"),unified_1);
		CompoundTerm ct4 = compoundTerm(atomic("a"),atomic("b"),unified_2);
		assertTrue(Term.isIdentical(ct3, ct4));
		assertTrue(Term.isIdentical(ct4, ct3));
		
		assertFalse(Term.isIdentical(ct4, ct1));
		assertFalse(Term.isIdentical(ct1, ct4));
		assertFalse(Term.isIdentical(ct3, ct1));
		assertFalse(Term.isIdentical(ct1, ct3));
		assertFalse(Term.isIdentical(ct4, ct2));
		assertFalse(Term.isIdentical(ct2, ct4));
		assertFalse(Term.isIdentical(ct3, ct2));
		assertFalse(Term.isIdentical(ct2, ct3));
		
	}
}
