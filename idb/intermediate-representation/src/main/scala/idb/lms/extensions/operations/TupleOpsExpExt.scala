package idb.lms.extensions.operations

import scala.reflect.SourceContext
import scala.virtualization.lms.common.TupleOpsExp
import scala.language.implicitConversions

/**
 * @author Mirko Köhler
 */
trait TupleOpsExpExt extends TupleOpsExp with StructExpExt {

	/*implicit override def make_tuple2[A:Manifest,B:Manifest](t: (Exp[A],Exp[B]))(implicit pos: SourceContext) : Exp[(A,B)] =
		super.make_tuple2(t)(t._1.tp, t._2.tp, pos)

	implicit override def make_tuple3[A:Manifest,B:Manifest,C:Manifest](t: (Exp[A],Exp[B],Exp[C]))(implicit pos: SourceContext) : Exp[(A,B,C)] =
		super.make_tuple3(t)(t._1.tp, t._2.tp, t._3.tp, pos)

	implicit override def make_tuple4[A:Manifest,B:Manifest,C:Manifest,D:Manifest](t: (Exp[A],Exp[B],Exp[C],Exp[D]))(implicit pos: SourceContext) : Exp[(A,B,C,D)] =
		super.make_tuple4(t)(t._1.tp, t._2.tp, t._3.tp, t._4.tp, pos)

	implicit override def make_tuple5[A:Manifest,B:Manifest,C:Manifest,D:Manifest,E:Manifest](t: (Exp[A],Exp[B],Exp[C],Exp[D],Exp[E]))(implicit pos: SourceContext) : Exp[(A,B,C,D,E)] =
		super.make_tuple5(t)(t._1.tp, t._2.tp, t._3.tp, t._4.tp, t._5.tp, pos)      */
}
