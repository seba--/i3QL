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

import static saere.IntValue.get;

/**
 * Representation of a Prolog term.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class Term {

	public final static Term[] NO_TERMS = new Term[0];

	public static final int VARIABLE_TYPE_ID = -1;
	public static final int COMPUND_TERM_TYPE_ID = 0;
	/** If the value is equal or larger than ATOMIC, then the term is atomic. */
	public static final int ATOMIC_TERM_TYPE_ID = 1;
	public static final int STRING_ATOM_TYPE_ID = 1;
	/** If the term type id is equal or larger than NUMBER, then the term is a number. */
	public static final int NUMBER_TYPE_ID = 2;
	public static final int FLOAT_VALUE_TYPE_ID = 2;
	public static final int INT_VALUE_TYPE_ID = 3;

	public abstract int termTypeID();

	/**
	 * Unification of this term with the given term. If the unification succeeds <code>true</code>
	 * is returned.
	 * <p>
	 * After return this or the other term's initially free variable may be (partially) bound. This
	 * method does not take care of state handling!
	 * </p>
	 */
	public final boolean unify(Term other) {
		return Term.unify(this, other);
	}

	/**
	 * Returns <code>true</code> if this term is ground. String atoms, float values and integer
	 * values are always ground. A complex term is ground if all arguments are ground. A variable is
	 * ground if the variable is bound to a ground term.
	 * 
	 * @return <code>true</code> if this term is ground.
	 */
	public abstract boolean isGround();

	/**
	 * Creates a {@link State} object that encapsulates the complete variable state information of
	 * this term. The {@link State} object is later on used to reset (cf.
	 * {@link State#reincarnate()}) the state of this term to the time when this method was called
	 * (to undo all changes that were done in between).
	 * 
	 * <p>
	 * <b>Implementation Note</b><br />
	 * This method implements the Memento Pattern.
	 * </p>
	 * 
	 * @return An object that encapsulates this term's state. The state object's precise type is
	 *         always private to the term object that created it. The caller must not make any
	 *         assumptions about the object's structure.
	 *         <p>
	 *         <b>Performance Guideline</b><br />
	 *         It is legal and highly encouraged to return <code>null</code> if this term's state is
	 *         immutable.
	 *         </p>
	 */
	public abstract State manifestState();

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link Variable} object.
	 *         <p>
	 *         <b>Prolog Semantics</b><br />
	 *         (Note, this is unrelated to the question whether the variable is instantiated / free
	 *         or not.)
	 *         </p>
	 */
	public boolean isVariable() {
		return false;
	}

	/**
	 * @return <code>true</code> if the type of this term is not a subtype of {@link Variable}.
	 */
	public boolean isNotVariable() {
		return true;
	}

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link Variable} object.
	 */
	public Variable asVariable() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link CompoundTerm}.
	 */
	public boolean isCompoundTerm() {
		return false;
	}

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link CompoundTerm}.
	 */
	public CompoundTerm asCompoundTerm() {
		throw new ClassCastException();
	}

	/**
	 * 
	 * @return <code>true</code> if this term is a subtype of {@link Atomic}.
	 */
	public boolean isAtomic() {
		return true;
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link StringAtom}.
	 */
	public boolean isStringAtom() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of a {@link StringAtom}.
	 */
	public StringAtom asStringAtom() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of an {@link IntValue}.
	 */
	public boolean isIntValue() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of an {@link IntValue}.
	 */
	public IntValue asIntValue() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link FloatValue}.
	 */
	public boolean isFloatValue() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of a {@link FloatValue}.
	 */
	public FloatValue asFloatValue() {
		throw new ClassCastException();
	}

	/**
	 * @return The functor of this term. If this term is subtype of atomic, then - if necessary - a
	 *         {@link StringAtom} is created and this {@link StringAtom} object is returned.
	 */
	public abstract StringAtom functor();

	/**
	 * @return The arity of this term.
	 */
	public abstract int arity();

	/**
	 * @return The i<i>th</i> argument of this term (zero based).<br/>
	 *         If this term does not have any arguments ( <code>arity() == 0</code>) an
	 *         <code>IndexOutOfBoundsException</code> is always thrown.
	 *         <p>
	 *         <b>Implementation Note</b><br />
	 *         If this term has at least one argument and <i>i</i> is larger than or equal to the
	 *         arity of the term, then the method is free to return the last argument or to throw an
	 *         <code>IndexOutOfBoundsException</code>.
	 *         </p>
	 */
	public abstract Term arg(int i) throws IndexOutOfBoundsException;

	/**
	 * @return Evaluates the arithmetic expression represented by this term, if possible.
	 * @throws PrologException
	 *             if this term does not model an arithmetic expression.
	 */
	public long intEval() throws PrologException {
		throw new PrologException("this term (" + this.toProlog() + ") is not an arithmetic term");
	}

	/**
	 * @return Evaluates the arithmetic expression represented by this term, if possible.
	 * @throws PrologException
	 *             if this term does not model an arithmetic expression.
	 */
	public double floatEval() throws PrologException {
		throw new PrologException("this term (" + this.toProlog() + ") is not an arithmetic term");
	}

	/**
	 * Calls the predicate that corresponds to this term.
	 * 
	 * @return A new instance of the predicate initialized using this term's current arguments.
	 */
	public abstract Goal call();

	/**
	 * @return A textual representation of the term that uses the Prolog syntax. I.e., a Prolog
	 *         compiler should be able to immediately parse the resulting string.
	 */
	public abstract String toProlog();

	/**
	 * Unifies two terms.
	 * <p>
	 * <font color="red><b> This method does not take care of state handling. It is the
	 * responsibility of the caller to manifest the state of the given terms before calling this
	 * method and to restore the state at the appropriate point in time. </b></font><br />
	 * By moving the responsibility for state handling to the caller various optimizations of how
	 * and when the state is saved / restored are possible
	 * </p>
	 * 
	 * @param t1
	 *            The first term.
	 * @param t2
	 *            The second term.
	 * @return <code>true</code> if both terms were successfully unified; <code>false</code>
	 *         otherwise.
	 */
	@SuppressWarnings("all")
	public final static boolean unify(Term t1, Term t2) {
		if (t1.isVariable()) {
			Variable t1hv = t1.asVariable().headVariable();
			Term t1hvv = t1hv.getValue();
			if (t1hvv == null) {
				t1 = t1hv; // t1 is a free head variable
			} else {
				t1 = t1hvv; // t1 is a term that is not a variable
			}
		}

		if (t2.isVariable()) {
			Variable t2v = t2.asVariable();
			Variable t2hv = t2v.headVariable();
			Term t2hvv = t2hv.getValue();
			if (t2hvv == null) {
				if (t2hv != t1) { // this checks that t1 and t2 not already
								  // share
					// now t2 and t1 either share or t2 is bound to some term
					t2hv.setValue(t1);
				}
				return true;
			} else {
				// ... t2 is an instantiated variable
				t2 = t2hvv;
			}
		}

		// minor performance improvement
		if (t1 == t2) {
			return true;
		}

		switch (t1.termTypeID()) {
		case VARIABLE_TYPE_ID:
			// We know that:
			// 1) t1 is actually a free variable and it is a head variable
			// 2) t2 is not a (free/instantiated) variable
			t1.asVariable().setValue(t2);
			return true;
		case COMPUND_TERM_TYPE_ID:
			return t2.isCompoundTerm() && t1.asCompoundTerm().unify(t2.asCompoundTerm());
		case STRING_ATOM_TYPE_ID:
			return t2.isStringAtom() && t1.asStringAtom().sameAs(t2.asStringAtom());
		case FLOAT_VALUE_TYPE_ID:
			return t2.isFloatValue() && t1.asFloatValue().sameAs(t2.asFloatValue());
		case INT_VALUE_TYPE_ID:
			return t2.isIntValue() && t1.asIntValue().sameAs(t2.asIntValue());
		default:
			throw new Error("encountered a term with an unknown type");
		}
	}

	public static final boolean is(Term term, long value) {
		if (term.isVariable()) {
			final Variable hv = term.asVariable().headVariable();
			final Term hvv = hv.getValue();
			if (hvv != null) {
				return hvv.intEval() == value;
			} else {
				hv.setValue(get(value));
				return true;
			}
		} else {
			return term.intEval() == value;
		}
	}

	public abstract Term expose();
}
