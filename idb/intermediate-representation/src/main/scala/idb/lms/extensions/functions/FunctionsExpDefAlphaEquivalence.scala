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
package idb.lms.extensions.functions

import idb.lms.extensions.equivalence.FunctionsExpAlphaEquivalence
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 *
 */

class FunctionsExpDefAlphaEquivalence
    extends FunctionsExpAlphaEquivalence
{

    override def doLambda[A: Manifest, B: Manifest] (f: Exp[A] => Exp[B])(implicit pos: SourceContext): Exp[A => B] = {

        val (lambdas, defs) = reifySubGraph {
            val eNew = findOrCreateDefinitionExp (doLambdaDef (f), List (pos))
            val eOld = getFirstEquivalentDefinitionExp (eNew)
            (eNew, eOld)
        }

        if( lambdas._1 == lambdas._2) {
            reflectSubGraph(defs)
        }
        lambdas._2
    }

    def getFirstEquivalentDefinitionExp[T] (e: Exp[T]): Exp[T] =
        globalDefs.collectFirst { case TP (sym: Sym[T], _) if sym == e || isEquivalent (sym, e) => sym }.get


    def infix_equivalent[A] (stm: Stm, other: Exp[A]): Option[Sym[A]] =
        stm match {
            case TP (sym: Sym[A], _) if sym == other || isEquivalent (sym, other) => Some (sym)
            case _ => None
        }


    /*
    def toExp[T](s: Def[T]): Exp[T] = findDefinition(s).flatMap(_.defines(s)).get

    def infix_equivalent[A] (stm: Stm, rhs: Def[A]): Option[Sym[A]] =
        stm match {
            case TP (sym: Sym[A], other) if isEquivalent (toExp(rhs), toExp(other)) => Some (sym)
            case _ => None
        }


    def findEquivalentDefinition[T] (d: Def[T]): Option[Stm] =
        globalDefs.find (x => x.equivalent (d).nonEmpty)

    def findEquivalentOrCreateDefinition[T: Manifest] (d: Def[T], pos: List[SourceContext]): Stm =
        findEquivalentDefinition[T](d) map { x => x.defines (d).foreach (_.withPos (pos)); x } getOrElse {
            createDefinition (fresh[T](pos), d)
        }

    def finsEquivalentOrCreateDefinitionExp[T: Manifest] (d: Def[T], pos: List[SourceContext]): Exp[T] =
        findEquivalentOrCreateDefinition (d, pos).defines (d).get
    */
}
