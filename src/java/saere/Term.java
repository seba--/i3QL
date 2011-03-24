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
	 * Returns <code>true</code> if this term is identical (as defined by Prolog's "==" operator) to
	 * the given term.
	 */
	@BuiltIn("==/2")
	public static boolean isIdentical(Term term1, Term term2) {
		if (term1 == term2) // to catch some trivial cases...
			return true;

		TermsList terms = TermsList.create(term1, term2);

		do {
			Term t1 = terms.first();
			terms = terms.rest();
			Term t2 = terms.first();
			terms = terms.rest();

			if (t1 == t2)
				return true;

			if (t1.isVariable()) {
				Variable v1fv = t1.asVariable().frontVariable();
				Term v1t = v1fv.getValue();
				t1 = (v1t == null ? v1fv : v1t);
			}
			if (t2.isVariable()) {
				Variable v2fv = t2.asVariable().frontVariable();
				Term v2t = v2fv.getValue();
				t2 = (v2t == null ? v2fv : v2t);
			}
			/* ... t1 and t2 are now either unbound (front) variables or are real terms */
			if (t1 == t2)
				return true; // Handles some cases; in particular, the case that the variables
							 // share.

			switch (t1.termTypeID()) {
			case VARIABLE_TYPE_ID:
				return false; // they don't share; we already checked that!
			case COMPUND_TERM_TYPE_ID:
				final int arity = t1.arity();
				if (t1.arity() == t2.arity() && t1.functor().sameAs(t2.functor())) {
					// prepare the comparison of the arguments
					for (int i = 0; i < arity; i++) {
						terms = new TermsList(t1.arg(i), terms);
						terms = new TermsList(t2.arg(i), terms);
					}
					continue;
				}
				return false;
			case STRING_ATOM_TYPE_ID:
				if (!t2.isStringAtom() || !t1.asStringAtom().sameAs(t2.asStringAtom()))
					return false;
				continue;
			case INT_VALUE_TYPE_ID:
				if (!t2.isIntValue() || !t1.asIntValue().sameAs(t2.asIntValue()))
					return false;
				continue;
			case FLOAT_VALUE_TYPE_ID:
				if (!t2.isFloatValue() || !t1.asFloatValue().sameAs(t2.asFloatValue()))
					return false;
				continue;
			default:
				throw new Error("encountered a term with an unknown type");
			}
		} while (terms != null);

		return true;
	}

	/**
	 * Returns <code>true</code> if this term is ground. String atoms, float values and integer
	 * values are always ground. A complex term is ground if all arguments are ground. A variable is
	 * ground if the variable is bound to a ground term.
	 * 
	 * @return <code>true</code> if this term is ground.
	 */
	@BuiltIn("ground/1")
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
	 *         immutable/if the term is ground..
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
	public abstract boolean isVariable();

	/**
	 * @return <code>true</code> if the type of this term is not a subtype of {@link Variable}.
	 */
	public abstract boolean isNotVariable();

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link Variable} object.
	 */
	public Variable asVariable() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link CompoundTerm}.
	 */
	public abstract boolean isCompoundTerm();

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link CompoundTerm}.
	 */
	public CompoundTerm asCompoundTerm() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this term is a subtype of {@link Atomic}.
	 */
	public abstract boolean isAtomic();

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link StringAtom}.
	 */
	public abstract boolean isStringAtom();

	/**
	 * @return <code>this</code> if this Term is an instance of a {@link StringAtom}.
	 */
	public StringAtom asStringAtom() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of an {@link IntValue}.
	 */
	public abstract boolean isIntValue();

	/**
	 * @return <code>this</code> if this Term is an instance of an {@link IntValue}.
	 */
	public IntValue asIntValue() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link FloatValue}.
	 */
	public abstract boolean isFloatValue();

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
	// FIXME is a functor not actually an atomic...
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
	 * Reveals the true nature of this term. If this term is not a variable it is the term itself.
	 * If it is a variable it is either the bound term or the variable to which we can immediately
	 * bind a value (see {@link Variable#frontVariable()}).
	 * 
	 * @return this term's true nature.
	 */
	public abstract Term reveal();

	/**
	 * @return A textual representation of the term that uses Prolog's syntax. I.e., a Prolog
	 *         compiler should be able to immediately parse the resulting string and the result
	 *         should be the same term.
	 */
	public abstract String toProlog();

	/**
	 * Unifies two terms.
	 * <p>
	 * <font color="red><b> This method does not take care of state handling. It is the
	 * responsibility of the caller to manifest the state of the given terms before calling this
	 * method and to restore the state at the appropriate point in time. </b></font><br />
	 * By moving the responsibility for state handling to the caller various optimizations of how
	 * and when the state is saved/restored are possible
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
			Variable t1fv = t1.asVariable().frontVariable();
			Term t1fvv = t1fv.getValue();
			if (t1fvv == null) {
				// t1 is a free front variable
				if (t2.isVariable()) {
					Variable t2fv = t2.asVariable().frontVariable();
					if (t2fv != t1fv) {
						t1fv.setValue(t2fv);
					}
				} else {
					t1fv.setValue(t2);
				}
				return true;
			} else {
				t1 = t1fvv; // t1 is a term that is not a variable
			}
		}
		// t1 is a term that is not a variable...

		if (t2.isVariable()) {
			Variable t2v = t2.asVariable();
			Variable t2fv = t2v.frontVariable();
			Term t2fvv = t2fv.getValue();
			if (t2fvv == null) {
				// the first case already dealt with sharing
				// if (t2fv != t1) { // this checks that t1 and t2 not already
				// // share
				// // now t2 and t1 either share or t2 is bound to some term
				t2fv.setValue(t1);
				// }
				return true;
			} else {
				// ... t2 is an instantiated variable
				t2 = t2fvv;
			}
		}
		// t2 is an instantiate term

		// minor performance improvement
		if (t1 == t2) {
			return true;
		}

		switch (t1.termTypeID()) {
		// case VARIABLE_TYPE_ID: ... is already dealt with
		case COMPUND_TERM_TYPE_ID:
			return t2.isCompoundTerm() && t1.asCompoundTerm().unify(t2.asCompoundTerm());
		case STRING_ATOM_TYPE_ID:
			return t2.isStringAtom() && t1.asStringAtom().sameAs(t2.asStringAtom());
		case INT_VALUE_TYPE_ID:
			return t2.isIntValue() && t1.asIntValue().sameAs(t2.asIntValue());
		case FLOAT_VALUE_TYPE_ID:
			return t2.isFloatValue() && t1.asFloatValue().sameAs(t2.asFloatValue());
		default:
			throw new Error("encountered a term with an unknown type");
		}
	}

	public final static boolean is(Term term, long value) {
		if (term.isVariable()) {
			final Variable hv = term.asVariable().frontVariable();
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

}
