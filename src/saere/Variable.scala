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

import java.util.WeakHashMap

// Variables that "share"; example, X = Y,A = B, Y = A, Y = a. => X = a, Y = a, A = a, B = y

final class Variable (
	/** 
	 * <code>value</code> is:
	 * <ul>
	 * <li><code>null</code> if this variable is free (new)</li>
	 * <li>a value of type {@link Term}</li>
	 * <li>another {@link Variable} if this variable and another variable share.</li>
	 * </ul> 
	 */
	private var value : Term = null
)extends Term {
	
	// IMPROVE If more than two variables share, don't create a chain, but instead attach each new Variable with the head variable. This way the maximum depth is two and manifestation etc. becomes much cheaper!

	override def isVariable = true
    
	override def asVariable = this

	def manifestState() : State = {
		if (isInstantiated) {
			null
		}
		else {
			new VariableState(this)
		}
	}

	def setState(state : State) {
		// "state == null" means that the variable was already instantiated when
		// manifest state was called the last time
		if (!(state eq null)) { 
			state.asVariableState()(this)
		}
	} 
	
	def arity() = 
		if(value eq null) 
			error("The variable is not sufficiently instantiated.")
		else 
			value.arity() 

	def arg(i : Int) = 
		if(value eq null) 
			error("The variable is not sufficiently instantiated.")
		else 
			value.arg(i) 		

	def functor() : StringAtom = 
		if(value eq null) 
			error("The variable is not sufficiently instantiated.")
		else 
			value.functor() 

	def isInstantiated() : Boolean = {
		!(value eq null) && 
		(
			!value.isVariable || // it is either an atom or a compound term
			value.asVariable.isInstantiated
		)
	}


	def binding : Term = {
		if (value eq null) {
			null
		} else if (value.isVariable) {
			value.asVariable.binding
		} else {
			value
		}
	}
	
	
	/** 
	 * Resets the state of this variable to the state when the object was newly
	 * created.
	 */
	def clear() {
		value = null
	}
	
	
	/**
	 * Returns this variable's value.
	 * <p>
	 * Intended to be used by {@link VariableState} only.
	 * </p>
	 */
	private[saere] def getValue = value

	/**
	 * Sets this variable's value.
	 * <p>
	 * Intended to be used by {@link VariableState} only.
	 * </p>
	 */
	private[saere] def setValue(value : Term) {
		assert(
			(this.value == null && value != null) ||
			(this.value != null && value == null)
		)
		this.value = value 
	}
	

	/** 
	 * Instantiates this variable.
	 * <p>
	 * It is illegal to call this method if this variable is already instantiated.
	 * </p>
	 */
	def bind(term : Term) {
		/*
		if (value eq null) {
			value = term
		} else { 
			value.asVariable.bind(term)
		}  
		*/
		headVariable.setValue(term)
	}


	/**
	 * Unification of this <b>free</b> variable with another free variable. These
	 * two variables are said to share.
	 *  
	 * @param other a Variable with which this variable shares.
	 */
	private[saere] def share(other : Variable) {

		/*
		 The general idea behind this implementation of sharing is that
		 a value is always only bound with the head variable. Problems can arise
		 if we have a cyclic unification of variables. E.g., 
		 A = X, X = Y,Y = A,..., X = 1 
		 In this case we have to make sure that at least one variable remains free.
		 */
		val ohv = other.headVariable
		if (!(this.headVariable eq ohv)) {
			ohv.value = this
		}
	}
	
	/**
	 * <b>Only to be used if this variable is "conceptually" free.<br />
	 * This is a companion method of {@link #share(Variable)}</b>
	 */
	private[saere] def headVariable() : Variable = {
		var h = this
		while (!(h.value eq null)) {
			h = h.value.asVariable
		}
		h
	}
	
    
	override def eval() : Int = {
		if (value eq null) {
			error("This variable is not sufficiently instantiated.")
		} else {
			value.eval
		}
	}


	override def toString() : String = {
		val term : Term = binding
		if (term eq null) { 
			Variable.variableToName(this)
		} else {
			term.toString
		}
	}
}

object Variable {

	private var variableCount = 0	
	private val variableNames = new WeakHashMap[Variable,String]
	
	private[Variable] def variableToName(variable : Variable) : String = {
		variableNames.synchronized{
			var name = variableNames.get(variable)
			if (name eq null){
				variableCount += 1
				name = "_V"+variableCount
				variableNames.put(variable,name)
			}
			name
		}
	}
	
	def apply() = new Variable
	
	def apply(t : Term) : Variable = {
		val v = new Variable(); 
		v.bind(t);
		v
	}
}



