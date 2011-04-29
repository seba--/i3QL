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
package saere.term;

import static saere.PredicateRegistry.predicateRegistry;
import saere.Goal;
import saere.PredicateIdentifier;
import saere.StringAtom;
import saere.Term;
import saere.PredicateFactoryTwoArgs;

public final class ListElement2 extends TwoArgsCompoundTerm {

	public static final PredicateFactoryTwoArgs FACTORY = (PredicateFactoryTwoArgs) predicateRegistry()
			.getPredicateFactory(new PredicateIdentifier(StringAtom.LIST, 2));

	public ListElement2(Term value, Term rest) {
		super(value, rest);

	}

	public Term getHead() {
		return t1;
	}

	public Term getRest() {
		return t2;
	}

	@Override
	public StringAtom functor() {
		return StringAtom.LIST;
	}

	@Override
	public Goal call() {
		return FACTORY.createInstance(t1, t2);
	}

	@Override
	public String toProlog() {
		return toListRepresentation("[");

	}

	private String toListRepresentation(String head) {
		// FIXME We need to implement this using a while loop to prevent stack overflow errors....
		String newHead = head + getHead().toProlog();
		final Term r = getRest().isVariable() ? getRest().asVariable().binding() : getRest();
		if (r instanceof ListElement2) {
			final ListElement2 le = (ListElement2) r;
			return le.toListRepresentation(newHead + ", ");
		} else if (r == StringAtom.EMPTY_LIST) {
			return newHead + "]";
		} else {
			return newHead + "|" + getRest().toProlog() + "]";
		}
	}

	public String toString() {
		return "ListElement2[head=" + getHead() + "; rest=" + getRest() + "]";
	}
}