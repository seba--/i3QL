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

import org.junit.Test;
import static org.junit.Assert.*;

import saere.State;
import saere.Variable;
import saere.term.Terms;

public class TestVariableSharingWithManifestation {

	private final static Atomic atomic = Terms.atomic(1);

	@Test
	public void testNoSharing() {
		{
			Variable v = new Variable();
			assertNull(v.binding());

			State s = v.manifestState();
			s.reincarnate();

			assertNull(v.getValue());
			assertNull(v.binding());
			assertFalse(v.isInstantiated());
			assertFalse(v.isGround());
		}

		{
			Variable v = new Variable();
			assertNull(v.binding());

			State s1 = v.manifestState();
			v.unify(Terms.atomic(1));
			{
				State s2 = v.manifestState();
				assertTrue(v.isInstantiated());
				assertTrue(v.isGround());
				if (s2 != null)
					s2.reincarnate();
			}
			s1.reincarnate();

			assertNull(v.getValue());
			assertNull(v.binding());
			assertFalse(v.isInstantiated());
			assertFalse(v.isGround());
		}
	}

	@Test
	public void testLongChainsSharing() {
		{
			Variable v11 = new Variable();
			Variable v12 = new Variable();
			Variable v13 = new Variable();
			Variable v21 = new Variable();
			Variable v22 = new Variable();
			Variable v23 = new Variable();
			Variable v24 = new Variable();
			v11.unify(v12);
			v12.unify(v13);
			v21.unify(v22);
			v22.unify(v23);
			v23.unify(v24);

			State s1 = v11.manifestState();
			State s2 = v21.manifestState();
			v11.unify(v21);
			s1.reincarnate();
			s2.reincarnate();
			v11.unify(atomic);

			assertEquals(atomic, v11.binding());
			assertEquals(atomic, v12.binding());
			assertEquals(atomic, v13.binding());
			assertNull(v21.binding());
			assertNull(v22.binding());
			assertNull(v23.binding());
			assertNull(v24.binding());
			assertFalse(v21.isInstantiated());
			assertFalse(v21.isGround());
			assertFalse(v22.isInstantiated());
			assertFalse(v22.isGround());
			assertFalse(v23.isInstantiated());
			assertFalse(v23.isGround());
			assertFalse(v24.isInstantiated());
			assertFalse(v24.isGround());
		}
		{
			Variable v11 = new Variable();
			Variable v12 = new Variable();
			Variable v13 = new Variable();
			Variable v21 = new Variable();
			Variable v22 = new Variable();
			Variable v23 = new Variable();
			Variable v24 = new Variable();
			v11.unify(v12);
			v12.unify(v13);
			v21.unify(v22);
			v22.unify(v23);
			v23.unify(v24);

			State s1 = v12.manifestState();
			State s2 = v21.manifestState();
			v12.unify(v21);
			s1.reincarnate();
			s2.reincarnate();
			v12.unify(atomic);

			assertEquals(atomic, v11.binding());
			assertEquals(atomic, v12.binding());
			assertEquals(atomic, v13.binding());
			assertNull(v21.binding());
			assertNull(v22.binding());
			assertNull(v23.binding());
			assertNull(v24.binding());
			assertFalse(v21.isInstantiated());
			assertFalse(v21.isGround());
			assertFalse(v22.isInstantiated());
			assertFalse(v22.isGround());
			assertFalse(v23.isInstantiated());
			assertFalse(v23.isGround());
			assertFalse(v24.isInstantiated());
			assertFalse(v24.isGround());
		}

		{
			Variable v11 = new Variable();
			Variable v12 = new Variable();
			Variable v13 = new Variable();
			Variable v21 = new Variable();
			Variable v22 = new Variable();
			Variable v23 = new Variable();
			Variable v24 = new Variable();
			v11.unify(v12);
			v12.unify(v13);
			v21.unify(v22);
			v22.unify(v23);
			v23.unify(v24);

			State s1 = v13.manifestState();
			State s2 = v21.manifestState();
			v13.unify(v21);
			s1.reincarnate();
			s2.reincarnate();
			v13.unify(atomic);

			assertEquals(atomic, v11.binding());
			assertEquals(atomic, v12.binding());
			assertEquals(atomic, v13.binding());
			assertNull(v21.binding());
			assertNull(v22.binding());
			assertNull(v23.binding());
			assertNull(v24.binding());
			assertFalse(v21.isInstantiated());
			assertFalse(v21.isGround());
			assertFalse(v22.isInstantiated());
			assertFalse(v22.isGround());
			assertFalse(v23.isInstantiated());
			assertFalse(v23.isGround());
			assertFalse(v24.isInstantiated());
			assertFalse(v24.isGround());
		}

		{
			Variable v11 = new Variable();
			Variable v12 = new Variable();
			Variable v13 = new Variable();
			Variable v21 = new Variable();
			Variable v22 = new Variable();
			Variable v23 = new Variable();
			Variable v24 = new Variable();
			v12.unify(v11);
			v13.unify(v12);
			v22.unify(v21);
			v23.unify(v22);
			v24.unify(v23);

			State s1 = v13.manifestState();
			State s2 = v21.manifestState();
			v13.unify(v21);
			s1.reincarnate();
			s2.reincarnate();
			v13.unify(atomic);

			assertEquals(atomic, v11.binding());
			assertEquals(atomic, v12.binding());
			assertEquals(atomic, v13.binding());
			assertNull(v21.binding());
			assertNull(v22.binding());
			assertNull(v23.binding());
			assertNull(v24.binding());
			assertFalse(v21.isInstantiated());
			assertFalse(v21.isGround());
			assertFalse(v22.isInstantiated());
			assertFalse(v22.isGround());
			assertFalse(v23.isInstantiated());
			assertFalse(v23.isGround());
			assertFalse(v24.isInstantiated());
			assertFalse(v24.isGround());
		}

		{
			Variable v11 = new Variable();
			Variable v12 = new Variable();
			Variable v13 = new Variable();
			Variable v21 = new Variable();
			Variable v22 = new Variable();
			Variable v23 = new Variable();
			Variable v24 = new Variable();
			v11.unify(v12);
			v12.unify(v13);
			v21.unify(v22);
			v22.unify(v23);
			v23.unify(v24);

			State s1 = v11.manifestState();
			State s2 = v21.manifestState();
			v11.unify(v21);
			v11.unify(atomic);
			s1.reincarnate();
			s2.reincarnate();

			assertNull(v11.binding());
			assertNull(v12.binding());
			assertNull(v13.binding());
			assertNull(v21.binding());
			assertNull(v22.binding());
			assertNull(v23.binding());
			assertNull(v24.binding());
			assertFalse(v21.isInstantiated());
			assertFalse(v21.isGround());
			assertFalse(v22.isInstantiated());
			assertFalse(v22.isGround());
			assertFalse(v23.isInstantiated());
			assertFalse(v23.isGround());
			assertFalse(v24.isInstantiated());
			assertFalse(v24.isGround());

		}
	}

	@Test
	public void testWithIntermediateSharing() {
		{
			Variable v = new Variable();
			Variable i = new Variable();
			assertNull(v.binding());

			State s = v.manifestState();
			v.unify(i);
			{
				i.unify(atomic);
				State sx = v.manifestState();
				assertTrue(v.isInstantiated());
				assertTrue(v.isGround());
				if (sx != null) // sx may be null, because sx is bound to an
								// atomic value.
					sx.reincarnate();
			}
			s.reincarnate();

			assertNull(v.getValue());
			assertNull(v.binding());
			assertFalse(v.isInstantiated());
			assertFalse(v.isGround());
		}

		{
			Variable v = new Variable();
			assertNull(v.binding());

			State s = v.manifestState();
			v.unify(new Variable());
			v.unify(new Variable());
			v.unify(new Variable());
			s.reincarnate();

			assertNull(v.getValue());
			assertNull(v.binding());
			assertFalse(v.isInstantiated());
			assertFalse(v.isGround());
		}
	}

	@Test
	public void testCyclicSharing() {
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();

			State s1 = v1.manifestState();
			State s2 = v2.manifestState();
			v2.unify(v1); // V2 = V1
			s1.reincarnate();
			s2.reincarnate();

			// V1 and V2 do no longer share
			v1.unify(atomic);
			assertEquals(atomic, v1.binding());
			assertNull(v2.binding());
		}
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			v1.unify(v2); // V1 = V2

			State s1 = v1.manifestState();
			State s2 = v2.manifestState();
			v2.unify(v1); // V2 = V1
			s1.reincarnate();
			s2.reincarnate();

			// V1 and V2 still share
			v1.unify(atomic);

			assertEquals(atomic, v1.binding());
			assertEquals(atomic, v2.binding());
		}
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			Variable v3 = new Variable();
			v1.unify(v2); // V1 = V2
			v2.unify(v3); // V2 = V1

			State s = v2.manifestState();
			v1.unify(v3);
			s.reincarnate();

			// V1, V2 and V3 still share
			v1.unify(atomic);

			assertEquals(atomic, v1.binding());
			assertEquals(atomic, v2.binding());
			assertEquals(atomic, v3.binding());
		}
		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			Variable v3 = new Variable();
			State s1 = v1.manifestState();
			State s2 = v2.manifestState();
			State s3 = v3.manifestState();
			v1.unify(v2); // V1 = V2
			v2.unify(v3); // V2 = V1
			v1.unify(v3);
			s1.reincarnate();
			s2.reincarnate();
			s3.reincarnate();

			// V1, V2 and V3 still share
			v1.unify(atomic);

			assertEquals(atomic, v1.binding());
			assertNull(v2.binding());
			assertNull(v3.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			Variable v3 = new Variable();

			{
				State s11 = v1.manifestState();
				State s21 = v2.manifestState();
				v1.unify(v2); // V1 = V2
				{
					{
						State is = v1.manifestState();
						v1.unify(atomic);
						assertEquals(atomic, v1.binding());
						assertEquals(atomic, v2.binding());
						assertNull(v3.binding());
						is.reincarnate();
						assertNull(v1.binding());
						assertNull(v2.binding());
						assertNull(v3.binding());
					}

					{
						State s22 = v2.manifestState();
						State s31 = v3.manifestState();
						v2.unify(v3); // V2 = V1
						{
							State is = v2.manifestState();
							v2.unify(atomic);
							assertEquals(atomic, v1.binding());
							assertEquals(atomic, v2.binding());
							assertEquals(atomic, v3.binding());
							is.reincarnate();
						}
						{
							State is = v3.manifestState();
							v3.unify(atomic);
							assertEquals(atomic, v1.binding());
							assertEquals(atomic, v2.binding());
							assertEquals(atomic, v3.binding());
							is.reincarnate();
						}
						{
							State s12 = v1.manifestState();
							State s32 = v3.manifestState();
							v1.unify(v3);
							s32.reincarnate();
							s12.reincarnate();
						}
						s22.reincarnate();
						s31.reincarnate();

					}
					{
						State is = v1.manifestState();
						v1.unify(atomic);
						assertEquals(atomic, v1.binding());
						assertEquals(atomic, v2.binding());
						assertNull(v3.binding());
						is.reincarnate();
					}
				}

				s11.reincarnate();
				s21.reincarnate();

				assertNull(v1.binding());
				assertNull(v2.binding());
				assertNull(v3.binding());

			}

			// V1, V2 and V3 still share
			v1.unify(atomic);

			assertEquals(atomic, v1.binding());
			assertNull(v2.binding());
			assertNull(v3.binding());
		}
	}

}
