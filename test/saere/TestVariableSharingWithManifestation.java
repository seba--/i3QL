package saere;

import org.junit.Test;
import static org.junit.Assert.*;

import saere.State;
import saere.Variable;
import saere.term.TermFactory;

public class TestVariableSharingWithManifestation {

	private final static Atomic atomic = TermFactory.atomic(1);

	@Test
	public void testNoSharing() {
		{
			Variable v = new Variable();
			assertNull(v.binding());

			State s = v.manifestState();
			v.setState(s);

			assertNull(v.getValue());
			assertNull(v.binding());
			assertFalse(v.isInstantiated());
			assertFalse(v.isGround());
		}

		{
			Variable v = new Variable();
			assertNull(v.binding());

			State s1 = v.manifestState();
			v.bind(TermFactory.atomic(1));
			{
				State s2 = v.manifestState();
				assertTrue(v.isInstantiated());
				assertTrue(v.isGround());
				v.setState(s2);
			}
			v.setState(s1);

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
			v11.setState(s1);
			v21.setState(s2);
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
			v12.setState(s1);
			v21.setState(s2);
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
			v13.setState(s1);
			v21.setState(s2);
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
			v13.setState(s1);
			v21.setState(s2);
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
			v11.setState(s1);
			v21.setState(s2);

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
				v.setState(sx);
			}
			v.setState(s);

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
			v.setState(s);

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
			v2.setState(s);

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
			v1.setState(s1);
			v2.setState(s2);

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
			v2.setState(s);

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
			v1.setState(s1);
			v2.setState(s2);
			v3.setState(s3);

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
						v1.setState(is);
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
							v2.setState(is);
						}
						{
							State is = v3.manifestState();
							v3.bind(atomic);
							assertEquals(atomic, v1.binding());
							assertEquals(atomic, v2.binding());
							assertEquals(atomic, v3.binding());
							v3.setState(is);
						}
						{
							State s12 = v1.manifestState();
							State s32 = v3.manifestState();
							v1.share(v3);
							v3.setState(s32);
							v1.setState(s12);
						}
						v2.setState(s22);
						v3.setState(s31);

					}
					{
						State is = v1.manifestState();
						v1.bind(atomic);
						assertEquals(atomic, v1.binding());
						assertEquals(atomic, v2.binding());
						assertNull(v3.binding());
						v1.setState(is);
					}
				}

				v1.setState(s11);
				v2.setState(s21);

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
