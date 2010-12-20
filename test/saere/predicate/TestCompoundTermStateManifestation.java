package saere.predicate;

import static org.junit.Assert.*;
import org.junit.Test;

import saere.CompoundTerm;
import saere.State;
import saere.StringAtom;
import saere.Variable;
import static saere.term.TermFactory.*;

public class TestCompoundTermStateManifestation {

	@Test
	public void testStateManifestationOfGroundCompoundTerm() {
		{
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR);
			State state = ct.manifestState();
			if (state != null)
				state.reset();

			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
		}

		{
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR, atomic(2), atomic(3.0));
			State state = ct.manifestState();
			if (state != null)
				state.reset();

			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
			assertEquals(atomic(2), ct.arg(1));
			assertEquals(atomic(3.0), ct.arg(2));
		}

		{
			CompoundTerm ct = compoundTerm(
					StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR,
					atomic(2),
					atomic(3.0),
					compoundTerm(StringAtom.AND_FUNCTOR,
							StringAtom.instance("demo"), atomic(1)));
			State state = ct.manifestState();
			if (state != null){
				fail("unexpected result; the term is ground");
				state.reset();
			}

			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
			assertEquals(atomic(2), ct.arg(1));
			assertEquals(atomic(3.0), ct.arg(2));
			assertSame(StringAtom.instance("demo"), ct.arg(3).arg(0));
			assertEquals(atomic(1), ct.arg(3).arg(1));
		}
	}

	@Test
	public void testStateManifestationOfNonGroundUninstantiatedCompoundTermNoChanges() {
		{
			Variable v1 = new Variable();

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1);
			State state = ct.manifestState();
			state.reset();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR, v1, v1);
			State state = ct.manifestState();
			state.reset();

			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1,
					StringAtom.EMPTY_LIST_FUNCTOR, atomic(2),
					compoundTerm(StringAtom.AND_FUNCTOR, v1, v2));
			State state = ct.manifestState();
			state.reset();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(1));
			assertSame(v1, ct.arg(3).arg(0));
			assertSame(v2, ct.arg(3).arg(1));
			assertNull(v2.binding());
		}
	}

	@Test
	public void testStateManifestationOfNonGroundInstantiatedCompoundTermNoChanges() {
		{
			Variable v1 = new Variable();
			v1.bind(compoundTerm(StringAtom.OR_FUNCTOR, atomic(1), atomic(2)));

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1);
			State state = ct.manifestState();
			if (state != null) // state may be null.... v1 is bound to an atomic
								// value
				state.reset();

			assertSame(v1, ct.arg(0));
			assertEquals(
					compoundTerm(StringAtom.OR_FUNCTOR, atomic(1), atomic(2)),
					v1.binding());
		}

		{
			Variable v1 = new Variable();
			v1.bind(atomic(2));

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR, v1, v1);
			State state = ct.manifestState();
			if (state != null)
				state.reset();

			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertEquals(atomic(2), v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			v1.bind(StringAtom.instance("demo"));
			v2.bind(atomic(3.0));

			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1,
					StringAtom.EMPTY_LIST_FUNCTOR, atomic(2),
					compoundTerm(StringAtom.AND_FUNCTOR, v1, v2));
			State state = ct.manifestState();
			if (state != null)
				state.reset();

			assertSame(v1, ct.arg(0));
			assertEquals(StringAtom.instance("demo"), v1.binding());
			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(1));
			assertSame(v1, ct.arg(3).arg(0));
			assertSame(v2, ct.arg(3).arg(1));
			assertEquals(atomic(3.0), v2.binding());
		}
	}

	@Test
	public void testStateManifestationOfNonGroundUninstantiatedCompoundTermWithIntermediateBinding() {
		{
			Variable v1 = new Variable();
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1);
			State state = ct.manifestState();
			v1.bind(atomic(2.0));
			state.reset();
			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR,
					StringAtom.EMPTY_LIST_FUNCTOR, v1, v1);
			State state = ct.manifestState();
			v1.bind(compoundTerm(StringAtom.CUT_FUNCTOR, atomic(0)));
			state.reset();
			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(0));
			assertSame(v1, ct.arg(1));
			assertSame(v1, ct.arg(2));
			assertNull(v1.binding());
		}

		{
			Variable v1 = new Variable();
			Variable v2 = new Variable();
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1,
					StringAtom.EMPTY_LIST_FUNCTOR, atomic(2),
					compoundTerm(StringAtom.AND_FUNCTOR, v1, v2));
			State state = ct.manifestState();
			v1.bind(atomic(1));
			v2.bind(compoundTerm(StringAtom.instance("test"), atomic(1)));
			state.reset();

			assertSame(v1, ct.arg(0));
			assertNull(v1.binding());
			assertSame(StringAtom.EMPTY_LIST_FUNCTOR, ct.arg(1));
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
			v1.bind(compoundTerm(StringAtom.OR_FUNCTOR, v2, atomic(2)));
			CompoundTerm ct = compoundTerm(StringAtom.AND_FUNCTOR, v1);

			State state = ct.manifestState();
			v2.bind(atomic(2.0));
			state.reset();

			assertSame(v1, ct.arg(0));
			assertEquals(compoundTerm(StringAtom.OR_FUNCTOR, v2, atomic(2)),
					v1.binding());
			assertNull(v2.binding());
		}

	}

}
