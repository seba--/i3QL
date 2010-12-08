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
package saere.meta;

import saere.*;


/**
 * <p>
 * Implements the overall strategy how to evaluate a predicate that is 
 * defined using multiple clauses.<br />
 * Clauses with cut operators are not supported. 
 * </p>
 * <b>Please note, that this class is not used by the compiler as the 
 * resulting code would not be efficient enough.</b> The primary use 
 * case of this class is to help you understand the evaluation strategy employed
 * by SAE Prolog. 
 * 
 * @author Michael Eichberg
 */
@Deprecated
public abstract class MultipleRules implements Solutions {

	public abstract int ruleCount();
	
	public abstract Solutions rule(int i); 
		
	private Solutions solutions = rule(1); 

	private int currentRule = 1;
	
	public final boolean next()  {
		while (currentRule <= ruleCount()) {
			if (solutions.next()) {
				return true;
			}
			else {
				currentRule += 1;
				if (currentRule <= ruleCount()) {
					solutions = rule(currentRule);
				}
				// else {
				// 	solutions = null
				// }
			}
		}
		return false;
	}
}