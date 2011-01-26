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
 * <p>
 * A <br />
 * Variables that "share":<br/>
 * <code><pre>
 * X = Y, A = B, Y = A, Y = a. 
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
	 * <li><code>null</code> if this variable is not yet instantiated. However, it is possible that
	 * this variable and another variable do share.</li>
	 * <li>a value of type {@link Term} that is not a sub-type of Variable</li>
	 * if this variable is instantiated.
	 * <li>another {@link Variable} if this variable and the other variable share.</li>
	 * </ul>
	 */
	private Term value;

	public Variable() {
		this.value = null;
	}

	/**
	 * Creates a new Variable and immediately binds it to the given term. If the term is a variable
	 * this variable shares with the other variable.
	 */
	public Variable(Term term) {
		this.value = term;
	}

	@Override
	public int termTypeID() {
		return Term.VARIABLE_TYPE_ID;
	}

	@Override
	public boolean isCompoundTerm() {
		return false;
	}

	@Override
	public boolean isFloatValue() {
		return false;
	}

	@Override
	public boolean isIntValue() {
		return false;
	}

	@Override
	public boolean isStringAtom() {
		return false;
	}

	@Override
	public boolean isNotVariable() {
		return false;
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
	public Variable asVariable() {
		return this;
	}

	@Override
	public State manifestState() {
		// A variable may be bound to a compound term that contains free variables...
		if (this.value == null) {
			return this;
		} else {
			Variable fv = frontVariable();
			Term fvv = fv.value;
			if (fvv == null) {
				return fv;
			} else if (fvv.isAtomic()) {
				return null; // further unifications will not change the value bound to this
							 // variable
			} else {
				return fvv.manifestState();
			}
		}
	}

	@Override
	public int arity() {
		Term fvv = frontVariable().value;
		if (fvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return fvv.arity();
	}

	@Override
	public Term arg(int i) {
		Term fvv = frontVariable().value;
		if (fvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return fvv.arg(i);
	}

	@Override
	public StringAtom functor() {
		Term fvv = frontVariable().value;
		if (fvv == null)
			throw new PrologException("The variable is not sufficiently instantiated.");
		else
			return fvv.functor();
	}
	
	public boolean isInstantiated() {
		return frontVariable().value != null;
	}

	public Term binding() {
		return frontVariable().value;
	}

	@Override
	public boolean isGround() {
		Term fvv = frontVariable().value;
		return fvv != null && fvv.isGround();
	}

	/**
	 * Clears all bindings of this variable.
	 */
	public void clear() {
		this.value = null;
	}

	/**
	 * Returns the value ({@link #Term}) this Variable points-to. This is <code>null</code> if this
	 * variable is not instantiated, another Variable with which this variable shares or a
	 * <code>Term</code> that is not a variable, if this variable is instantiated.
	 */
	public Term getValue() {
		return this.value;
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
	 * If you know that this variable is also a frontVariable, then it is more efficient to just
	 * call {@link #setValue(Term)}. If in doubt, call this method.
	 * </p>
	 */
	public void bind(Term term) {

		assert !this.isInstantiated() : "binding of an instantiated variable";
		assert term != null : "binding to null";
		assert term.isNotVariable() : "variables cannot be bound together, they can only share";

		frontVariable().value = term;
	}

	/**
	 * Unification of this <b>free</b> variable with another free variable. These two variables are
	 * said to share.
	 * 
	 * @param other
	 *            a Variable with which this variable shares.
	 */
	void share(Variable other) {

		assert !isInstantiated() && !other.isInstantiated() : "two variables can only share if both are not bound";

		/*
		 * The general idea behind this implementation of sharing is to chain variables such that
		 * the value field of one variable remains free. Problems that arise if we have a cyclic
		 * unification of variables, e.g., A = X, X = Y,Y = A,..., X = 1 are mitigated. We do make
		 * sure that at least one value field remains free. If more than two variables share, we
		 * don't create a chain, but instead set the value field of each new Variable to the front
		 * variable of an existing variable. This way the maximum depth of the chain is more limited
		 * and manifestation etc. becomes cheaper.
		 */
		if (this.value == null) {
			this.value = other.frontVariable();
		} else if (other.value == null) {
			other.value = this;
		} else {
			final Variable otherFrontVariable = other.frontVariable();

			if (this.frontVariable() != otherFrontVariable) {
				otherFrontVariable.value = this;
			}
		}
	}

	/**
	 * If this variable shares with another variable, it is possible that this variable points to
	 * another variable, which may reference yet another variable, and so on, and so on. The
	 * variable that no longer points to another variable is called the front variable.
	 * <p>
	 * <b>Implementation Note</b> In previous versions, this method was called "headVariable()". We
	 * have renamed this method to avoid confusion with variables that occur in the head of a
	 * clause.
	 * </p>
	 * 
	 * @see #share(Variable)
	 */
	public final Variable frontVariable() {
		Variable fv = this;
		Term fvv = fv.value;
		while (fvv != null) {
			if (fvv.isVariable()) {
				fv = fvv.asVariable();
				fvv = fv.value;
			} else {
				return fv;
			}
		}
		return fv;
	}

	@Override
	public long intEval() {
		final Term fvv = frontVariable().value;
		if (fvv == null) {
			throw new Error("This variable is not sufficiently instantiated.");
		} else {
			return fvv.intEval();
		}
	}

	@Override
	public double floatEval() {
		final Term fvv = frontVariable().value;
		if (fvv == null) {
			throw new PrologException("This variable is not sufficiently instantiated.");
		} else {
			return fvv.floatEval();
		}
	}

	@Override
	public Goal call() {
		final Term hvv = frontVariable().value;
		if (hvv == null) {
			throw new PrologException("This variable is not sufficiently instantiated: "
					+ frontVariable().value.toProlog());
		} else {
			return hvv.call();
		}
	}

	@Override
	public void reincarnate() {
		this.clear();
	}

	@Override
	public Term expose() {
		Variable hv = frontVariable();
		Term hvv = hv.value;
		if (hvv == null) {
			return hv;
		} else
			return hvv;
	}

	@Override
	public String toProlog() {
		final Term hvv = frontVariable().value;
		if (hvv == null) {
			return variableToName(this);
		} else {
			return hvv.toProlog();
		}
	}

	//
	//
	// DEBUGGING RELATED FUNCTIONALITY
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
