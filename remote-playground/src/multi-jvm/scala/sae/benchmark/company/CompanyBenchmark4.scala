package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.colors._
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.{BenchmarkMultiNodeSpec, TestConfig1}

class CompanyBenchmark4MultiJvmNode1 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode2 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode3 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode4 extends CompanyBenchmark4
class CompanyBenchmark4MultiJvmNode5 extends CompanyBenchmark4

object CompanyBenchmark4 {} // this object is necessary for multi-node testing

//FIXME: Fix boxed function not working correctly
class CompanyBenchmark4 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with TestCompanyBenchmark
	//Specifies the number of measurements/warmups
	with TestConfig1 {

	override val benchmarkQuery = "query4"
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

	type ResultType = (Long, Long, Int, Int)

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
				SELECT ((c : Rep[Component], s : Rep[Supplier], sc : Rep[SC], pc : Rep[PC]) =>
					(c.timestamp, s.timestamp, c.id, s.id)
				) FROM (
					components, suppliers, scs, pcs
				) WHERE ((c, s, sc, pc) =>
					sc.price < 100.00 AND sc.inventory >= 10 AND c.id == sc.componentId AND
						c.name == "wood" AND s.id == sc.supplierId AND (
						s.city == "Darmstadt" OR s.city == "Frankfurt") AND
						s.name != "Woody's Wood" AND pc.componentId == c.id
				)


			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q

			//Print the LMS tree representation
			val printer = new RelationalAlgebraPrintPlan {
				override val IR = idb.syntax.iql.IR
			}
			Predef.println("Relation.tree#" + printer.quoteRelation(query))

			//Define the root. The operators get distributed here.
			val r : idb.syntax.iql.IR.Relation[ResultType] =
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
