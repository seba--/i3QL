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
package idb.lms.extensions.reduction

import idb.lms.extensions.operations.{SeqOpsExpExt, StringOpsExpExt, OptionOpsExp}
import idb.lms.extensions.print.QuoteFunction

import scala.reflect.SourceContext
import scala.virtualization.lms.common._
import scala.collection.immutable.Map
import idb.lms.extensions.{FunctionUtils, ExpressionUtils}
import idb.lms.extensions.equivalence.EffectExpAlphaEquivalence

/**
 *
 * @author Ralf Mitschke
 *
 */
trait FunctionsExpBetaReduction
    extends FunctionsExp
    with BaseFatExp // required to use the forward transformer
    with EffectExpAlphaEquivalence
    with ExpressionUtils
{

    @SerialVersionUID(42303241L)
    protected val transformer = new ForwardTransformer with Serializable
    {
        val IR: FunctionsExpBetaReduction.this.type = FunctionsExpBetaReduction.this
    }


    override def doApply[A: Manifest, B: Manifest] (
        f: Exp[A => B], x: Exp[A]
    )(
        implicit pos: SourceContext
    ): Exp[B] = {
        f match {
            case Def (Lambda (_, oldX: Rep[A@unchecked], Block (Def (Reify (body, summary, effects))))) =>
                val block = reifyEffects (body)
                val result = transformBlock (substitutions (oldX, x)(manifest[A]), block)
                // adds all old effectful statements that are still referenced to the context, thus a correct Reify node will
                // be created again
                context :::= findSyms(result)(effects.toSet).toList
                // adds all new effectful statements to the context and returns a non-reified version of the sym
                result match {
                    case Def (Reify (inner, _, newEffects)) =>
                        context :::= newEffects
                        inner

                    case _ => result
                }


            case Def (l@Lambda (_, oldX : Rep[A@unchecked], block)) =>
                val subs = substitutions (oldX, x)(manifest[A])
                val r = transformBlock (subs, block)(l.mB.asInstanceOf[Manifest[B]])
                r


            case _ =>
                super.doApply (f, x)

        }
    }

    protected def transformBlock[B: Manifest] (subsitutions: Map[Exp[Any], Exp[Any]], block: Block[B]): Exp[B] = {
        transformer.subst = subsitutions.asInstanceOf[Map[transformer.IR.Exp[Any], transformer.IR.Exp[Any]]]
        val result = transformer.transformBlock (block).res
        transformer.subst = Map ()
        result
    }


    protected def substitutions[A: Manifest] (oldX: Exp[A], x: Exp[A]): Map[Exp[Any], Exp[Any]] = {
        Map (oldX -> x)
    }


    override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext) =
        e match {
            case Apply (s, a) => Apply (f (s), f (a))
            case _ => super.mirror (e, f)
        }


    override def mirrorDef[A:Manifest](e: Def[A], f: Transformer)(implicit pos: SourceContext) =
        e match {
            case Apply (s, a) => Apply (f (s), f (a))
            case _ => super.mirrorDef (e, f)
        }
}
