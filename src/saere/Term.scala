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
package saere

/**
 * Representation of a term.
 * 
 * @author Michael Eichberg
 */
abstract class Term {

	
	/**
	 * Unification of this term with the given term. If the unification succeeds
	 * true is returned. However, in both cases this or the other term's initially
	 * free variable may be bound. This method does not take of state handling!
	 */
	final def unify(other : Term) : Boolean = Term.unify(this,other)

	
	/**
	 * Creates a {@link State} object that encapsulates the complete variable state information of
	 * this term. The {@link State} object is later on used to reset the state of this term
	 * to the time when this method was called (to undo all changes that were done in between).
	 * <p>
	 * This method in conjunction with {@link #setState(State)} implements the Memento Pattern.
	 * </p>
	 * @return The created state object; can be <code>null</code> if this term's corresponding
	 *  {@link #setState(State)} method accepts <code>null</code> as a legal parameter value.
	 */
	def manifestState() : State
  
	/**
	 * Resets this term's state to the state encapsulated by the given <code>State</code> object.
	 * <p> 
	 * The given <code>State</code> must be a <code>State</code> object previously created by this 
	 * term using {@link #manifestState()}.
	 * </p> 
	 */
	def setState(state : State) : Unit  

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link Variable} object (Note, this is unrelated to
	 * the question whether the variable is instantiated / free or not.)  
	 */
	def isVariable() : Boolean = false

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link Variable} object. 
	 */
	def asVariable() : Variable = throw new ClassCastException()

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link CompoundTerm}.
	 */
	def isCompoundTerm() : Boolean = false

	/**
	 * @return <code>this</code> if this term object is an instance of a {@link CompoundTerm}. 
	 */
	def asCompoundTerm() : CompoundTerm = throw new ClassCastException()

	/**
	 * @return <code>true</code> if this Term is an instance of a {@link StringAtom}.
	 */
	def isStringAtom() : Boolean = false

	/**
	 * @return <code>this</code> if this Term is an instance of a {@link StringAtom}.
	 */
	def asStringAtom() : StringAtom = throw new ClassCastException()

	/**
	 * @return <code>true</code> if this Term is an instance of an {@link IntegerAtom}.
	 */
	def isIntegerAtom() : Boolean = false

	/**
	 * @return <code>this</code> if this Term is an instance of an {@link IntegerAtom}.
	 */	
	def asIntegerAtom() : IntegerAtom = throw new ClassCastException()

	/**
	 * @return The functor of this term. If this term is an atom, then the atom is 
	 * converted to a {@link StringAtom} and this {@link StringAtom} object is 
	 * returned. 
	 */
	def functor() : StringAtom

	/**
	 * @return The arity of this term.
	 */
	def arity() : Int
	
	/**
	 * @return The i<i>th</i> argument of this term (zero based). If this 
	 * term does not have
	 * any arguments (<code>arity() == 0</code>) an {@throws IndexOutOfBoundsException}
	 * is always thrown. If this term has at least one argument and <i>i</i> is
	 * larger than or equal to the aritry of the term, then the method is free to
	 * return the last argument
	 * or to throw an {@throw IndexOutOfBoundsException}.
	 */
	@throws(classOf[IndexOutOfBoundsException])
	def arg(i : Int) : Term

	/**
	 * @return Evaluates the arithmetic expression represented by this term, if possible.
	 * @throws IllegalStateException if this term does not model an arithmetic expression.
	 */
	def eval() : Int = throw new IllegalStateException("This term is not an arithmetic term: "+this.toString)

}
object Term {

	/**
	 * Unifies two terms.
	 * <p> 
	 * <b> This method does not take care* of state handling.
	 * It is the responsibility of the caller to manifest the state of the given
	 * terms before calling this method and to restore the state after calling.
	 * </b> 
	 * This enables optimizations of how and when the state is saved / restored.
	 * </p>
	 * 
	 * @param t1 The first term.
	 * @param t2 The second term.
	 * @return <code>true</code> if both terms were successfully unified; <code>false</code>
	 *  otherwise.
	 */
	protected def unify(t1 : Term, t2 :Term) : Boolean = {
		if (t1 eq t2) {
			return true
		}

		// Basically, the Robinson Algorithm
		if (t1 isVariable) {
			val v1 = t1.asVariable 
			if (v1.isInstantiated) {
				return unify(v1.binding, t2)
			} else {
				if (t2.isVariable) {
					// Performance Evaluate if it is more efficient to always just share a free variable with another variable or to bind it.
					val v2 = t2.asVariable
					if (v2.isInstantiated) {
						v1 bind (v2.binding)
						return true
					} else {
						/* 
	 					 * If two variables, e.g. X = Y, that are both not instantiated then we have to 
						 * "link" both variables, because in this case Y is just an alias for X.
						 */
						v1 share v2
						return true
					}
				} else {
					v1 bind t2
					return true
				}
			}
		} else if (t2 isVariable) { // t1 is not a variable...
			val v2 = t2.asVariable 
			if (v2.isInstantiated) {
				return unify(v2.binding, t1)
			} else {
				v2 bind t1
				return true
			}
		} else {
			(t1.isStringAtom && t2.isStringAtom && t1.asStringAtom.sameAs(t2.asStringAtom)) ||
			(t1.isIntegerAtom && t2.isIntegerAtom && t1.asIntegerAtom.sameAs(t2.asIntegerAtom)) ||
			(t1.isCompoundTerm && t2.isCompoundTerm && ((t1.asCompoundTerm).unify(t2.asCompoundTerm))) 
		}
	}
}