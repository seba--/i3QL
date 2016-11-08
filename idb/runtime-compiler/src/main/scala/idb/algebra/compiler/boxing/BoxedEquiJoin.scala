package idb.algebra.compiler.boxing

import java.io.PrintStream

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

	@transient var equiJoin : EquiJoinView[DomainA, DomainB, (DomainA, DomainB), Any] = null

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

		observers.foreach(o => equiJoin.addObserver(o))
		observers.clear()
	}

	override def foreach[T](f: ((DomainA, DomainB)) => T): Unit = equiJoin.foreach(f)

	override def children: Seq[Relation[_]] = equiJoin.children

	override protected def resetInternal(): Unit = equiJoin.resetInternal()

	override protected[idb] def printInternal(out : PrintStream)(implicit prefix: String = " "): Unit =
		equiJoin.printInternal(out)
}
