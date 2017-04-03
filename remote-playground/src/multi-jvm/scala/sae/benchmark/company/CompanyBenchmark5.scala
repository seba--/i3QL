package sae.benchmark.company

import akka.remote.testkit.MultiNodeSpec
import idb.{Relation, algebra}
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.syntax.iql.IR
import idb.syntax.iql.impl._
import sae.benchmark.BenchmarkMultiNodeSpec

class CompanyBenchmark5MultiJvmNode1 extends CompanyBenchmark5
class CompanyBenchmark5MultiJvmNode2 extends CompanyBenchmark5
class CompanyBenchmark5MultiJvmNode3 extends CompanyBenchmark5
class CompanyBenchmark5MultiJvmNode4 extends CompanyBenchmark5
class CompanyBenchmark5MultiJvmNode5 extends CompanyBenchmark5

object CompanyBenchmark5 {} // this object is necessary for multi-node testing

class CompanyBenchmark5 extends MultiNodeSpec(CompanyMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultCompanyBenchmark
	//Specifies the number of measurements/warmups
	with Measure4000DefaultPriorityConfig {

	override val benchmarkQuery = "query5"
	override val benchmarkNumber: Int = 1

	override val waitForData = 30000 //ms


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

	type ResultType = (Int, Int, idb.schema.company.FE)

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

//			val workersInFactory : Rep[Query[(Int, Int)]] =
//				SELECT (
//					(fid : Rep[Int]) => fid, COUNT(*)
//				) FROM (fes) GROUP BY ((fe : Rep[FE]) => fe.factoryId)


			val a = COUNT.apply((fe : Rep[FE]) => fe)
			val aggregateFunction : AggregateFunctionSelfMaintained[FE, Int] = a.asInstanceOf[AggregateFunctionSelfMaintained[FE, Int]]

			val clause =
				GroupByClause1 (
					(fe : Rep[FE]) => fe.factoryId,
					FromClause1 (
						fes,
						SelectAggregateClause[Int, FE, (Int, Int, FE)] (
							AggregateTupledFunctionSelfMaintained[Int, FE, Int, Int, (Int, Int, FE)] (
								aggregateFunction.start,
								(x : Rep[(FE, Int)]) => aggregateFunction.added ((x._1, x._2)),
								(x : Rep[(FE, Int)]) => aggregateFunction.removed ((x._1, x._2)),
								(x : Rep[(FE, FE, Int)]) => aggregateFunction.updated ((x._1, x._2, x._3)),
								(fid : Rep[Int]) => fid,
								fun ( (x : Rep[(Int, Int, FE)]) => (x._1, x._2, x._3) )
							),
							false
						)
					)
				)

			val workersInFactory : Rep[Query[(Int, Int, FE)]] = plan(clause)


			val q = SELECT ((fw : Rep[(Int, Int, FE)], fp : Rep[FP], p : Rep[Product]) =>
				fw
			) FROM (
				workersInFactory, fps, products
			) WHERE ((fw : Rep[(Int, Int, FE)], fp : Rep[FP], p : Rep[Product]) =>
				p.name.startsWith("Billy") AND fp.productId == p.id AND fp.factoryId == fw._1
			)


			//Compile to LMS representation (only needed for printing)
			val query : Rep[Query[ResultType]] = q

			//Define the root. The operators get distributed here.
			val r : idb.Relation[ResultType] =
				ROOT(clientHost, query)
			r
		}

		override def eventStartTime(e: ResultType): Long = {
			e._3.timestamp
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
