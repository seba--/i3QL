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
 * A compound term is a term with at least one argument.
 * 
 * @author Michael Eichberg
 */
public abstract class CompoundTerm extends Term {

	/**
	 * @return <code>true</code> - always.
	 */
	@Override
	final public boolean isCompoundTerm() {
		return true;
	}

	/**
	 * @return <code>this</code> - always.
	 */
	@Override
	final public CompoundTerm asCompoundTerm() {
		return this;
	}

	/**
	 * The state of the compound term's arguments is saved for later recovery.
	 * <p>
	 * This requires a traversal of the complete compound terms structure.<br />
	 * Hence, if all instances of a compound term are always ground then it is
	 * highly encouraged to overwrite this method (and {@link #setState(State)})
	 * to avoid traversing the state of a term.
	 * </p>
	 */
	public State manifestState() {
		return new CompoundTermState(this);
	}

	/**
	 * The state of the compound term's arguments is restored.
	 * <p>
	 * This requires a traversal of the complete compound term's structure.<br />
	 * Hence, if all instances of a compound term are always ground then it is
	 * highly encouraged to overwrite this method (and {@link #manifestState()})
	 * to avoid traversing the state of a term.
	 * </p>
	 */
	public void setState(State state) {
		state.asCompoundTermState().apply(this);
	}

	/**
	 * Unifies this compound term with another compound term.
	 * <p>
	 * This method does not take care of state handling.
	 * </p>
	 */
	public boolean unify(CompoundTerm other) {
		if (this.arity() == other.arity()
				&& this.functor().equals(other.functor())) {
			int i = 0;
			final int a = arity();
			while (i < a) {
				if ((this.arg(i)).unify(other.arg(i))) {
					i += 1;
				} else {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}
}
