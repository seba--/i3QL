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
 * Common interface implemented by {@link Term}s that represent atomic
 * information. I.e., string atoms, float values and integer values.
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public abstract class Atomic extends Term {

	/**
	 * @return <code>true</code>; atomic information are by definition always
	 *         ground.
	 */
	@Override
	public final boolean isGround() {
		return true;
	}

	/**
	 * @return 0. By definition the arity of some atomic information is always
	 *         0.
	 */
	@Override
	public final int arity() {
		return 0;
	}

	/**
	 * Will always throw an <code>IndexOutOfBoundsException</code>, because
	 * atoms do not have arguments.
	 * 
	 * @param i
	 *            <i>"ignored"</i>.
	 * @throws IndexOutOfBoundsException
	 *             always.
	 */
	@Override
	public final Term arg(int i) {
		throw new IndexOutOfBoundsException("atoms have no arguments");
	}

	/**
	 * @return <code>null</code>; an atomic information's state is immutable
	 *         and, hence, no state information needs to be preserved.<br/>
	 */
	/*
	 * In general, the compiler tries to avoid explicit manifestation of an
	 * Atom's state. This – i.e., avoiding useless calls to manifestState –
	 * however, requires deep static analyses.
	 */
	@Override
	public final State manifestState() {
		return null;
	}

	@Override
	public final Term expose() {
		return this;
	}

}
