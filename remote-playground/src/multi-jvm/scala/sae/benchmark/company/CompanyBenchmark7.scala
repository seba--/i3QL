package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.{Relation, algebra}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.BenchmarkMultiNodeSpec

class CompanyBenchmark7MultiJvmNode1 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode2 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode3 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode4 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode5 extends CompanyBenchmark7

object CompanyBenchmark7 {} // this object is necessary for multi-node testing

class CompanyBenchmark7 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultCompanyBenchmark
	//Specifies the number of measurements/warmups
	with Measure4000ClientPriorityConfig {

	override val benchmarkQuery = "query7"
	override val benchmarkNumber: Int = 100

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

	type ResultType = idb.schema.company.Product

	object ClientNode extends ReceiveNode[ResultType] {
		override def relation(): Relation[ResultType] = {
			//Write an i3ql query...
			import BaseCompany._
			import idb.algebra.IR._
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


			val q =
				SELECT ((p : Rep[Product], fp : Rep[FP]) => p) FROM (products, fps) WHERE ((p : Rep[Product], fp : Rep[FP]) =>
					EXISTS (
						SELECT ((f : Rep[Factory]) => f) FROM factories WHERE ((f : Rep[Factory]) =>
							 f.id == fp.factoryId AND f.city == "Darmstadt"
						)
					) AND p.id == fp.productId
				)


			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q

			//Print the LMS tree representation
			val printer = new RelationalAlgebraPrintPlan {
				override val IR = algebra.IR
			}
			Predef.println(printer.quoteRelation(query))

			//Define the root. The operators get distributed here.
			val r : algebra.IR.Relation[ResultType] =
			ROOT(clientHost, query)
			r
		}

		override def eventStartTime(e: ResultType): Long = {
			e.timestamp
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
