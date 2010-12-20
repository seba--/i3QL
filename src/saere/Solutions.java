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

import saere.predicate.And2;
import saere.predicate.Cut0;

/**
 * Main interface of all predicate implementations. The primary purpose of this
 * interface is to enable to iterate over all solutions for a given predicate.
 * <p>
 * The programming model is as follows:
 * <ol>
 * <li>After instantiation, the first method that has to be called is the
 * {@link #next()} method.
 * <li>{@link #next()} has to be called until {@link #next()} fails; i.e., until
 * <code>false</code> is returned. After that, neither {@link #next()} nor
 * {@link #abort()} are allowed to be called.</li>
 * <li>If {@link #next()} has failed, {@link #choiceCommitted()} can be called
 * to determine if a cut was performed while evaluating this predicate.
 * <li>If next has not yet failed, but the computation needs to be aborted (due
 * to some cut), then {@link #abort()} has to be called. After that, neither
 * {@link #next()} nor {@link #abort()} are allowed to be called (again).<br/>
 * Note, calling {@link #choiceCommitted()} is meaningless since a cut was the
 * reason for calling {@link #abort()} in the first place. The result of
 * {@link #choiceCommitted()} is undefined if it is called after
 * {@link #abort()}.</li>
 * </ol>
 * </p>
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public interface Solutions {

	/**
	 * If this method succeeds - i.e., <code>true</code> is returned - at least
	 * one solution exists. The solution is implicitly stored in the variables
	 * of the terms passed to this predicate instance.<br/>
	 * If <code>false</code> is returned, all {@link Term}s must have the same
	 * state as before the very first call (i.e., it must not be the case that
	 * some variables that were free before the call remain instantiated). </p>
	 * <b><u>After <code>false</code> is returned this method must not be called
	 * again.</u></b>
	 */
	boolean next();

	/**
	 * Reset the state of all term's to the state before the very first call to
	 * next. It is not allowed to call abort before the first call to
	 * {@link #next()}. It is not allowed to call <code>abort</code> if
	 * {@link #next()} has already returned false.
	 * <p>
	 * This method needs to be called, if the last call to {@link #next()}
	 * returned <code>true</code> and during the evaluation of a parent goal a
	 * cut was performed that cuts this predicate's choice points. If abort is
	 * called, all sub-goals that have not yet failed need also to be aborted.<br />
	 * For example, let's assume the following goal sequence: <code><pre>
	 * a,!,b 
	 * </pre></code>
	 * </p>
	 * Let's further assume that <code>a</code> would succeed more than once. In
	 * this case – i.e., after <code>b</code> has failed – we are not allowed to
	 * call <code>a</code> again. But we have to make sure that the state of the
	 * variables "manipulated" by a is reset, before this predicate as a whole
	 * fails.
	 */
	void abort();

	/**
	 * This method returns <code>true</code>, if - as part of the evaluation of
	 * this predicate - a cut was performed.
	 * <p>
	 * Initially, this can only be the case if this implementation of this
	 * interface is actually the cut operator itself (@link {@link Cut0}).<br />
	 * If this implementation of this interface models one of the standard
	 * control-flow predicates ("," {@link And2}) or ";" {@link Or2}) the
	 * information is passed on the parent goal (and or or).<br/>
	 * Recall, cuts appearing in the condition part of ->/2 (if-then(-else)) and
	 * *->/2 (soft-cut) or in \+/1 (not) are local to the condition.
	 * </p>
	 * <p>
	 * <b>Control-flow predicates are only allowed to call this method after a
	 * call to {@link #next()} has failed.</b> For example, given the following
	 * term <code>repeat , ((true;!),true).</code> and imagine that this is the
	 * instance of the first conjunction (and). In this case, we would first try
	 * to prove the left goal (repeat), which will succeed. After that, we would
	 * try to prove the right goal ( <code>(true;!),true</code>). The right goal
	 * will also succeed and, hence, this goal as a whole will succeed. So far,
	 * no cut has been performed. This means, it is possible that there are
	 * further solutions. If the user asks for the next solution (the second
	 * solution), we would again try to prove the left goal, which will succeed
	 * again. After that, we will also try to prove the right goal, which will
	 * also succeed. But, in this case, a cut operation is performed. Hence, if
	 * the user asks for the third solution, we now know that this predicate
	 * (modeling and) must not produce further solutions.
	 * </p>
	 * 
	 * @return <code>true</code>, if a cut was performed while proving this
	 *         goal.
	 */
	boolean choiceCommitted();

}
