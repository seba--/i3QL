package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.colors._
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.{BenchmarkMultiNodeSpec, TestConfig1}

class CompanyBenchmark7MultiJvmNode1 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode2 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode3 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode4 extends CompanyBenchmark7
class CompanyBenchmark7MultiJvmNode5 extends CompanyBenchmark7

object CompanyBenchmark7 {} // this object is necessary for multi-node testing

class CompanyBenchmark7 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with TestCompanyBenchmark
	//Specifies the number of measurements/warmups
	with TestConfig1 {

	override val benchmarkQuery = "query7"
	override val benchmarkNumber: Int = 1

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
			publicHost -> (1, Set("lab:public")),
			productionHost -> (1, Set("lab:public", "lab:production")),
			purchasingHost -> (1, Set("lab:public", "lab:purchasing")),
			employeesHost -> (1, Set("lab:public", "lab:employees")),
			clientHost -> (0, Set("lab:public", "lab:production", "lab:purchasing", "lab:employees"))
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
			import idb.syntax.iql.IR._
			import idb.syntax.iql._
			import idb.schema.company._


			val products : Rep[Query[Product]] = REMOTE GET (publicHost, "product-db", Color("lab:public"))
			val factories : Rep[Query[Factory]] = REMOTE GET (publicHost, "factory-db", Color("lab:public"))

			val components : Rep[Query[Component]] = REMOTE GET (productionHost, "component-db", Color("lab:production"))
			val pcs : Rep[Query[PC]] = REMOTE GET (productionHost, "pc-db", Color("lab:production"))
			val fps : Rep[Query[FP]] = REMOTE GET (productionHost, "fp-db", Color("lab:production"))

			val suppliers : Rep[Query[Supplier]] = REMOTE GET (purchasingHost, "supplier-db", Color("lab:purchasing"))
			val scs : Rep[Query[SC]] = REMOTE GET (purchasingHost, "sc-db", Color("lab:purchasing"))

			val employees : Rep[Query[Employee]] = REMOTE GET (employeesHost, "employee-db", Color("lab:employees"))
			val fes : Rep[Query[FE]] = REMOTE GET (employeesHost, "fe-db", Color("lab:employees"))


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
				override val IR = idb.syntax.iql.IR
			}
			Predef.println(printer.quoteRelation(query))

			//Define the root. The operators get distributed here.
			val r : idb.syntax.iql.IR.Relation[ResultType] =
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
