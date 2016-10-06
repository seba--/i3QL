/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
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
package idb.lms.extensions.operations

import scala.virtualization.lms.common._
import scala.reflect.SourceContext


/**
 *
 * @author Ralf Mitschke
 */
trait MirrorDefinitions
    extends CastingOpsExp
    with SeqOpsExp
    with StringOpsExp
    with ListOpsExp
	with PrimitiveOpsExp
	with MiscOpsExp
{

    // The following entities were not defined for mirroring in the orignal LMS
    override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
        case RepIsInstanceOf (lhs, mA, mB) => rep_isinstanceof (f (lhs), mA, mB)

        case SeqApply(xs, index) => seq_apply(f(xs), f(index))
        case SeqLength(xs) => seq_length(f(xs))

		case IntBinaryAnd(a1, a2) => int_binaryand(f(a1), f(a2))

        case StringStartsWith(s, prefix) => string_startswith(f(s), f(prefix))
		//case StringSubstring(s, i1, i2) => string_substring(f(s), f(i1), f(i2))
        case ListConcat (l1, l2) => list_concat(f(l1), f(l2))
		case ListHead (l) => list_head(f(l))

		case PrintLn (s) => println(f(s))

        case _ => super.mirror (e, f)
    }).asInstanceOf[Exp[A]]

}
