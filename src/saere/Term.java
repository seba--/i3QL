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
 * Representation of a term.
 * 
 * @author Michael Eichberg
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
		return Unification.unify(this, other);
	}

	/**
	 * Creates a {@link State} object that encapsulates the complete variable
	 * state information of this term. The {@link State} object is later on used
	 * to reset the state of this term to the time when this method was called
	 * (to undo all changes that were done in between).
	 * <p>
	 * This method implements – in conjunction with {@link #setState(State)} –
	 * the Memento Pattern.
	 * </p>
	 * 
	 * @return The created state object; can be <code>null</code> if this term's
	 *         corresponding {@link #setState(State)} method accepts
	 *         <code>null</code> as a legal parameter value and no state
	 *         information needs to be preserved.
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
	 *         {@link Variable} object (Note, this is unrelated to the question
	 *         whether the variable is instantiated / free or not.)
	 */
	public boolean isVariable() {
		return false;
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
	 * @return The functor of this term. If this term is an atom, then the atom
	 *         is converted to a {@link StringAtom} and this {@link StringAtom}
	 *         object is returned.
	 */
	public abstract StringAtom functor();

	/**
	 * @return The arity of this term.
	 */
	public abstract int arity();

	/**
	 * @return The i<i>th</i> argument of this term (zero based). If this term
	 *         does not have any arguments (<code>arity() == 0</code>) an
	 *         <code>IndexOutOfBoundsException</code> is always thrown. If this
	 *         term has at least one argument and <i>i</i> is larger than or
	 *         equal to the aritry of the term, then the method is free to
	 *         return the last argument or to throw an
	 *         <code>IndexOutOfBoundsException</code>.
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
	
	
	public Solutions call() {
		throw new IllegalStateException("this term (" + this.toString()
				+ ") cannot be called");
	}

}
