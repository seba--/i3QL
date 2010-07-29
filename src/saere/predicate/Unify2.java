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
package saere.predicate;

import saere.*;


/**
 * Implementation of SAE Prolog's <code>=</code> operator (unify).
 * <p>
 * This implementation generates a choice point and – in general – should not be called.<br />
 * <i>It is only intended to be used to execute meta-level calls. </i><br />
 * The compiler has specific support for this operator and does not make 
 * use of this class.
 * </p>
 *  
 * @author Michael Eichberg
 */
public class Unify2  {

		
	/**
	 * Implementation of the unify (=) operator. 
	 */
	public static Solutions apply(final Term a1, final Term a2) {
	

		return new Solutions() {
			
			private final State a1State = a1.manifestState();
			private final State a2State = a2.manifestState();
			
			private boolean called = false;

			public boolean next() {
				if (!called) {
					called = true;
					if (a1.unify(a2)) {
						return true;
					}
				}
				a1.setState(a1State);
				a2.setState(a2State);
				return false;
			}
		};
	}
}

