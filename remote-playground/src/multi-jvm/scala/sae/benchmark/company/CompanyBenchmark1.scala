package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.BenchmarkMultiNodeSpec

class CompanyBenchmark1MultiJvmNode1 extends CompanyBenchmark1
class CompanyBenchmark1MultiJvmNode2 extends CompanyBenchmark1
class CompanyBenchmark1MultiJvmNode3 extends CompanyBenchmark1
class CompanyBenchmark1MultiJvmNode4 extends CompanyBenchmark1
class CompanyBenchmark1MultiJvmNode5 extends CompanyBenchmark1

object CompanyBenchmark1 {} // this object is necessary for multi-node testing

class CompanyBenchmark1 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultCompanyBenchmark
	//Specifies the number of measurements/warmups
	with AWS4000ClientPriorityNoWarmupConfig {

	override val benchmarkQuery = "query1"
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

	type ResultType = (Long, Int, String)

	object ClientNode extends ReceiveNode[ResultType] {
		override def relation(): Relation[ResultType] = {
			//Write an i3ql query...
			import idb.syntax.iql.IR._
			import idb.syntax.iql._
			import idb.schema.company._
			import BaseCompany._

			val products : Rep[Query[Product]] = RECLASS (REMOTE GET (publicHost, "product-db"), labelPublic)
			val factories : Rep[Query[Factory]] = RECLASS (REMOTE GET (publicHost, "factory-db"), labelPublic)

			val components : Rep[Query[Component]] = RECLASS (REMOTE GET (productionHost, "component-db"), labelProduction)
			val pcs : Rep[Query[PC]] = RECLASS (REMOTE GET (productionHost, "pc-db"), labelProduction)
			val fps : Rep[Query[FP]] = RECLASS (REMOTE GET (productionHost, "fp-db"), labelProduction)

			val suppliers : Rep[Query[Supplier]] = RECLASS (REMOTE GET (purchasingHost, "supplier-db"), labelPurchasing)
			val scs : Rep[Query[SC]] = RECLASS (REMOTE GET (purchasingHost, "sc-db"), labelPurchasing)

			val employees : Rep[Query[Employee]] = RECLASS (REMOTE GET (employeesHost, "employee-db"), labelEmployees)
			val fes : Rep[Query[FE]] = RECLASS (REMOTE GET (employeesHost, "fe-db"), labelEmployees)

			val q1 =
				SELECT ( (p : Rep[Product], pc : Rep[PC], c : Rep[Component]) =>
					(p.timestamp, c.id, c.name)
				) FROM (
					products, pcs, components
				) WHERE ((p : Rep[Product], pc : Rep[PC], c : Rep[Component]) =>
					p.name == "Billy3" AND pc.productId == p.id AND
						pc.componentId == c.id
				)

			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q1

			//Define the root. The operators get distributed here.
			val r : idb.Relation[ResultType] =
				ROOT(clientHost, query)
			r
		}

		override def eventStartTime(e: ResultType): Long = {
			e._1
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
