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

/**
 * Representation of a Prolog variable.
 * 
 * <p>
 * Variables that "share"; example:
 * 
 * <pre>
 * <code>
 * 	X = Y,A = B, Y = A, Y = a. 
 * =>
 *  X = a, Y = a, A = a, B = a
 * </code>
 * </pre>
 * 
 * </p>
 * 
 * @author Michael Eichberg
 */
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

	@Override
	public boolean isAtom() {
		return false;
	}

	@Override
	public boolean isVariable() {
		return true;
	}

	@Override
	public boolean isNotVariable() {
		return false;
	}

	@Override
	public Variable asVariable() {
		return this;
	}

	@Override
	public VariableState manifestState() {
		// Remember that a variable may be bound to a compound term that
		// contains free variables...
		if (value != null) {
			Variable hv = headVariable();
			if (hv.value == null) {
				return VariableState.share(hv);
			} else if (hv.value.isAtom())
				return VariableState.immutable;
			else
				return VariableState.instantiated(hv);
		} else {
			return null;
		}
	}

	@Override
	public void setState(State state) {
		if (state == null)
			value = null;
		else
			state.asVariableState().apply(this);
	}
	
	
	void setState(VariableState state) {
		if (state == null)
			value = null;
		else
			state.apply(this);
	}

	@Override
	public int arity() {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.arity();
	}

	@Override
	public Term arg(int i) {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.arg(i);
	}

	@Override
	public StringAtom functor() {
		if (value == null)
			throw new Error("The variable is not sufficiently instantiated.");
		else
			return value.functor();
	}

	public boolean isInstantiated() {
		return value != null
				&& (value.isNotVariable() || value.asVariable()
						.isInstantiated());
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

	@Override
	public boolean isGround() {
		Variable hv = headVariable();
		Term hvValue = hv.value;
		return hvValue != null && hvValue.isGround();
	}

	/**
	 * Clears all bindings of this variable.
	 */
	void clear() {
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
		assert (this.value == null && value != null)
				|| (this.value != null && value == null) : "precondition of variable.setValue(term) not met";
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

		assert (!this.isInstantiated()) : "binding of an instantiated variable";
		assert (term != null) : "binding to null";

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

		assert !isInstantiated() && !other.isInstantiated() : "two variables can only share if both are not bound";

		/*
		 * The general idea behind this implementation of sharing is that a
		 * value is always only bound with the head variable. Problems that
		 * arise if we have a cyclic unification of variables, e.g., A = X, X =
		 * Y,Y = A,..., X = 1 are mitigated. We do make sure that at least one
		 * variable remains free. If more than two variables share, don't create
		 * a chain, but instead attach each new Variable with the head variable.
		 * This way the maximum depth of the chain is more limited and
		 * manifestation etc. becomes cheaper.
		 */
		if (value == null) {
			value = other.headVariable();
		} else if (other.value == null) {
			other.value = this;
		} else {
			final Variable otherHeadVariable = other.headVariable();

			if (this.headVariable() != otherHeadVariable) {
				otherHeadVariable.value = this;
			}
		}
	}

	/**
	 * If this variable shares with another variable, it is possible
	 * that this variable points to another variable which is then
	 * responsible
	 * This is a companion method of {@link #share(Variable)}</b>
	 */
	Variable headVariable() {
		Variable h = this;
		while (h.value != null && h.value.isVariable()) {
			h = h.value.asVariable();
		}
		return h;
	}

	@Override
	public long intEval() {
		if (value == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return value.intEval();
		}
	}

	@Override
	public double floatEval() {
		if (value == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return value.floatEval();
		}
	}

	@Override
	public Solutions call() {
		if (value == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return value.call();
		}
	}

	@Override
	public String toProlog() {
		final Term term = binding();
		if (term == null) {
			return variableToName(this);
		} else {
			return term.toProlog();
		}
	}

	private static int variableCount = 0;
	private final static WeakHashMap<Variable, String> variableNames = new WeakHashMap<Variable, String>();

	static String variableToName(Variable variable) {
		synchronized (variableNames) {
			String name = variableNames.get(variable);
			if (name == null) {
				variableCount += 1;
				name = "V" + variableCount;
				variableNames.put(variable, name);
			}
			return name;
		}
	}

	@Override
	public String toString() {
		return "Variable[id=" + variableToName(this) + "; value=" + value + "]";
	}

}
