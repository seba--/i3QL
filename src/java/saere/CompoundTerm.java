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
 * A compound term is a term with a functor and at least one argument.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class CompoundTerm extends Term {

	@Override
	public final int termTypeID() {
		return Term.COMPUND_TERM_TYPE_ID;
	}

	/**
	 * @return <code>false</code>; always.
	 */
	@Override
	public final boolean isAtomic() {
		return false;
	}

	@Override
	public final boolean isFloatValue() {
		return false;
	}

	@Override
	public final boolean isIntValue() {
		return false;
	}

	@Override
	public final boolean isVariable() {
		return false;
	}

	@Override
	public boolean isNotVariable() {
		return true;
	}

	@Override
	public boolean isStringAtom() {
		return false;
	}

	/**
	 * @return <code>true</code>; always.
	 */
	@Override
	public final boolean isCompoundTerm() {
		return true;
	}

	/**
	 * @return Always returns "<code>this</code>"
	 */
	@Override
	public final CompoundTerm asCompoundTerm() {
		return this;
	}

	public abstract Term firstArg(); // every compound term has at least one arg

	public Term secondArg() {
		throw new IndexOutOfBoundsException();
	}

	/**
	 * Returns <code>true</code> if this term is ground; i.e. if all arguments are ground.
	 * 
	 * <p>
	 * <b>Performance Guideline</b><br/>
	 * This method (re)calculates the answer whenever this method is called. If you know that for
	 * your specific kind of compound term the answer is always <code>true</code> you should
	 * override this method and return <code>true</code> (see also {@link #manifestState()}).
	 * </p>
	 */
	@Override
	public boolean isGround() {
		final int arity = this.arity();
		for (int i = 0; i < arity; i++) {
			if (!arg(i).isGround()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The state of the compound term's arguments is saved for later recovery.
	 * <p>
	 * <b>Performance Guideline</b><br/>
	 * In general, state manifestation requires a traversal of the complete compound term's
	 * structure. Hence, if all instances of a compound term are always ground then it is highly
	 * recommended to override the corresponding {@link #isGround()} method and to always return
	 * <code>true</code>. Overwriting {@link #isGround()} is more beneficial than overwriting this
	 * method, because this method immediately calls the method {@link #isGround()} and returns
	 * <code>null</code> if this compound term is ground.
	 * </p>
	 */
	@Override
	public State manifestState() {
		// TODO reevaluate if the "isGround" test is still meaningful if we have completed the
		// compiler support for variable dereferencing
		if (isGround()) {
			return null;
		}

		CompoundTerm compoundTerm = this;

		// Compared to the recursive implementation,
		// this implementation is ~5-10% faster (overall!).
		CompoundTermsList workList = null;
		States states = null;

		do {
			final int arity = compoundTerm.arity();
			/* for_each_argument: */for (int i = 0; i < arity; i++) {
				// We needed to integrate the state handling of
				// variables here, to avoid stack overflow errors.
				// E.g., if a compound term is bound to a variable
				// the manifestation of the variable would lead to
				// another call of the manifest method. If we now
				// manifest a large data structure (e.g., a long list)
				// which contains a large number of (bound) variables
				// this easily leads to a stack overflow error.
				Term arg_i = compoundTerm.arg(i).reveal();
				switch (arg_i.termTypeID()) {
				case Term.VARIABLE_TYPE_ID:
					Variable variable = arg_i.asVariable();
					Term value = variable.getValue();
					if (value == null) {
						states = new States(variable/* .manifestState() */, states);
					}
					break;

				case Term.COMPUND_TERM_TYPE_ID:
					workList = new CompoundTermsList(arg_i.asCompoundTerm(), workList);
					break;
				}
			}
			if (workList != null) {
				compoundTerm = workList.first();
				workList = workList.rest();
			} else {
				compoundTerm = null;
			}
		} while (compoundTerm != null);

		return states;
	}

	/**
	 * Unifies this compound term with another compound term.
	 * <p>
	 * This method does not take care of state handling; i.e, <font color="ref">both compound terms
	 * may be (partially) bound when this method returns.</font> The caller must take care of state
	 * manifestation and state reincarnation of this compound term as well as the passed in compound
	 * term.
	 * </p>
	 */
	public final boolean unify(CompoundTerm other) {
		final int arity = arity();
		if (arity == other.arity() && functor().sameAs(other.functor())) {
			int i = 0;
			do { // a compound term always has at least one argument
				if (this.arg(i).unify(other.arg(i))) {
					i += 1;
				} else {
					return false;
				}
			} while (i < arity);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public final Term reveal() {
		return this;
	}

	@Override
	public final boolean equals(Object otherObject) {
		if (otherObject instanceof CompoundTerm) {
			CompoundTerm other = (CompoundTerm) otherObject;
			int arity = arity();
			if (arity == other.arity() && functor().sameAs(other.functor())) {
				for (int i = 0; i < arity; i++) {
					if (!this.arg(i).equals(other.arg(i)))
						return false;
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public final int hashCode() {
		return functor().hashCode() + arity();
	}

	@Override
	public String toProlog() {
		// a compound term always has at least one argument
		String s = arg(0).toProlog();
		for (int i = 1; i < arity(); i++) {
			if (i > 0)
				s += ", ";
			s += arg(i).toProlog();
		}
		return functor().toProlog() + "(" + s + ")";
	}

}
