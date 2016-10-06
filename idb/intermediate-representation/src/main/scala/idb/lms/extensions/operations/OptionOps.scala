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

import scala.reflect.SourceContext
import scala.virtualization.lms.common._
import scala.language.implicitConversions


/**
 *
 * @author Ralf Mitschke
 *
 */

trait OptionOps
    extends Base
    with Variables
{

    object Some
    {
        def apply[A: Manifest] (x: Rep[A])(implicit pos: SourceContext) = some_new (x)
    }

    implicit def varToOptOps[A: Manifest] (x: Var[Option[A]]) = new OptOpsCls (readVar (x))

    implicit def repOptToOptOps[T: Manifest] (a: Rep[Option[T]]) = new OptOpsCls (a)

    implicit def optToOptOps[T: Manifest] (a: Option[T]) = new OptOpsCls (unit (a))

    class OptOpsCls[T: Manifest] (a: Rep[Option[T]])
    {
        def isEmpty (implicit pos: SourceContext) = opt_isEmpty (a)

        def isDefined (implicit pos: SourceContext) = opt_isDefined (a)

        def get (implicit pos: SourceContext) = opt_get (a)

        def getOrElse[B >: T : Manifest] (default: => Rep[B]): Rep[B] = opt_getOrElse (a, default)
    }

    //def none_new (implicit pos: SourceContext): Rep[None.type]

    def some_new[A: Manifest] (x: Rep[A])(implicit pos: SourceContext): Rep[Some[A]]

    def opt_isEmpty[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[Boolean]

    def opt_isDefined[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[Boolean]

    def opt_get[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[T]

    def opt_getOrElse[T: Manifest, B >: T : Manifest] (x: Rep[Option[T]], default: Rep[B])
            (implicit pos: SourceContext): Rep[B]
}

trait OptionOpsExp extends OptionOps with EffectExp
{

    case class SomeNew[A: Manifest] (x: Rep[A]) extends Def[Some[A]]

    case class OptionIsEmpty[T: Manifest] (a: Exp[Option[T]]) extends Def[Boolean]

    case class OptionIsDefined[T: Manifest] (a: Exp[Option[T]]) extends Def[Boolean]

    case class OptionGet[T: Manifest] (x: Rep[Option[T]]) extends Def[T]

    case class OptionGetOrElse[T: Manifest, B >: T : Manifest] (x: Rep[Option[T]], default: Rep[B]) extends Def[B]


    override def some_new[A: Manifest] (x: Rep[A])(implicit pos: SourceContext): Rep[Some[A]] =
        SomeNew (x)

    override def opt_isEmpty[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[Boolean] =
        OptionIsEmpty (x)

    override def opt_isDefined[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[Boolean] =
        OptionIsDefined (x)

    override def opt_get[T: Manifest] (x: Rep[Option[T]])(implicit pos: SourceContext): Rep[T] =
        OptionGet (x)

    override def opt_getOrElse[T: Manifest, B >: T : Manifest] (x: Rep[Option[T]], default: Rep[B])
            (implicit pos: SourceContext): Rep[B] =
        OptionGetOrElse (x, default)

    override def mirror[A: Manifest] (e: Def[A], f: Transformer)(implicit pos: SourceContext): Exp[A] = (e match {
        case SomeNew (x) => some_new (f (x))
        case OptionIsEmpty (x) => opt_isEmpty (f (x))
        case OptionIsDefined (x) => opt_isDefined (f (x))
        case OptionGet (x) => opt_get (f (x))
        case OptionGetOrElse (x, d) => opt_getOrElse (f (x), f (d))
        case _ => super.mirror (e, f)
    }).asInstanceOf[Exp[A]]


}

trait BaseGenOptionOps extends ScalaGenBase
{
    val IR: OptionOpsExp


}

trait ScalaGenOptionOps extends BaseGenOptionOps with ScalaGenEffect
{
    val IR: OptionOpsExp

    import IR._

    override def emitNode (sym: Sym[Any], rhs: Def[Any]) = rhs match {
        case SomeNew (x) => emitValDef (sym, "Some(" + quote (x) + ")")
        case OptionIsEmpty (x) => emitValDef (sym, "" + quote (x) + ".isEmpty")
        case OptionIsDefined (x) => emitValDef (sym, "" + quote (x) + ".isDefined")
        case OptionGet (x) => emitValDef (sym, "" + quote (x) + ".get")
        case OptionGetOrElse (x, d) => emitValDef (sym, "" + quote (x) + ".getOrElse(" + quote (d) + ")")
        case _ => super.emitNode (sym, rhs)
    }
}
