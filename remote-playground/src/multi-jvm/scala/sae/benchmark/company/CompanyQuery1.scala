package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.colors._
import idb.query.{QueryEnvironment, RemoteHost}
import sae.benchmark.{BenchmarkMultiNodeSpec, TestConfig1}

class CompanyQuery1MultiJvmNode1 extends CompanyQuery1
class CompanyQuery1MultiJvmNode2 extends CompanyQuery1
class CompanyQuery1MultiJvmNode3 extends CompanyQuery1
class CompanyQuery1MultiJvmNode4 extends CompanyQuery1
class CompanyQuery1MultiJvmNode5 extends CompanyQuery1

object CompanyQuery1 {} // this object is necessary for multi-node testing

//Selection is pushed down == events get filtered before getting sent
class CompanyQuery1 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with TestCompanyBenchmark
	//Specifies the number of measurements/warmups
	with TestConfig1 {

	override val benchmarkName = "query1"
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
			publicHost -> Set("lab:public"),
			productionHost -> Set("lab:public", "lab:production"),
			purchasingHost -> Set("lab:public", "lab:purchasing"),
			employeesHost -> Set("lab:public", "lab:employees"),
			clientHost -> Set("lab:client") //For now: Client has its own permission to simulate pushing queries down
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


			val products : Rep[Query[Product]] =
				REMOTE GET (publicHost, "product-db", Color("lab:public"))
			val pcs : Rep[Query[PC]] =
				REMOTE GET (productionHost, "pc-db", Color("lab:production"))
			val components : Rep[Query[Component]] =
				REMOTE GET (productionHost, "component-db", Color("lab:production"))

			val q1 =
				SELECT ( (p : Rep[Product], pc : Rep[PC], c : Rep[Component]) =>
					(p.timestamp, c.id, c.name)
				) FROM (
					products, pcs, components
				) WHERE ((p : Rep[Product], pc : Rep[PC], c : Rep[Component]) =>
					p.name == "Billy3" AND pc.productId == p.id AND
						pc.componentId == c.id
				)

			//Print the LMS tree representation
			val printer = new RelationalAlgebraPrintPlan {
				override val IR = idb.syntax.iql.IR
			}
			Predef.println("Relation.tree#" + printer.quoteRelation(q1))

			//... and add ROOT. Workaround: Reclass the data to make it pushable to the client node.
			val r : idb.syntax.iql.IR.Relation[ResultType] =
				ROOT(clientHost, RECLASS(q1, Color("lab:client")))
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
