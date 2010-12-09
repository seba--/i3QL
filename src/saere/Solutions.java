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
 * FIXME wording of this comment Enables to enumerate over all solutions
 * produced by a predicate. Encoding of the execution semantics of a predicate.
 * 
 * @author Michael Eichberg
 */
public interface Solutions {

	/**
	 * If this method succeeds - i.e., <code>true</code> is returned - at least
	 * one solution exists. The solution is implicitly stored in the variables
	 * of the terms passed to this predicate instance.<br/>
	 * If <code>false</code> is returned, all {@link Variable}s must have the
	 * same state as before the very first call (i.e., it must not be the case
	 * that some variables that were free before the call remain instantiated).
	 * </p> <b><u>After <code>false</code> is returned this method must not be
	 * called again.</u></b>
	 */
	boolean next();

	/**
	 * This method returns <code>true</code>, if - as part of the evaluation of
	 * this predicate - a cut was performed. This can only be the case if this
	 * implementation of this interface is actually the cut operator itself or
	 * if this implementation of this interface models one of the standard
	 * control-flow predicates ("," or ";"). Recall, cuts appearing in the
	 * condition part of ->/2 (if-then(-else)) and *->/2 (soft-cut) or in \+/1
	 * (not) are local to the condition.<br />
	 * If a subtype of this interface models any other then <code>false</code>
	 * has to be returned.
	 * <p>
	 * <b>Control-flow predicates are not allowed to cache the return value of
	 * this method after a successful call to next.</b> For example, given the
	 * following term <code>repeat , ((true;!),true).</code> and imagine that
	 * this is the instance of the first conjunction (and). In this case, we
	 * would first try to prove the left goal (repeat), which will succeed.
	 * After that, we would try to prove the right goal (
	 * <code>(true;!),true</code>). The right goal will also succeed and, hence,
	 * this goal as a whole will succeed. So far, no cut has been performed.
	 * This means, it is possible that there are further solutions. If the user
	 * asks for further solutions (a second solution), we would again try to
	 * prove the left goal, which will succeed again. After that, we will also
	 * try to prove the right goal, which will also succeed. But, in this case,
	 * we a cut operation is performed. Hence, if the user asks for a third
	 * solution, we now know that this predicate (modeling and) cannot 
	 * produce further solutions.
	 * </p>
	 * 
	 * @return <code>true</code>, if a cut was performed while proving this
	 *         goal.
	 */
	boolean choiceCommitted();

}
