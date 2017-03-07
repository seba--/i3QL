package idb.algebra.remote.placement

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.RemoteUtils
import idb.query.QueryEnvironment

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
trait CSPPlacementTransformer
	extends QueryTransformerAdapter with QueryTaint {


	val IR : RelationalAlgebraBase
		with RelationalAlgebraIRBasicOperators
		with RelationalAlgebraIRRemoteOperators
		with RelationalAlgebraIRAggregationOperators
		with RelationalAlgebraIRSetTheoryOperators
		with RemoteUtils

	import IR._

	override def transform[Domain: Manifest](relation: Rep[Query[Domain]])(implicit env: QueryEnvironment): Rep[Query[Domain]] = {
		//Predef.println(s"Transforming ~ $relation")

		val nodes = env.hosts
		null

	}

	private def computePlacement(
        //operator = (load on server, pinned? on server)
        operators : Seq[(Int, Option[Int])],
        //operator links = (from, to, network load)
        links : Seq[(Int, Int, Int)],
        //servers = (maximum load)
        servers : Seq[Int]
    ): Seq[Int] = {
		import org.jacop.core._
		import org.jacop.constraints._
		import org.jacop.search._
		import org.jacop.constraints.binpacking.Binpacking


		val numOperators = operators.size
		val numLinks = links.size
		val numServers = servers.size


		//Create global store
		val store = new Store()

		//Define IntVar for each operator. Value = Server number, Domain = correct server numbers
		//Define load for each operator
		val operatorVars = new Array[IntVar](numOperators)
		val loads = new Array[Int](numOperators)

		{
			var i = 0
			for (operator <- operators) {
				operatorVars(i) = new IntVar(store, "op" + i, 0, numServers - 1)
				loads(i) = operator._1
				//Pin operator on table if needed
				operator._2 match {
					case Some(s) => store.impose(new XeqC(operatorVars(i), s))
					case None =>
				}

				i = i + 1
			}
		}


		//Define IntVar for each servers. Value = load on that server, Domain = min/max load
		val serverVars = new Array[IntVar](numServers)

		{
			var i = 0
			for (server <- servers) {
				serverVars(i) = new IntVar(store, "s" + i, 0, server)
				i = i + 1
			}
		}

		//Define operator links
		val linkVars = new Array[IntVar](numLinks)
		var maximumCost = 0

		{
			var i = 0
			for (link <- links) {
				linkVars(i) = new IntVar(store, "l" + i, 0, link._3)
				maximumCost = maximumCost + link._3
				//Define network constraint for the link
				store.impose(
					new IfThenElse(
						new XeqY(operatorVars(link._1), operatorVars(link._2)),
						new XeqC(linkVars(i), 0),
						new XeqC(linkVars(i), link._3)
					)
				)
				i = i + 1
			}
		}

		//Define cost == network load
		val cost = new IntVar(store, "cost", new IntervalDomain(0, maximumCost))
		store.impose(new SumInt(store, linkVars, "==", cost))

		//Define bin packing constraint (= load on all servers)
		store.impose(new Binpacking(operatorVars, serverVars, loads))

		//Search for a solution and print results
		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
		val select: SelectChoicePoint[IntVar] =
			new InputOrderSelect[IntVar](store, operatorVars,
				new IndomainMin[IntVar]())
		val result: Boolean = search.labeling(store, select, cost)

		if (result) {
			println("Solution:")
			for (op <- operatorVars)
				println(op)
		} else {
			println("*** No")
		}

		operatorVars.map(op => op.value())
	}


}
