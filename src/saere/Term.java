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

/**
 * Representation of a Prolog term.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class Term {

	public final static Term[] NO_TERMS = new Term[0];

	/**
	 * Unification of this term with the given term. If the unification succeeds
	 * <code>true</code> is returned.
	 * <p>
	 * After return this or the other term's initially free variable may be
	 * (partially) bound. This method does not take care of state handling!
	 * </p>
	 */
	public final boolean unify(Term other) {
		return Term.unify(this, other);
	}

	/**
	 * Returns <code>true</code> if this term is ground. String atome, float
	 * values and integer values are always ground. A complex term is ground if
	 * all arguments are ground. A variable is ground if the variable is bound
	 * to a ground term.
	 * 
	 * @return <code>true</code> if this term is ground.
	 */
	public abstract boolean isGround();

	/**
	 * Creates a {@link State} object that encapsulates the complete variable
	 * state information of this term. The {@link State} object is later on used
	 * to reset the state of this term to the time when this method was called
	 * (to undo all changes that were done in between).
	 * 
	 * <p>
	 * <b>Implementation Note</b><br />
	 * This method implements – in conjunction with {@link #setState(State)} –
	 * the Memento Pattern.
	 * </p>
	 * 
	 * @return An object that encapsulates this term's state. The object's
	 *         precise type is always private to the term object that created
	 *         it. The caller must not make any assumptions about the object's
	 *         structure.
	 *         <p>
	 *         <b>Performance Guideline</b><br />
	 *         It is legal and highly encouraged to return <code>null</code> if
	 *         this term's state is immutable. In this case the corresponding
	 *         {@link #setState(State)} method has to be able to accept
	 *         <code>null</code> as a legal parameter value.
	 *         </p>
	 */
	public abstract State manifestState();

	/**
	 * Resets this term's state to the state encapsulated by the given
	 * <code>State</code> object.
	 * <p>
	 * The given <code>State</code> must be a <code>State</code> object
	 * previously created by this term using {@link #manifestState()}.
	 * </p>
	 */
	public abstract void setState(State state);

	/**
	 * @return <code>true</code> if this Term is an instance of a
	 *         {@link Variable} object.
	 *         <p>
	 *         <b>Prolog Semantics</b><br />
	 *         (Note, this is unrelated to the question whether the variable is
	 *         instantiated / free or not.)
	 *         </p>
	 */
	public boolean isVariable() {
		return false;
	}

	/**
	 * @return <code>true</code> if the type of this term is not a subtype of
	 *         {@link Variable}.
	 */
	public boolean isNotVariable() {
		return true;
	}

	/**
	 * @return <code>this</code> if this term object is an instance of a
	 *         {@link Variable} object.
	 */
	public Variable asVariable() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a
	 *         {@link CompoundTerm}.
	 */
	public boolean isCompoundTerm() {
		return false;
	}

	/**
	 * @return <code>this</code> if this term object is an instance of a
	 *         {@link CompoundTerm}.
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
	 * @return <code>true</code> if this Term is an instance of a
	 *         {@link StringAtom}.
	 */
	public boolean isStringAtom() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of a
	 *         {@link StringAtom}.
	 */
	public StringAtom asStringAtom() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of an
	 *         {@link IntegerAtom}.
	 */
	public boolean isIntegerAtom() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of an
	 *         {@link IntegerAtom}.
	 */
	public IntegerAtom asIntegerAtom() {
		throw new ClassCastException();
	}

	/**
	 * @return <code>true</code> if this Term is an instance of a
	 *         {@link FloatAtom}.
	 */
	public boolean isFloatAtom() {
		return false;
	}

	/**
	 * @return <code>this</code> if this Term is an instance of a
	 *         {@link FloatAtom}.
	 */
	public FloatAtom asFloatAtom() {
		throw new ClassCastException();
	}

	/**
	 * @return The functor of this term. If this term is subtype of atomic, then
	 *         - if necessary - a {@link StringAtom} is created and this
	 *         {@link StringAtom} object is returned.
	 */
	public abstract StringAtom functor();

	/**
	 * @return The arity of this term.
	 */
	public abstract int arity();

	/**
	 * @return The i<i>th</i> argument of this term (zero based).<br/>
	 *         If this term does not have any arguments (
	 *         <code>arity() == 0</code>) an
	 *         <code>IndexOutOfBoundsException</code> is always thrown.
	 *         <p>
	 *         <b>Implementation Note</b><br />
	 *         If this term has at least one argument and <i>i</i> is larger
	 *         than or equal to the arity of the term, then the method is free
	 *         to return the last argument or to throw an
	 *         <code>IndexOutOfBoundsException</code>.
	 *         </p>
	 */
	public abstract Term arg(int i) throws IndexOutOfBoundsException;

	/**
	 * @return Evaluates the arithmetic expression represented by this term, if
	 *         possible.
	 * @throws IllegalStateException
	 *             if this term does not model an arithmetic expression.
	 */
	public long intEval() {
		throw new IllegalStateException("this term (" + this.toString()
				+ ") is not an arithmetic term");
	}

	/**
	 * @return Evaluates the arithmetic expression represented by this term, if
	 *         possible.
	 * @throws IllegalStateException
	 *             if this term does not model an arithmetic expression.
	 */
	public double floatEval() {
		throw new IllegalStateException("this term (" + this.toString()
				+ ") is not an arithmetic term");
	}

	/**
	 * Calls the predicate that corresponds to this term.
	 * 
	 * @return A new instance of the predicate initialized using this term's
	 *         current arguments.
	 */
	public abstract Solutions call();

	/**
	 * @return A textual representation of the term that uses the Prolog syntax.
	 *         I.e., a Prolog compiler should be able to immediately parse the
	 *         resulting string.
	 */
	public abstract String toProlog();

	/**
	 * Unifies two terms.
	 * <p>
	 * <font color="red><b> This method does not take care of state handling. It
	 * is the responsibility of the caller to manifest the state of the given
	 * terms before calling this method and to restore the state at the
	 * appropriate point in time. </b></font><br />
	 * By moving the responsibility for state handling to the caller various
	 * optimizations of how and when the state is saved / restored are possible
	 * </p>
	 * 
	 * @param t1
	 *            The first term.
	 * @param t2
	 *            The second term.
	 * @return <code>true</code> if both terms were successfully unified;
	 *         <code>false</code> otherwise.
	 */
	static boolean unify(Term t1, Term t2) {
		if (t1 == t2) {
			return true;
		}

		// Basically, the Robinson Algorithm
		if (t1.isVariable()) {
			final Variable v1 = t1.asVariable();
			if (v1.isInstantiated()) {
				return unify(v1.binding(), t2);
			} else {
				if (t2.isVariable()) {
					// PERFORMANCE Evaluate if it is more efficient to always
					// just share a free variable with another variable or to
					// bind it.
					final Variable v2 = t2.asVariable();
					if (v2.isInstantiated()) {
						v1.bind(v2.binding());
						return true;
					} else {
						/*
						 * Both variables are not instantiated. We now have to
						 * "link" both variables; because these two variables
						 * are said to share.
						 */
						v1.share(v2);
						return true;
					}
				} else {
					v1.bind(t2);
					return true;
				}
			}
		} else if (t2.isVariable()) { // t1 is not a variable...
			final Variable v2 = t2.asVariable();
			if (v2.isInstantiated()) {
				return unify(v2.binding(), t1);
			} else {
				v2.bind(t1);
				return true;
			}
		} else {
			return (t1.isStringAtom() && t2.isStringAtom() && t1.asStringAtom()
					.sameAs(t2.asStringAtom()))
					|| (t1.isIntegerAtom() && t2.isIntegerAtom() && t1
							.asIntegerAtom().sameAs(t2.asIntegerAtom()))
					|| (t1.isCompoundTerm() && t2.isCompoundTerm() && t1
							.asCompoundTerm().unify(t2.asCompoundTerm()))
					|| (t1.isFloatAtom() && t2.isFloatAtom() && t1
							.asFloatAtom().sameAs(t2.asFloatAtom()));
		}
	}
}
