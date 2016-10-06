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
package idb.lms.extensions.equivalence

import scala.virtualization.lms.common.BaseExp
import scala.collection.mutable
import scala.reflect.SourceContext
import scala.virtualization.lms.internal.Effects

/**
 *
 * @author Ralf Mitschke
 *
 */

trait BaseExpAlphaEquivalence
    extends BaseExp
    with BaseAlphaEquivalence
    with Effects
{

    type VarExp[+T] = Sym[T]

    def isEquivalent[A, B] (a: Exp[A], b: Exp[B])(implicit renamings: VariableRenamings): Boolean =
        (a, b) match {
            case (Const (x), Const (y)) => x == y
            //case (Variable (x), Variable (y)) => isEquivalent (x, y) // TODO is this type of expression even created?

            case (Def (Reflect (x1, summary1, deps1)), Def (Reflect (x2, summary2, deps2))) =>
                isEquivalentDef (x1, x2) && isEquivalentSeq (deps1, deps2)
                // For now we ignore the dependencies, since due to beta reduction we can create functions
                // that do not resolve the dependencies as they would be resolved during standard creation.
                // This happens in function that computes two reflected effects, e.g., a and b in
                // f(x){
                //   a(x) && b(x)
                // }
                // Original LMS wil determine List(a) as dependencies of b.
                // When we create this as a composition of functions the dependency is not there, e.g.,
                // g(x) = { a(x) }
                // h(x) = { b(x) }
                // f(x) = { g(x) && h(x) }

            case (Def (Reify (x1, summary1, effects1)), Def (Reify (x2, summary2, effects2))) =>
                isEquivalent (x1, x2) //&& isEquivalentSeq (effects1, effects2)
                // For now we ignore the effects.
                // They should be traversed anyway, when their reflected expressions are met in the AST

            case (Def (x), Def (y)) => isEquivalentDef (x, y)

            case (x: Sym[_], y: Sym[_]) => x == y || renamings.canRename (x, y)

            case _ => false
        }

    def isEquivalentDef[A, B] (a: Def[A], b: Def[B])(implicit renamings: VariableRenamings): Boolean =
        if (a.getClass.isAssignableFrom (b.getClass) || b.getClass.isAssignableFrom (a.getClass)) {
            throw new IllegalArgumentException ("Expression types are unknown to alpha equivalence: " + a + " =?= " + b)
        }
        else
        {
            false
        }


    class MultiMapVariableRenamings ()
        extends VariableRenamings
    {

        private val map: mutable.MultiMap[VarExp[Any], VarExp[Any]] =
            new mutable.HashMap[VarExp[Any], mutable.Set[VarExp[Any]]] with mutable.MultiMap[VarExp[Any], VarExp[Any]]

        def add[T] (x: VarExp[T], y: VarExp[T]): VariableRenamings = {
            map.addBinding (x, y)
            map.addBinding (y, x)
            this
        }

        def canRename[T] (x: VarExp[T], y: VarExp[T]): Boolean = {
            map.entryExists (x, _ == y)
        }

    }

    def emptyRenaming = new MultiMapVariableRenamings ()

    /**
     * Create a definition or find an equivalent one
     */
    def createOrFindEquivalent[T: Manifest] (doCreate: => Def[T])(implicit pos: SourceContext): Exp[T] = {
        // with reifySubGraph the whole reification is done without saving all nodes
        val ((createdExp, equivalentExp), defs) = reifySubGraph {
            val eNew = findOrCreateDefinitionExp (doCreate, List (pos))
            val eOld = getFirstEquivalentDefinitionExp (eNew)
            (eNew, eOld)
        }
        // if we have created a new function, i.e., not found another alpha equivalent one, save the nodes
        // saving is done by reflectSubGraph
        if (createdExp == equivalentExp) {
            reflectSubGraph (defs)
        }
        equivalentExp
    }

    /**
     * Find the first equivalent definition, assumes at least the exp itself is stored as a definition
     */
    def getFirstEquivalentDefinitionExp[T] (e: Exp[T]): Exp[T] =
        globalDefs.collectFirst { case TP (sym: Sym[T], _) if sym == e || isEquivalent (sym, e) => sym }.get


}
