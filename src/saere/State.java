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

import saere.predicate.Unify2;

/**
 * Encapsulates the (remaining) mutable state of a term.
 * <p>
 * State manifestation is only necessary if a term is subject to unification. If a term is just
 * passed to another predicate, manifestation of its state is useless. If the subsequent predicate
 * fails, it guarantees that the term's state is the same as before the call. I.e., only if you
 * directly call {@link Term#unify(Term, Term)}, you have to manifest the state and reset it
 * afterwards. If you use the predicate {@link Unify2}, you do not have to take care of state
 * manifestation, but directly calling {@link Unification#unify(Term, Term)} is generally more
 * efficient.
 * </p>
 * <p>
 * <b>Implementation Notes</b><br />
 * <p>
 * The SAE represents the state of ground terms using the value <code>null</code>. Hence, before
 * calling {@link #reset()} you have to check that the state object returned by a call to
 * {@link Term#manifestState()} is not null.
 * </p>
 * <p>
 * This is the state interface of the Memento Pattern.
 * </p>
 * 
 * @author Michael Eichberg (mail@michael-eichberg.de)
 */
public interface State {

    void reset();

}