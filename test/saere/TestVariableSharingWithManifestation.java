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
	    s.reset();

	    assertNull(v.getValue());
	    assertNull(v.binding());
	    assertFalse(v.isInstantiated());
	    assertFalse(v.isGround());
	}

	{
	    Variable v = new Variable();
	    assertNull(v.binding());

	    State s1 = v.manifestState();
	    v.bind(Terms.atomic(1));
	    {
		State s2 = v.manifestState();
		assertTrue(v.isInstantiated());
		assertTrue(v.isGround());
		if (s2 != null)
		    s2.reset();
	    }
	    s1.reset();

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
	    v11.share(v12);
	    v12.share(v13);
	    v21.share(v22);
	    v22.share(v23);
	    v23.share(v24);

	    State s1 = v11.manifestState();
	    State s2 = v21.manifestState();
	    v11.share(v21);
	    s1.reset();
	    s2.reset();
	    v11.bind(atomic);

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
	    v11.share(v12);
	    v12.share(v13);
	    v21.share(v22);
	    v22.share(v23);
	    v23.share(v24);

	    State s1 = v12.manifestState();
	    State s2 = v21.manifestState();
	    v12.share(v21);
	    s1.reset();
	    s2.reset();
	    v12.bind(atomic);

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
	    v11.share(v12);
	    v12.share(v13);
	    v21.share(v22);
	    v22.share(v23);
	    v23.share(v24);

	    State s1 = v13.manifestState();
	    State s2 = v21.manifestState();
	    v13.share(v21);
	    s1.reset();
	    s2.reset();
	    v13.bind(atomic);

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
	    v12.share(v11);
	    v13.share(v12);
	    v22.share(v21);
	    v23.share(v22);
	    v24.share(v23);

	    State s1 = v13.manifestState();
	    State s2 = v21.manifestState();
	    v13.share(v21);
	    s1.reset();
	    s2.reset();
	    v13.bind(atomic);

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
	    v11.share(v12);
	    v12.share(v13);
	    v21.share(v22);
	    v22.share(v23);
	    v23.share(v24);

	    State s1 = v11.manifestState();
	    State s2 = v21.manifestState();
	    v11.share(v21);
	    v11.bind(atomic);
	    s1.reset();
	    s2.reset();

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
	    v.share(i);
	    {
		i.bind(atomic);
		State sx = v.manifestState();
		assertTrue(v.isInstantiated());
		assertTrue(v.isGround());
		if (sx != null) // sx may be null, because sx is bound to an atomic value.
		    sx.reset();
	    }
	    s.reset();

	    assertNull(v.getValue());
	    assertNull(v.binding());
	    assertFalse(v.isInstantiated());
	    assertFalse(v.isGround());
	}

	{
	    Variable v = new Variable();
	    assertNull(v.binding());

	    State s = v.manifestState();
	    v.share(new Variable());
	    v.share(new Variable());
	    v.share(new Variable());
	    s.reset();

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

	    State s = v2.manifestState();
	    v2.share(v1); // V2 = V1
	    s.reset();

	    // V1 and V2 do no longer share
	    v1.bind(atomic);
	    assertEquals(atomic, v1.binding());
	    assertNull(v2.binding());
	}
	{
	    Variable v1 = new Variable();
	    Variable v2 = new Variable();
	    v1.share(v2); // V1 = V2

	    State s1 = v1.manifestState();
	    State s2 = v2.manifestState();
	    v2.share(v1); // V2 = V1
	    s1.reset();
	    s2.reset();

	    // V1 and V2 still share
	    v1.bind(atomic);

	    assertEquals(atomic, v1.binding());
	    assertEquals(atomic, v2.binding());
	}
	{
	    Variable v1 = new Variable();
	    Variable v2 = new Variable();
	    Variable v3 = new Variable();
	    v1.share(v2); // V1 = V2
	    v2.share(v3); // V2 = V1

	    State s = v2.manifestState();
	    v1.share(v3);
	    s.reset();

	    // V1, V2 and V3 still share
	    v1.bind(atomic);

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
	    v1.share(v2); // V1 = V2
	    v2.share(v3); // V2 = V1
	    v1.share(v3);
	    s1.reset();
	    s2.reset();
	    s3.reset();

	    // V1, V2 and V3 still share
	    v1.bind(atomic);

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
		v1.share(v2); // V1 = V2
		{
		    {
			State is = v1.manifestState();
			v1.bind(atomic);
			assertEquals(atomic, v1.binding());
			assertEquals(atomic, v2.binding());
			assertNull(v3.binding());
			is.reset();
			assertNull(v1.binding());
			assertNull(v2.binding());
			assertNull(v3.binding());
		    }

		    {
			State s22 = v2.manifestState();
			State s31 = v3.manifestState();
			v2.share(v3); // V2 = V1
			{
			    State is = v2.manifestState();
			    v2.bind(atomic);
			    assertEquals(atomic, v1.binding());
			    assertEquals(atomic, v2.binding());
			    assertEquals(atomic, v3.binding());
			    is.reset();
			}
			{
			    State is = v3.manifestState();
			    v3.bind(atomic);
			    assertEquals(atomic, v1.binding());
			    assertEquals(atomic, v2.binding());
			    assertEquals(atomic, v3.binding());
			    is.reset();
			}
			{
			    State s12 = v1.manifestState();
			    State s32 = v3.manifestState();
			    v1.share(v3);
			    s32.reset();
			    s12.reset();
			}
			s22.reset();
			s31.reset();

		    }
		    {
			State is = v1.manifestState();
			v1.bind(atomic);
			assertEquals(atomic, v1.binding());
			assertEquals(atomic, v2.binding());
			assertNull(v3.binding());
			is.reset();
		    }
		}

		s11.reset();
		s21.reset();

		assertNull(v1.binding());
		assertNull(v2.binding());
		assertNull(v3.binding());

	    }

	    // V1, V2 and V3 still share
	    v1.bind(atomic);

	    assertEquals(atomic, v1.binding());
	    assertNull(v2.binding());
	    assertNull(v3.binding());
	}
    }

}
