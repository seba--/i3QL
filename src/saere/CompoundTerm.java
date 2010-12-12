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

	@Override
	final public boolean isAtom() {
		return false;
	}

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
	 * Returns <code>true</code> if this term is ground.
	 * <p>
	 * <b>Performance Guideline</b><br/>
	 * This method (re)calculates the answer whenever this method is called. If
	 * you know that the answer is <code>true</code> you should override this
	 * method and return true.
	 * </p>
	 */
	@Override
	public boolean isGround() {
		final int arity = arity();
		for (int i = 0; i < arity; i++) {
			if (!arg(i).isGround())
				return false;
		}

		return true;
	}

	// Caches the information if a compound term is ground.
	private boolean ground = false;

	/**
	 * The state of the compound term's arguments is saved for later recovery.
	 * <p>
	 * <b>Performance Guideline</b><br/>
	 * In general, state manifestation requires a traversal of the complete
	 * compound term's structure. Hence, if all instances of a compound term are
	 * always ground then it is highly recommended to override the
	 * {@link #isGround()} method and to always return <code>true</code>.
	 * Overwriting {@link #isGround()} is more benefical than overwriting this
	 * method.
	 * </p>
	 */
	@Override
	public CompoundTermState manifestState() {
		if (ground)
			return null;
		if (isGround()) {
			ground = true;
			return null;
		} else
			return new CompoundTermState(this);
	}

	/**
	 * The state of the compound term's arguments is restored.
	 * <p>
	 * <b>Performance Guideline</b><br/>
	 * This requires a traversal of the complete compound term's structure.<br />
	 * Hence, if all instances of a compound term are always ground then it is
	 * highly encouraged to overwrite this method and the
	 * {@link #manifestState()} method and to use <code>null</code> to declare
	 * that no immutable state exists.
	 * </p>
	 * 
	 * @see #manifestState()
	 */
	@Override
	public void setState(State state) {
		if (state != null) {
			state.asCompoundTermState().apply(this);
			ground = false;
		}
	}

	/**
	 * Unifies this compound term with another compound term.
	 * <p>
	 * This method does not take care of state handling; i.e, <font
	 * color="ref">both compound terms may be partially bound after
	 * return.</font>
	 * </p>
	 */
	public boolean unify(CompoundTerm other) {
		final int arity = arity();
		if (arity == other.arity() && functor().sameAs(other.functor())) {
			int i = 0;

			while (i < arity) {
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

	@Override
	public String toProlog() {

		String s = "";
		for (int i = 0; i < arity(); i++) {
			if (i > 0)
				s += ", ";
			s += arg(i).toProlog();
		}

		return functor().toProlog() + "(" + s + ")";
	}

}
