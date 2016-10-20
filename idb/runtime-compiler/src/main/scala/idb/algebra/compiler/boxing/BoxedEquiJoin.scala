package idb.algebra.compiler.boxing

import idb.Relation
import idb.lms.extensions.ScalaCodegenExt
import idb.operators.impl.EquiJoinView

/**
  * Created by mirko on 18.10.16.
  */
case class BoxedEquiJoin[DomainA, DomainB](
	relA : Relation[DomainA],
	relB : Relation[DomainB],
	eqA : List[DomainA => Any],
	eqB : List[DomainB => Any],
	isSet : Boolean
) extends Relation[(DomainA, DomainB)] {

	@transient var equiJoin : EquiJoinView[DomainA, DomainB, (DomainA, DomainB), Seq[Any]] = null

	def compile(compiler : ScalaCodegenExt): Unit = {

		val compiledEqsA = eqA.map(f => BoxedFunction.compile(f, compiler))
		val compiledEqsB = eqB.map(f => BoxedFunction.compile(f, compiler))

		equiJoin = EquiJoinView(
			relA,
			relB,
			compiledEqsA,
			compiledEqsB,
			isSet
		)
	}

	override def foreach[T](f: ((DomainA, DomainB)) => T): Unit = equiJoin.foreach(f)

	/**
	  * Each view must be able to
	  * materialize it's content from the underlying
	  * views.
	  * The laziness allows a query to be set up
	  * on relations (tables) that are already filled.
	  * The lazy initialization must be performed prior to processing the
	  * first add/delete/update events or foreach calls.
	  */

	override def children: Seq[Relation[_]] = equiJoin.children()

	override protected def resetInternal(): Unit = equiJoin.resetInternal()

	override def prettyprint(implicit prefix: String): String = equiJoin.prettyprint
}
