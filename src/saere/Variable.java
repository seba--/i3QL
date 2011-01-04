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
 * Variables that "share":<br/>
 * <code><pre>
 * X = Y,A = B, Y = A, Y = a. 
 * =>
 * X = a, Y = a, A = a, B = a
 * </pre></code>
 * </p>
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Variable extends Term implements State {

	/**
	 * <code>value</code> is:
	 * <ul>
	 * <li><code>null</code> if this variable is free (new); i.e., the variable is not instantiated
	 * and does not share.</li>
	 * <li>a value of type {@link Term} that is not a subtype of Variable</li>
	 * if this variable is instantiated.
	 * <li>another {@link Variable} if this variable and another variable share.</li>
	 * </ul>
	 */
	private Term value;

	public Variable() {
		value = null;
	}

	public Variable(Term term) {
		this.value = term;
	}

	@Override
	public boolean isAtomic() {
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
	public State manifestState() {
		// Remember that a variable may be bound to a compound term that
		// contains free variables...
		if (this.value == null) {
			return this;
		} else {
			Variable hv = headVariable();
			Term hvv = hv.value;
			if (hvv == null) {
				return hv;
			} else if (hvv.isAtomic()) {
				return null;
			} else {
				return hvv.manifestState();
			}
		}
	}

	@Override
	public int arity() {
		Term hvv = headVariable().value;
		if (hvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return hvv.arity();
	}

	@Override
	public Term arg(int i) {
		Term hvv = headVariable().value;
		if (hvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return hvv.arg(i);
	}

	@Override
	public StringAtom functor() {
		Term hvv = headVariable().value;
		if (hvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return hvv.functor();
	}

	public boolean isInstantiated() {
		return headVariable().value != null;
	}

	public Term binding() {
		return headVariable().value;
	}

	@Override
	public boolean isGround() {
		Term hvv = headVariable().value;
		return hvv != null && hvv.isGround();
	}

	/**
	 * Clears all bindings of this variable.
	 */
	public void clear() {
		value = null;
	}

	/**
	 * Returns this variable's value.
	 */
	public Term getValue() {
		return value;
	}

	/**
	 * Sets this variable's value.
	 */
	public void setValue(Term value) {
		this.value = value;
	}

	/**
	 * Instantiates this variable.
	 * <p>
	 * It is illegal to call this method if this variable is already instantiated.
	 * </p>
	 * <p>
	 * <b>Performance Guideline</b><br />
	 * If you know that this variable is also a headVariable, then it is more efficient to just call
	 * {@link #setValue(Term)}. If in doubt, call this method.
	 * </p>
	 */
	public void bind(Term term) {

		assert !this.isInstantiated() : "binding of an instantiated variable";
		assert term != null : "binding to null";
		assert term.isNotVariable() : "variables cannot be bound together, they can only share";

		headVariable().value = term;
	}

	/**
	 * Unification of this <b>free</b> variable with another free variable. These two variables are
	 * said to share.
	 * 
	 * @param other
	 *            a Variable with which this variable shares.
	 */
	@Deprecated
	void share(Variable other) {

		assert !isInstantiated() && !other.isInstantiated() : "two variables can only share if both are not bound";

		/*
		 * The general idea behind this implementation of sharing is that a value is always only
		 * bound with the head variable. Problems that arise if we have a cyclic unification of
		 * variables, e.g., A = X, X = Y,Y = A,..., X = 1 are mitigated. We do make sure that at
		 * least one variable remains free. If more than two variables share, we don't create a
		 * chain, but instead attach each new Variable with the head variable. This way the maximum
		 * depth of the chain is more limited and manifestation etc. becomes cheaper.
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
	 * If this variable shares with another variable, it is possible that this variable points to
	 * another variable.
	 * 
	 * @see #share(Variable)
	 */
	public final Variable headVariable() {
		Variable hv = this;
		Term hvv = hv.value;
		while (hvv != null) {
			if (hvv.isVariable()) {
				hv = hvv.asVariable();
				hvv = hv.value;
			} else {
				return hv;
			}
		}
		return hv;
	}

	@Override
	public long intEval() {
		final Term hvv = headVariable().value;
		if (hvv == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return hvv.intEval();
		}
	}

	@Override
	public double floatEval() {
		final Term hvv = headVariable().value;
		if (hvv == null) {
			throw new PrologException("This variable is not sufficiently instantiated.");
		} else {
			return hvv.floatEval();
		}
	}

	@Override
	public int termTypeID() {
		return Term.VARIABLE;
	}

	@Override
	public Goal call() {
		final Term hvv = headVariable().value;
		if (hvv == null) {
			throw new PrologException("This variable is not sufficiently instantiated: "
					+ headVariable().value.toProlog());
		} else {
			return hvv.call();
		}
	}

	@Override
	public void reincarnate() {
		this.clear();
	}

	@Override
	public Term unwrap() {
		Variable hv = headVariable();
		Term hvv = hv.value;
		if (hvv == null) {
			return hv;
		} else
			return hvv;
	}

	@Override
	public String toProlog() {
		final Term hvv = headVariable().value;
		if (hvv == null) {
			return variableToName(this);
		} else {
			return hvv.toProlog();
		}
	}

	//
	//
	// D E B U G G I N G   R E L A T E D   F U N C T I O N A L I T Y 
	//
	//
	
	@Override
	public String toString() {
		return "Variable[id=" + variableToName(this) + "; value=" + value + "]";
	}

	private static int variableCount = 0;
	private final static WeakHashMap<Variable, String> variableNames = new WeakHashMap<Variable, String>();

	static String variableToName(Variable variable) {
		synchronized (variableNames) {
			// if (variableNames.isEmpty()) {
			// variableCount = 0;
			// }
			String name = variableNames.get(variable);
			if (name == null) {
				variableCount += 1;
				name = "V" + variableCount;
				variableNames.put(variable, name);
			}
			return name;
		}
	}

}
