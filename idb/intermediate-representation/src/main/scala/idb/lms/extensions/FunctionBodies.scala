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
package idb.lms.extensions


/**
 *
 * @author Ralf Mitschke
 */
trait FunctionBodies
    extends FunctionCreator
{

    import IR._


    def unify[T: Manifest, B: Manifest] (
        f: (Rep[B], Rep[B]) => Rep[B]
    )(
        xa: Rep[T], xb: Rep[T]
    )(
        ba: Option[Rep[B]], bb: Option[Rep[B]]
    )(
        asUnique: Boolean = false
    ): (Rep[T], Option[Rep[B]]) = {
        if (!ba.isDefined)
            return (xb, bb)
        if (!bb.isDefined)
            return (xa, ba)

        if (asUnique) {
            val x = IR.fresh[T]
            subst = Map (xa -> x, xb -> x)

            val baS = transformBlock (reifyEffects (ba.get)).res
            val bbS = transformBlock (reifyEffects (bb.get)).res

            if(baS == bbS)
            // blocks are the same
                return (xa, ba)
            if (!IR.findSyms (baS)(Predef.Set (bbS)).isEmpty)
            // block b is contained in block a
                return (xa, ba)
            if (!IR.findSyms (bbS)(Predef.Set (baS)).isEmpty)
            // block a is contained in block b
                return (xb, bb)
        }


        if (xa == xb)
            return (xa, Some (f (ba.get, bb.get)))

        // substitute xb with xa
        subst = Map (xb -> xa)
        val bbAsXa = transformBlock (reifyEffects (bb.get)).res
        subst = Map ()
        (xa, Some (f (ba.get, bbAsXa)))
    }

    case class FunctionBodies1[T1: Manifest, B: Manifest] (x1: Rep[T1], b1: Option[Rep[B]])
    {
        def manifestB = implicitly[Manifest[B]]

        def combineWith (
            f: (Rep[B], Rep[B]) => Rep[B]
        )(
            other: FunctionBodies1[T1, B]
        )(
            asUnique: Boolean = false
        ): FunctionBodies1[T1, B] = {
            val (x1New, b1New) = unify (f)(this.x1, other.x1)(this.b1, other.b1)(asUnique)
            FunctionBodies1 (x1New, b1New)
        }
    }


    case class FunctionBodies2[T1: Manifest, T2: Manifest, B: Manifest] (
        x1: Rep[T1], b1: Option[Rep[B]],
        x2: Rep[T2], b2: Option[Rep[B]]
    )
    {
        def manifestB = implicitly[Manifest[B]]

        def combineWith (
            f: (Rep[B], Rep[B]) => Rep[B]
        )(
            other: FunctionBodies2[T1, T2, B]
        )(
            asUnique: Boolean = false
        ): FunctionBodies2[T1, T2, B] = {
            val (x1New, b1New) = unify (f)(this.x1, other.x1)(this.b1, other.b1)(asUnique)
            val (x2New, b2New) = unify (f)(this.x2, other.x2)(this.b2, other.b2)(asUnique)
            FunctionBodies2 (x1New, b1New, x2New, b2New)
        }
    }

    case class FunctionBodies3[T1: Manifest, T2: Manifest, T3: Manifest, B: Manifest] (
        x1: Rep[T1], b1: Option[Rep[B]],
        x2: Rep[T2], b2: Option[Rep[B]],
        x3: Rep[T3], b3: Option[Rep[B]]
    )
    {
        def manifestB = implicitly[Manifest[B]]

        def combineWith (
            f: (Rep[B], Rep[B]) => Rep[B]
        )(
            other: FunctionBodies3[T1, T2, T3, B]
        )(
            asUnique: Boolean = false
        ): FunctionBodies3[T1, T2, T3, B] = {
            val (x1New, b1New) = unify (f)(this.x1, other.x1)(this.b1, other.b1)(asUnique)
            val (x2New, b2New) = unify (f)(this.x2, other.x2)(this.b2, other.b2)(asUnique)
            val (x3New, b3New) = unify (f)(this.x3, other.x3)(this.b3, other.b3)(asUnique)
            FunctionBodies3 (x1New, b1New, x2New, b2New, x3New, b3New)
        }
    }

    case class FunctionBodies4[T1: Manifest, T2: Manifest, T3: Manifest, T4: Manifest, B: Manifest] (
        x1: Rep[T1], b1: Option[Rep[B]],
        x2: Rep[T2], b2: Option[Rep[B]],
        x3: Rep[T3], b3: Option[Rep[B]],
        x4: Rep[T4], b4: Option[Rep[B]]
    )
    {
        def manifestB = implicitly[Manifest[B]]

        def combineWith (
            f: (Rep[B], Rep[B]) => Rep[B]
        )(
            other: FunctionBodies4[T1, T2, T3, T4, B]
        )(
            asUnique: Boolean = false
        ): FunctionBodies4[T1, T2, T3, T4, B] = {
            val (x1New, b1New) = unify (f)(this.x1, other.x1)(this.b1, other.b1)(asUnique)
            val (x2New, b2New) = unify (f)(this.x2, other.x2)(this.b2, other.b2)(asUnique)
            val (x3New, b3New) = unify (f)(this.x3, other.x3)(this.b3, other.b3)(asUnique)
            val (x4New, b4New) = unify (f)(this.x4, other.x4)(this.b4, other.b4)(asUnique)
            FunctionBodies4 (x1New, b1New, x2New, b2New, x3New, b3New, x4New, b4New)
        }
    }

}
