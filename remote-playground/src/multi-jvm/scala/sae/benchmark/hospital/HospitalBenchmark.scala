package sae.benchmark.hospital

import idb.benchmark.Measurement
import idb.query.QueryEnvironment
import idb.schema.hospital
import idb.schema.hospital._
import idb.{BagTable, Relation, Table}
import sae.benchmark.{Benchmark, BenchmarkConfig, CSVPrinter}

/**
  * Barriers that are used in the hospital benchmark:
  *
  * deployed - The tables have been deployed on their servers and the printer has been initialized.
  * compiled - The query has been compiled and deployed to the servers.
  *
  * sent-warmup - The warmup events have been sent (from the tables)
  *
  * resetted - The warmup events have been received and the data structures have been resetted.
  *
  * ready-measure - The classes needed for measurements have been initialized.
  * sent-measure - The measure events have been sent (from the tables).
  *
  * finished - The measurement has been finished.
  *
  *
  * deploy
  * compile
  * warmup-predata
  * warmup-data
  * warmup-finish
  * reset
  * measure-predata
  * measure-init
  * measure-data
  * measure-finish
  * finish
  */
trait HospitalBenchmark extends Benchmark {

	override val benchmarkGroup = "hospital"

	object BaseHospital extends HospitalSchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends HospitalTestData

	type PersonType
	type PatientType
	type KnowledgeType
	type ResultType
}

trait DoublePersonHospitalBenchmark extends HospitalBenchmark {
	override val benchmarkType = "double-person"

	override type PersonType = (Long, Person)
	override type PatientType = Patient
	override type KnowledgeType = KnowledgeData


	object PersonDBNode extends DBNode {

		override val nodeName = "person-node"
		override val dbNames = Seq("person-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			db += ((System.currentTimeMillis(), Person(index, "John Doe", 1973)))
			db += ((System.currentTimeMillis(), hospital.Person(index, "Jane Doe", 1960)))
		}
	}

	object PatientDBNode extends DBNode {
		import Data._

		override val nodeName = "patient-node"
		override val dbNames = Seq("patient-db")

		override val iterations: Int = measureIterations

		override val isPredata : Boolean = true

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			db += Patient(index, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
		}
	}

	object KnowledgeDBNode extends DBNode {
		import Data._

		override val nodeName = "knowledge-node"
		override val dbNames = Seq("knowledge-db")

		override val iterations: Int = 1

		override val isPredata : Boolean = true

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			db += lungCancer1
		}
	}
}

trait DefaultHospitalBenchmark extends HospitalBenchmark {


	override type PersonType = (Long, Person)
	override type PatientType = Patient
	override type KnowledgeType = KnowledgeData

	val numberOfJohnDoes = 2500

	override val benchmarkType = "default-" + numberOfJohnDoes

	object PersonDBNode extends DBNode {
		override val nodeName = "person-node"
		override val dbNames = Seq("person-db")

		override val iterations: Int = measureIterations

		override val isPredata : Boolean = false

		private val interval = iterations / numberOfJohnDoes

		private var count = 0
		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			if (count == interval)
				count = 0

			if (count == 0)
				db += ((System.currentTimeMillis(), hospital.Person(index, "John Doe", 1973)))
			else
				db += ((System.currentTimeMillis(), hospital.Person(index, "Jane Doe", 1960)))

			count += 1
		}
	}

	object PatientDBNode extends DBNode {
		import Data._

		override val nodeName = "patient-node"
		override val dbNames = Seq("patient-db")

		override val iterations: Int = measureIterations

		override val isPredata : Boolean = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			db += hospital.Patient(index, 4, 2011, Seq(Symptoms.cough, Symptoms.chestPain))
		}
	}

	object KnowledgeDBNode extends DBNode {
		import Data._

		override val nodeName = "knowledge-node"
		override val dbNames = Seq("knowledge-db")

		override val iterations: Int = 1

		override val isPredata : Boolean = true

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val db = dbs.head
			db += lungCancer1
		}
	}
}
