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

import java.util.WeakHashMap;

// Variables that "share"; example, X = Y,A = B, Y = A, Y = a. => X = a, Y = a, A = a, B = y

public final class Variable extends Term {

	/**
	 * <code>value</code> is:
	 * <ul>
	 * <li><code>null</code> if this variable is free (new)</li>
	 * <li>a value of type {@link Term}</li>
	 * <li>another {@link Variable} if this variable and another variable share.
	 * </li>
	 * </ul>
	 */
	private Term value;

	public Variable() {
		value = null;
	}

	public Variable(Term t) {
		bind(t);
	}

	// IMPROVE If more than two variables share, don't create a chain, but
	// instead attach each new Variable with the head variable. This way the
	// maximum depth is two and manifestation etc. becomes much cheaper!

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public Variable asVariable() {
		return this;
	}

	public State manifestState() {
		if (isInstantiated()) {
			return null;
		} else {
			return new VariableState(this);
		}
	}

	public void setState(State state) {
		// "state == null" means that the variable was already instantiated when
		// manifest state was called the last time
		if (!(state == null)) {
			state.asVariableState().apply(this);
		}
	}

	public int arity() {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.arity();
	}

	public Term arg(int i) {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.arg(i);
	}

	public StringAtom functor() {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.functor();
	}

	public boolean isInstantiated() {
		return (value != null) && (!value.isVariable() || // it is either an
															// atom or a
															// compound term
				value.asVariable().isInstantiated());
	}

	public Term binding() {
		if (value == null) {
			return null;
		} else if (value.isVariable()) {
			return value.asVariable().binding();
		} else {
			return value;
		}
	}

	/**
	 * Clears all bindings of this variable.
	 */
	public void clear() {
		value = null;
	}

	/**
	 * Returns this variable's value.
	 * <p>
	 * Intended to be used by {@link VariableState} only.
	 * </p>
	 */
	Term getValue() {
		return value;
	}

	/**
	 * Sets this variable's value.
	 * <p>
	 * Intended to be used by {@link VariableState} only.
	 * </p>
	 */
	void setValue(Term value) {
		assert ((this.value == null && value != null) || (this.value != null && value == null));
		this.value = value;
	}

	/**
	 * Instantiates this variable.
	 * <p>
	 * It is illegal to call this method if this variable is already
	 * instantiated.
	 * </p>
	 */
	public void bind(Term term) {
		/*
		 * if (value == null) { value = term; } else {
		 * value.asVariable().bind(term); }
		 */
		headVariable().setValue(term);
	}

	/**
	 * Unification of this <b>free</b> variable with another free variable.
	 * These two variables are said to share.
	 * 
	 * @param other
	 *            a Variable with which this variable shares.
	 */
	void share(Variable other) {

		/*
		 * The general idea behind this implementation of sharing is that a
		 * value is always only bound with the head variable. Problems can arise
		 * if we have a cyclic unification of variables. E.g., A = X, X = Y,Y =
		 * A,..., X = 1 In this case we have to make sure that at least one
		 * variable remains free.
		 */
		final Variable ohv = other.headVariable();
		if (!(this.headVariable() == ohv)) {
			ohv.value = this;
		}
	}

	/**
	 * <b>Only to be used if this variable is "conceptually" free.<br />
	 * This is a companion method of {@link #share(Variable)}</b>
	 */
	Variable headVariable() {
		Variable h = this;
		while (!(h.value == null)) {
			h = h.value.asVariable();
		}
		return h;
	}

	public int eval() {
		if (value == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return value.eval();
		}
	}

	public String toString() {
		final Term term = binding();
		if (term == null) {
			return variableToName(this);
		} else {
			return term.toString();
		}
	}

	private static int variableCount = 0;
	private final static WeakHashMap<Variable, String> variableNames = new WeakHashMap<Variable, String>();

	private static String variableToName(Variable variable) {
		synchronized (variableNames) {
			String name = variableNames.get(variable);
			if (name == null) {
				variableCount += 1;
				name = "_V" + variableCount;
				variableNames.put(variable, name);
			}
			return name;
		}
	}

}
