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

import saere.PredicateInstanceFactory;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.State;
import saere.StringAtom;
import saere.Term;

/**
 * Implementation of ISO Prolog's member/2 predicate.
 * 
 * <pre>
 * <code>
 * member(X,Y) :- Y = [X|_].
 * member(X,[_|Ys]) :- member(X,Ys).
 * </code>
 * </pre>
 * 
 * @author Michael Eichberg
 */
public final class Member2 implements Solutions {

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(StringAtom.instance("member"), 2,
				new PredicateInstanceFactory() {

					@Override
					public Solutions createPredicateInstance(Term[] args) {
						return new Member2(args[0], args[1]);
					}
				});

	}


	private final Term element;
	private final State elementState;
	private Term list;
	private Term listElement;
	private State listElementState;
	private boolean doTest = true;

	public Member2(final Term element, final Term list) {
		this.element = element;
		this.elementState = element.manifestState();
		this.list = list;
	}

	public boolean next() {
		while (true) {
			if (doTest) {
				if (list.isVariable()) {
					list = list.asVariable().binding();
				}

				if (list.arity() == 2 && list.functor().sameAs(StringAtom.LIST_FUNCTOR)) {
					listElement = list.arg(0);
					listElementState = listElement.manifestState();
					doTest = false;
					if (element.unify(listElement)) {
						return true;
					}
				} else {
					return false;
				}
			} else {
				element.setState(elementState);
				listElement.setState(listElementState);
				list = list.arg(1);
				doTest = true;
			}
		}
	}

	@Override
	public void abort() {
		// nothing to do
	}

	@Override
	public boolean choiceCommitted() {
		return false;
	}
}
