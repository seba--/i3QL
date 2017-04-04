package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.{Relation, algebra}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.syntax.iql.IR
import sae.benchmark.BenchmarkMultiNodeSpec

class CompanyBenchmark4MultiJvmNode1 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode2 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode3 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode4 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode5 extends CompanyBenchmark4

object CompanyBenchmark4 {} // this object is necessary for multi-node testing

class CompanyBenchmark4 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultCompanyBenchmark
	//Specifies the number of measurements/warmups
	with AWS4000ClientPriorityConfig {

	override val benchmarkQuery = "query4"
	//override val benchmarkNumber: Int = 2

	import CompanyMultiNodeConfig._
	def initialParticipants = roles.size

	//Setup query environment
	val publicHost = RemoteHost("public-host", node(rolePublic))
	val productionHost = RemoteHost("production-host", node(roleProduction))
	val purchasingHost = RemoteHost("purchasing-host", node(rolePurchasing))
	val employeesHost = RemoteHost("employees-host", node(roleEmployees))
	val clientHost = RemoteHost("clients-host", node(roleClient))

	implicit val env = QueryEnvironment.create(
		system,
		Map(
			publicHost -> (priorityPublic, permissionsPublic),
			productionHost -> (priorityProduction, permissionsProduction),
			purchasingHost -> (priorityPurchasing, permissionsPurchasing),
			employeesHost -> (priorityEmployees, permissionsEmployees),
			clientHost -> (priorityClient, permissionsClient)
		)
	)

	def internalBarrier(name : String): Unit = {
		enterBarrier(name)
	}

	type ResultType = (Long, Long, Int, Int)

	object ClientNode extends ReceiveNode[ResultType] {
		override def relation(): Relation[ResultType] = {
			//Write an i3ql query...
			import BaseCompany._
			import idb.syntax.iql.IR._
			import idb.syntax.iql._
			import idb.schema.company._

			val products : Rep[Query[Product]] = RECLASS (REMOTE GET (publicHost, "product-db"), labelPublic)
			val factories : Rep[Query[Factory]] = RECLASS (REMOTE GET (publicHost, "factory-db"), labelPublic)

			val components : Rep[Query[Component]] = RECLASS (REMOTE GET (productionHost, "component-db"), labelProduction)
			val pcs : Rep[Query[PC]] = RECLASS (REMOTE GET (productionHost, "pc-db"), labelProduction)
			val fps : Rep[Query[FP]] = RECLASS (REMOTE GET (productionHost, "fp-db"), labelProduction)

			val suppliers : Rep[Query[Supplier]] = RECLASS (REMOTE GET (purchasingHost, "supplier-db"), labelPurchasing)
			val scs : Rep[Query[SC]] = RECLASS (REMOTE GET (purchasingHost, "sc-db"), labelPurchasing)

			val employees : Rep[Query[Employee]] = RECLASS (REMOTE GET (employeesHost, "employee-db"), labelEmployees)
			val fes : Rep[Query[FE]] = RECLASS (REMOTE GET (employeesHost, "fe-db"), labelEmployees)

//			val q =
//				SELECT ((c : Rep[Component], s : Rep[Supplier], sc : Rep[SC]) =>
//					(c.timestamp, s.timestamp, c.id, s.id)
//				) FROM (
//					components, suppliers, scs
//				) WHERE ((c, s, sc) =>
//					sc.price < 60.00 AND sc.inventory >= 4 AND c.id == sc.componentId AND
//						c.material == "Wood" AND s.id == sc.supplierId AND
//						(s.city == "Darmstadt" OR s.city == "Hanau") AND
//						s.name.startsWith("Bauhaus")
//				)

			val qSC =
				SELECT (*) FROM scs WHERE (sc =>
					sc.price < 60.00 AND sc.inventory >= 4
				)

			val qSupplier =
				SELECT (*) FROM suppliers WHERE (s =>
					(s.city == "Darmstadt" OR s.city == "Hanau") AND s.name.startsWith("Bauhaus")
				)

			val qa =
				SELECT ((s : Rep[Supplier], sc : Rep[SC]) =>
					(s.timestamp, s.id, sc.componentId)
				) FROM (qSupplier, qSC) WHERE ((s, sc) =>
					s.id == sc.supplierId
				)

			val q =
				SELECT ((c : Rep[Component], e : Rep[(Long, Int, Int)]) =>
					(c.timestamp, e._1, c.id, e._3)
				) FROM (components, qa) WHERE ((c : Rep[Component], e : Rep[(Long, Int, Int)]) =>
					c.id == e._3 AND c.material == "Wood"
				)


			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q

			//Define the root. The operators get distributed here.
			val r : idb.Relation[ResultType] =
			ROOT(clientHost, query)
			r
		}

		override def eventStartTime(e: ResultType): Long = {
			scala.math.max(e._1, e._2)
		}
	}

	"Hospital Benchmark" must {
		"run benchmark" in {
			runOn(rolePublic) { PublicDBNode.exec() }
			runOn(roleProduction) { ProductionDBNode.exec()	}
			runOn(rolePurchasing) { PurchasingDBNode.exec() }
			runOn(roleEmployees) { EmployeesDBNode.exec() }
			runOn(roleClient) { ClientNode.exec() }
		}
	}
}
