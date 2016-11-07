package sae.benchmark.company

import idb.Table
import idb.schema.company._
import sae.benchmark.Benchmark

/**
  * Created by mirko on 07.11.16.
  */
trait CompanyBenchmark extends Benchmark {

	override val benchmarkGroup = "company"

	object BaseCompany extends CompanySchema {
		override val IR = idb.syntax.iql.IR
	}

	object Data extends CompanyTestData
}

trait TestCompanyBenchmark extends CompanyBenchmark {
	override val benchmarkType = "test"

	object PublicDBNode extends DBNode {

		override val nodeName = "public-node"
		override val dbNames = Seq("product-db", "factory-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val productDB = dbs(0)
			productDB += Product(index, "Billy" + index)

			if (index == 0) {
				val factoryDB = dbs(1)
				factoryDB += Factory(0, Data.Cities.darmstadt)
			}
		}
	}

	object ProductionDBNode extends DBNode {

		override val nodeName = "production-node"
		override val dbNames = Seq("component-db", "pc-db", "fp-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			pcDB += PC(index, 0, 1)
			fpDB += FP(0, index)

			if (index == 0) {
				val componentsDB = dbs(0)
				componentsDB += Component(0, "wood")
			}
		}
	}

	object PurchasingDBNode extends DBNode {

		override val nodeName = "purchasing-node"
		override val dbNames = Seq("supplier-db", "sc-db")

		override val iterations = 1

		override val isPredata = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val supplierDB = dbs(0)
			val scDB = dbs(1)

			supplierDB += Supplier(0, "Woodland Inc.", Data.Cities.frankfurt)
			scDB += SC(0, 0, 1000, 42.23)
		}
	}

	object EmployeesDBNode extends DBNode {

		override val nodeName = "employee-node"
		override val dbNames = Seq("employee-db", "fe-db")

		override val iterations = 1

		override val isPredata = false

		override def iteration(dbs : Seq[Table[Any]], index : Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			employeesDB += Employee(0, "Alice")
			feDB += FE(0, 0, Data.Jobs.worker)
		}
	}
}
