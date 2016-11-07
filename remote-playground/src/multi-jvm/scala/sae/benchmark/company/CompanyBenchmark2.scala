package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.colors._
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.{BenchmarkMultiNodeSpec, TestConfig1}

class CompanyBenchmark2MultiJvmNode1 extends CompanyBenchmark2
class CompanyBenchmark2MultiJvmNode2 extends CompanyBenchmark2
class CompanyBenchmark2MultiJvmNode3 extends CompanyBenchmark2
class CompanyBenchmark2MultiJvmNode4 extends CompanyBenchmark2
class CompanyBenchmark2MultiJvmNode5 extends CompanyBenchmark2

object CompanyBenchmark2 {} // this object is necessary for multi-node testing

class CompanyBenchmark2 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with TestCompanyBenchmark
	//Specifies the number of measurements/warmups
	with TestConfig1 {

	override val benchmarkQuery = "query2"
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

			//TODO: Here a cross product (instead of equi join) is generated!
//			val q1 : Rep[Query[ResultType]] =
//				SELECT ( (s : Rep[Supplier], p : Rep[Product], pc : Rep[PC], sc : Rep[SC]) =>
//					(pc.timestamp, sc.timestamp, pc.productId, sc.supplierId)
//				) FROM (
//					suppliers, products, pcs, scs
//				) WHERE ( (s : Rep[Supplier], p : Rep[Product], pc : Rep[PC], sc : Rep[SC]) =>
//					sc.componentId == pc.componentId AND pc.productId == p.id AND s.id == sc.supplierId
//				)

			val qa =
				SELECT ( (s : Rep[Supplier], sc : Rep[SC]) =>
					(s.timestamp, s.id, sc.componentId)
				) FROM (
					suppliers, scs
				) WHERE ( (s : Rep[Supplier], sc : Rep[SC]) =>
					s.id == sc.supplierId
				)

			val qb  =
				SELECT ( (p : Rep[Product], pc : Rep[PC]) =>
					(p.timestamp, p.id, pc.componentId)
				) FROM (
					products, pcs
				) WHERE ( (p : Rep[Product], pc : Rep[PC]) =>
					pc.productId == p.id
				)

			val q1 =
				SELECT ((a : Rep[(Long, Int, Int)], b : Rep[(Long, Int, Int)]) =>
					(a._1, b._1, a._2, b._2)
				) FROM (
					qa, qb
				) WHERE ((a : Rep[(Long, Int, Int)], b : Rep[(Long, Int, Int)]) =>
					a._3 == b._3
				)



			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q1

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
