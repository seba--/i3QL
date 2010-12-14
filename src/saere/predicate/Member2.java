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

import saere.PredicateFactory;
import saere.PredicateIdentifier;
import saere.PredicateRegistry;
import saere.Solutions;
import saere.State;
import saere.StringAtom;
import static saere.StringAtom.LIST_FUNCTOR;
import saere.Term;
import saere.TwoArgsPredicateFactory;

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
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public final class Member2 implements Solutions {

	public final static PredicateIdentifier IDENTIFIER = new PredicateIdentifier(
			StringAtom.instance("member"), 2);

	public final static PredicateFactory FACTORY = new TwoArgsPredicateFactory() {

		@Override
		public Solutions createInstance(Term t1, Term t2) {
			return new Member2(t1, t2);
		}

	};

	public static void registerWithPredicateRegistry(PredicateRegistry registry) {
		registry.register(IDENTIFIER, FACTORY);
	}

	private final static int SETUP = 0;
	private final static int TEST = 1;
	private final static int ADVANCE = 2;

	private final Term element;
	private State elementState;
	private Term list;
	private Term listElement;
	private State listElementState;
	private int state = SETUP;

	public Member2(final Term element, final Term list) {
		this.element = element;
		this.list = list;
	}

	public boolean next() {
		while (true) {
			switch (state) {
			case SETUP:
				elementState = element.manifestState();
				state = TEST;
			case TEST:
				if (list.isVariable()) {
					list = list.asVariable().binding();
				}
				if (list.arity() == 2 && list.functor().sameAs(LIST_FUNCTOR)) {
					listElement = list.arg(0);
					listElementState = listElement.manifestState();
					state = ADVANCE;
					if (element.unify(listElement)) {
						return true;
					}
				} else {
					return false;
				}
			case ADVANCE:
				element.setState(elementState);
				listElement.setState(listElementState);
				list = list.arg(1);
				state = TEST;
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
