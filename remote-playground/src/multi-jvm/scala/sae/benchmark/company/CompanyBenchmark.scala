package sae.benchmark.company

import idb.Table
import idb.algebra.IR
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

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val productDB = dbs(0)
			productDB += Product(index, "Billy" + index)

			if (index == 0) {
				val factoryDB = dbs(1)
				factoryDB += Factory(0, Data.Cities.DARMSTADT)
			}
		}
	}

	object ProductionDBNode extends DBNode {

		override val nodeName = "production-node"
		override val dbNames = Seq("component-db", "pc-db", "fp-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			pcDB += PC(index, 0, 1)
			fpDB += FP(0, index)

			if (index == 0) {
				val componentsDB = dbs(0)
				componentsDB += Component(0, Data.ComponentNames.OAK, Data.Materials.WOOD)
			}
		}
	}

	object PurchasingDBNode extends DBNode {

		override val nodeName = "purchasing-node"
		override val dbNames = Seq("supplier-db", "sc-db")

		override val iterations = 1

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val supplierDB = dbs(0)
			val scDB = dbs(1)

			supplierDB += Supplier(0, "Woodland Inc.", Data.Cities.FRANKFURT)
			scDB += SC(0, 0, 1000, 42.23)
		}
	}

	object EmployeesDBNode extends DBNode {

		override val nodeName = "employee-node"
		override val dbNames = Seq("employee-db", "fe-db")

		override val iterations = 1

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			employeesDB += Employee(0, "Alice")
			feDB += FE(0, 0, Data.Jobs.WORKER)
		}
	}
}

trait DefaultCompanyBenchmark extends CompanyBenchmark {
	override val benchmarkType = "default"

	object PublicDBNode extends DBNode {

		override val nodeName = "public-node"
		override val dbNames = Seq("product-db", "factory-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val productDB = dbs(0)
			val factoryDB = dbs(1)

			productDB += Product(index, Data.ProductNames.BILLY + index)
			productDB += Product(iterations + index, Data.ProductNames.KNUT + index)

			if (index % 10 < 5)
				factoryDB += Factory(index, Data.Cities.FRANKFURT)
			else
				factoryDB += Factory(index, Data.Cities.DARMSTADT)

		}
	}

	object ProductionDBNode extends DBNode {

		override val nodeName = "production-node"
		override val dbNames = Seq("component-db", "pc-db", "fp-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val componentsDB = dbs(0)
			val pcDB = dbs(1)
			val fpDB = dbs(2)

			componentsDB += Component(index, Data.ComponentNames.OAK + index, Data.Materials.WOOD)
			componentsDB += Component(iterations + index, Data.ComponentNames.IRON + index, Data.Materials.WOOD)

			val i = index % 10
			pcDB += PC(index, index, i + 1)
			pcDB += PC(index, iterations + index, i + 1)
			pcDB += PC(iterations + index, iterations + index, i + 1)

			fpDB += FP(index, index)
			fpDB += FP(index, iterations + index)
		}
	}

	object PurchasingDBNode extends DBNode {

		override val nodeName = "purchasing-node"
		override val dbNames = Seq("supplier-db", "sc-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val supplierDB = dbs(0)
			val scDB = dbs(1)

			val i = index % 10
			i match {
				case 0 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.FRANKFURT)
				case 1 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.FRANKFURT)
				case 2 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.FRANKFURT)
				case 3 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.FRANKFURT)
				case 4 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.DARMSTADT)
				case 5 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.DARMSTADT)
				case 6 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.HANAU)
				case 7 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.HANAU)
				case 8 => supplierDB += Supplier(index, Data.SupplierNames.BAUHAUS + index, Data.Cities.OFFENBACH)
				case 9 => supplierDB += Supplier(index, Data.SupplierNames.HORNBACH + index, Data.Cities.OFFENBACH)
			}

			scDB += SC(index, index, i + 1, (i + 1) * 10)
			scDB += SC(index, iterations + index, i + 1, (i + 1) * 10)
		}
	}

	object EmployeesDBNode extends DBNode {

		override val nodeName = "employee-node"
		override val dbNames = Seq("employee-db", "fe-db")

		override val iterations = measureIterations

		override val isPredata = false

		override def iteration(dbs: Seq[Table[Any]], index: Int): Unit = {
			val employeesDB = dbs(0)
			val feDB = dbs(1)

			val n = index * 10
			(0 until 10) foreach (i => {
				employeesDB += Employee(n + i, Data.EmployeeNames.ALICE + (n + i))
				if (i == 0)
					feDB += FE(index, n + i, Data.Jobs.ACCOUNTANT)
				else
					feDB += FE(index, n + i, Data.Jobs.WORKER)
			})
		}
	}

}
