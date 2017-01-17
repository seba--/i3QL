package sae.benchmark.tpch

import java.util.Date

import akka.remote.testkit.MultiNodeSpec
import idb.Relation
import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.query.taint._
import idb.query.{QueryEnvironment, RemoteHost}
import idb.schema.tpch._
import sae.benchmark.BenchmarkMultiNodeSpec


class Q3MultiJvmNode1 extends Q3
class Q3MultiJvmNode2 extends Q3
class Q3MultiJvmNode3 extends Q3
class Q3MultiJvmNode4 extends Q3
class Q3MultiJvmNode5 extends Q3
class Q3MultiJvmNode6 extends Q3
class Q3MultiJvmNode7 extends Q3
class Q3MultiJvmNode8 extends Q3
class Q3MultiJvmNode9 extends Q3
class Q3MultiJvmNode10 extends Q3
class Q3MultiJvmNode11 extends Q3
class Q3MultiJvmNode12 extends Q3
class Q3MultiJvmNode13 extends Q3
class Q3MultiJvmNode14 extends Q3


object Q3 {} // this object is necessary for multi-node testing

class Q3 extends MultiNodeSpec(TPCHMultiNodeConfig)
	with BenchmarkMultiNodeSpec
	//Specifies the table setup
	with DefaultDataGenerator
	//Specifies the number of measurements/warmups
	with TestMeasureConfig {

	import TPCHMultiNodeConfig._

	override val benchmarkQuery = "q3"
	override val benchmarkNumber: Int = 0
	def initialParticipants = roles.size

	/*
		Setup query environment
	 */
	//data hosts
	val host_data_customer = RemoteHost("host_data_customer", node(node_data_customer))
	val host_data_lineitem = RemoteHost("host_data_lineitem", node(node_data_lineitem))
	val host_data_nation = RemoteHost("host_data_nation", node(node_data_nation))
	val host_data_orders = RemoteHost("host_data_orders", node(node_data_orders))
	val host_data_part = RemoteHost("host_data_part", node(node_data_part))
	val host_data_partsupp = RemoteHost("host_data_partsupp", node(node_data_partsupp))
	val host_data_region = RemoteHost("host_data_region", node(node_data_region))
	val host_data_supplier = RemoteHost("host_data_supplier", node(node_data_supplier))
	//processing hosts
	val host_process_finance = RemoteHost("host_process_finance", node(node_process_finance))
	val host_process_purchasing = RemoteHost("host_process_purchasing", node(node_process_purchasing))
	val host_process_shipping = RemoteHost("host_process_shipping", node(node_process_shipping))
	val host_process_geographical = RemoteHost("host_process_geographical", node(node_process_geographical))
	val host_process_private = RemoteHost("host_process_private", node(node_process_private))
	//client
	val host_client = RemoteHost("host_client", node(node_client))

	//taint defs
	val taintid_CustomerGeneral = "CustomerGeneral"
	val taintid_LineItemGeneral = "LineItemGeneral"
	val taintid_NationGeneral = "NationGeneral"
	val taintid_OrderGeneral = "OrderGeneral"
	val taintid_PartGeneral = "PartGeneral"
	val taintid_PartSuppGeneral = "PartSuppGeneral"
	val taintid_RegionGeneral = "RegionGeneral"
	val taintid_SupplierGeneral = "SupplierGeneral"

	val taintid_LineItemKey = "LineItemKey"
	val taintid_PartSuppKey = "PartSuppKey"

	val taintid_CustomerFinance = "CustomerFinance"
	val taintid_LineItemFinance = "LineItemFinance"
	val taintid_OrderFinance = "OrderFinance"

	val taintid_PartPurchasing = "PartPurchasing"
	val taintid_PartSuppPurchasing = "PartSuppPurchasing"
	val taintid_SupplierPurchasing = "SupplierPurchasing"

	val taintid_LineItemShipping = "LineItemShipping"
	val taintid_OrderShipping = "OrderShipping"

	val allTaints = Set(taintid_CustomerGeneral, taintid_LineItemGeneral, taintid_NationGeneral, taintid_OrderGeneral,
		taintid_PartGeneral, taintid_PartSuppGeneral, taintid_RegionGeneral, taintid_SupplierGeneral,
		taintid_LineItemKey, taintid_PartSuppKey, taintid_CustomerFinance, taintid_LineItemFinance, taintid_OrderFinance,
		taintid_PartPurchasing, taintid_PartSuppPurchasing, taintid_SupplierPurchasing, taintid_LineItemShipping,
		taintid_OrderShipping)

	//query environment
	implicit val env : QueryEnvironment = QueryEnvironment.create(
		system,
		Map(
			//data
			host_data_customer -> (100, Set(taintid_CustomerGeneral, taintid_CustomerFinance)),
			host_data_lineitem -> (100, Set(taintid_LineItemGeneral, taintid_LineItemKey, taintid_LineItemFinance, taintid_LineItemShipping)),
			host_data_nation -> (100, Set(taintid_NationGeneral)),
			host_data_orders -> (100, Set(taintid_OrderGeneral, taintid_OrderFinance, taintid_OrderShipping)),
			host_data_part -> (100, Set(taintid_PartGeneral, taintid_PartPurchasing)),
			host_data_partsupp -> (100, Set(taintid_PartSuppGeneral, taintid_PartSuppPurchasing, taintid_PartSuppKey)),
			host_data_region -> (100, Set(taintid_RegionGeneral)),
			host_data_supplier -> (100, Set(taintid_SupplierGeneral, taintid_SupplierPurchasing)),
			//process
			host_process_finance ->  (200, Set(taintid_CustomerFinance, taintid_LineItemFinance, taintid_OrderFinance, taintid_LineItemKey)),
			host_process_purchasing ->  (200, Set(taintid_PartPurchasing, taintid_SupplierPurchasing, taintid_PartSuppPurchasing, taintid_PartGeneral,
				taintid_PartSuppGeneral, taintid_SupplierGeneral, taintid_PartSuppKey)),
			host_process_shipping ->  (200, Set(taintid_LineItemShipping, taintid_OrderShipping, taintid_LineItemKey, taintid_CustomerGeneral)),
			host_process_geographical ->  (200, Set(taintid_RegionGeneral, taintid_NationGeneral)),
			host_process_private ->  (100, allTaints),
			//client
			host_client -> (0, allTaints)
		)
	)
	
	//data taints
	val taint_customer = Taint(
		"custKey" -> Taint.NO_TAINT, 
		"name" -> Taint(taintid_CustomerGeneral),
		"address" -> Taint(taintid_CustomerGeneral),
		"nationKey" -> Taint(taintid_CustomerGeneral),
		"phone" -> Taint(taintid_CustomerGeneral),
		"acctBal" -> Taint(taintid_CustomerFinance),
		"mktSegment" -> Taint(taintid_CustomerGeneral),
		"comment" -> Taint(taintid_CustomerGeneral)
	)
	val taint_lineitem = Taint(
		"orderKey" -> Taint(taintid_LineItemKey),
		"partKey" -> Taint(taintid_LineItemKey),
		"suppKey" -> Taint(taintid_LineItemKey),
		"lineNumber" -> Taint(taintid_LineItemGeneral),
		"quantity" -> Taint(taintid_LineItemGeneral),
		"extendedPrice" -> Taint(taintid_LineItemFinance),
		"discount" -> Taint(taintid_LineItemFinance),
		"tax" -> Taint(taintid_LineItemFinance),
		"returnFlag" -> Taint(taintid_LineItemGeneral),
		"lineStatus" -> Taint(taintid_LineItemGeneral),
		"shipUpdate" -> Taint(taintid_LineItemShipping),
		"commitDate" -> Taint(taintid_LineItemShipping),
		"receiptDate" -> Taint(taintid_LineItemShipping),
		"shipInstruct" -> Taint(taintid_LineItemShipping),
		"shipMode" -> Taint(taintid_LineItemShipping),
		"comment" -> Taint(taintid_LineItemGeneral)
	)

	val taint_nation = Taint(
		"nationKey" -> Taint.NO_TAINT,
		"name" -> Taint.NO_TAINT,
		"regionKey" -> Taint.NO_TAINT,
		"comment" -> Taint(taintid_NationGeneral)
	)

	val taint_orders = Taint(
		"orderKey" -> Taint.NO_TAINT,
		"custKey" -> Taint(taintid_OrderGeneral),
		"orderStatus" -> Taint(taintid_OrderGeneral),
		"totalPrice" -> Taint(taintid_OrderFinance),
		"orderDate" -> Taint(taintid_OrderGeneral),
		"orderPriority" -> Taint(taintid_OrderShipping),
		"clerk" -> Taint(taintid_OrderGeneral),
		"shipPriority" -> Taint(taintid_OrderShipping),
		"comment" -> Taint(taintid_OrderGeneral)
	)

	val taint_part = Taint(
		"partKey" -> Taint.NO_TAINT,
		"name" -> Taint(taintid_PartGeneral),
		"mfgr" -> Taint(taintid_PartGeneral),
		"brand" -> Taint(taintid_PartGeneral),
		"typ" -> Taint(taintid_PartGeneral),
		"size" -> Taint(taintid_PartGeneral),
		"container" -> Taint(taintid_PartGeneral),
		"retailPrice" -> Taint(taintid_PartPurchasing),
		"comment" -> Taint(taintid_PartGeneral)
	)

	val taint_partsupp = Taint(
		"partKey" -> Taint(taintid_PartSuppKey),
		"suppKey" -> Taint(taintid_PartSuppKey),
		"availQty" -> Taint(taintid_PartSuppPurchasing),
		"supplyCost" -> Taint(taintid_PartSuppPurchasing),
		"comment" -> Taint(taintid_PartSuppGeneral)
	)

	val taint_region = Taint(
		"regionKey" -> Taint.NO_TAINT,
		"name" -> Taint.NO_TAINT,
		"comment" -> Taint(taintid_RegionGeneral)
	)

	val taint_supplier = Taint(
		"suppKey" -> Taint.NO_TAINT,
		"name" -> Taint(taintid_SupplierGeneral),
		"address" -> Taint(taintid_SupplierGeneral),
		"nationKey" -> Taint(taintid_SupplierGeneral),
		"phone" -> Taint(taintid_SupplierGeneral),
		"acctBal" -> Taint(taintid_SupplierPurchasing),
		"comment" -> Taint(taintid_SupplierGeneral)
	)

	def internalBarrier(name : String): Unit = {
		enterBarrier(name)
	}

	object ClientNode extends ReceiveNode[Any] {
		override def relation(): Relation[Any] = {
			//Write an i3ql query...
			import idb.syntax.iql.IR._
			import idb.syntax.iql._
			import DataSchema._

			val customerDB : Rep[Query[Customer]] =
				REMOTE GET (host_data_customer, "db", taint_customer)
			val lineItemDB : Rep[Query[LineItem]] =
				REMOTE GET (host_data_lineitem, "db", taint_lineitem)
			val nationDB : Rep[Query[Nation]] =
				REMOTE GET (host_data_nation, "db", taint_nation)
			val ordersDB : Rep[Query[Orders]] =
				REMOTE GET (host_data_orders, "db", taint_orders)
			val partDB : Rep[Query[Part]] =
				REMOTE GET (host_data_part, "db", taint_part)
			val partSuppDB : Rep[Query[PartSupp]] =
				REMOTE GET (host_data_partsupp, "db", taint_partsupp)
			val regionDB : Rep[Query[Region]] =
				REMOTE GET (host_data_region, "db", taint_region)
			val supplierDB : Rep[Query[Supplier]] =
				REMOTE GET (host_data_supplier, "db", taint_supplier)


			val q1 =
				SELECT (
					(t : Rep[(Int, Date, Int)]) => t,
					SUM((c : Rep[Customer], o : Rep[Orders], l : Rep[LineItem]) => l.extendedPrice * (1 - l.discount))
				) FROM (
					customerDB, ordersDB, lineItemDB
				) WHERE	(
					(c : Rep[Customer], o : Rep[Orders], l : Rep[LineItem]) =>
						c.mktSegment == "[SEGMENT]" AND
						c.custKey == o.custKey AND
						l.orderKey == o.orderKey AND
						o.orderDate < DATE("2016-05-01") AND
						l.shipDate > DATE("2016-01-01")
				) GROUP BY (
					(c : Rep[Customer], o : Rep[Orders], l : Rep[LineItem]) =>
						(l.orderKey, o.orderDate, o.shipPriority)
				)

			//... and add ROOT.
			val r : idb.syntax.iql.IR.Relation[Any] =
				ROOT(host_client, q1)

			r
		}

		override def eventStartTime(e: Any): Long = {
			0
		}
	}

	"Hospital Benchmark" must {
		"run benchmark" in {
			runOn(node_data_customer) { CustomerDBNode.exec() }
			runOn(node_data_lineitem) { LineItemDBNode.exec() }
			runOn(node_data_nation) { NationDBNode.exec() }
			runOn(node_data_orders) { OrdersDBNode.exec() }
			runOn(node_data_part) { PartDBNode.exec() }
			runOn(node_data_partsupp) { PartDBNode.exec() }
			runOn(node_data_region) { RegionDBNode.exec() }
			runOn(node_data_supplier) { SupplierDBNode.exec() }

			runOn(node_process_finance) {IntermediateNode.exec()}
			runOn(node_process_purchasing) {IntermediateNode.exec()}
			runOn(node_process_shipping) {IntermediateNode.exec()}
			runOn(node_process_geographical) {IntermediateNode.exec()}
			runOn(node_process_private) {IntermediateNode.exec()}

			runOn(node_client) { ClientNode.exec() }
		}
	}
}
