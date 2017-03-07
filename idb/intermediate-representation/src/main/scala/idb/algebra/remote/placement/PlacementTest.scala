package idb.algebra.remote.placement

import java.util

import org.jacop.constraints.binpacking.Binpacking

/**
  * TODO: Add documentation of class!
  *
  * @author mirko
  */
object PlacementTest {
	import org.jacop.core._
	import org.jacop.constraints._
	import org.jacop.search._

	def computePlacement(
        //operator = (load on server, pinned? on server)
		operators : Seq[(Int, Option[Int])],
        //operator links = (from, to, network load)
        links : Seq[(Int, Int, Int)],
        //servers = (maximum load)
        servers : Seq[Int]
	): Seq[Int] = {
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


	def placementTest1(): Unit = {
		val store = new Store()

		//Define IntVar for each operator. Value = Server number
		val t0 = new IntVar(store, "t0", new IntervalDomain(0, 3))
		val t1 = new IntVar(store, "t1", new IntervalDomain(0, 3))

		val o0 = new IntVar(store, "o0", new IntervalDomain(0, 3))
		val o1 = new IntVar(store, "o1", new IntervalDomain(0, 3))

		val r = new IntVar(store, "r", new IntervalDomain(0, 3))

		val allOperators = Array(t0, t1, o0, o1, r)

		//Define maximum operator loads
		val loads = Array(10, 10, 10, 100, 0)

		//Define servers. Value = load on that server, Domain = min/max load
		val s0 = new IntVar(store, "client", new IntervalDomain(0, 10))
		val s1 = new IntVar(store, "s1", new IntervalDomain(0, 150))
		val s2 = new IntVar(store, "s2", new IntervalDomain(0, 50))
		val s3 = new IntVar(store, "s3", new IntervalDomain(0, 200))

		val allServers = Array(s0, s1, s2, s3)



		//Pin tables/result on servers
		store.impose(new XeqC(t0, 1) )
		store.impose(new XeqC(t1, 2) )
		store.impose(new XeqC(r, 0) )

		//Define network links. Value = Stress on that link
		val l0 = new IntVar(store, "l0", new IntervalDomain(0, 1000))
		val l1 = new IntVar(store, "l1", new IntervalDomain(0, 1000))
		val l2 = new IntVar(store, "l2", new IntervalDomain(0, 1000))
		val l3 = new IntVar(store, "l3", new IntervalDomain(0, 1000))

		val allLinks = Array(l0, l1, l2, l3)

		store.impose(new IfThenElse(new XeqY(t0, o0), new XeqC(l0, 0), new XeqC(l0, 100)))
		store.impose(new IfThenElse(new XeqY(o0, o1), new XeqC(l2, 0), new XeqC(l2, 50)))
		store.impose(new IfThenElse(new XeqY(o1, r), new XeqC(l3, 0), new XeqC(l3, 150)))
		store.impose(new IfThenElse(new XeqY(t1, o1), new XeqC(l1, 0), new XeqC(l1, 100)))

		//Define cost (Total stress on the network)
		val cost = new IntVar(store, "cost", new IntervalDomain(0, 10000))
		store.impose(new SumInt(store, allLinks, "==", cost))

		//Define bin packing constraint
		store.impose(new Binpacking(allOperators, allServers, loads))

		//Search for a solution and print results
		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
		val select: SelectChoicePoint[IntVar] =
			new InputOrderSelect[IntVar](store, Array.concat(allOperators, allLinks),
				new IndomainMin[IntVar]())
		val result: Boolean = search.labeling(store, select, cost)

		if (result)
			println("Solution: " + t0 + ", " + t1 + ", " +
				o0 + ", " + o1 + ", " + r + ", " + cost)
		else
			println("*** No")
	}

	def main(args: Array[String]) {

		//placementTest1()

		val p = computePlacement(
			Seq((10, Some(1)), (10, Some(2)), (10, None), (100, None), (0, Some(0))),
			Seq((0, 2, 100), (1, 3, 100), (2, 3, 50), (3, 4, 150)),
			Seq(0, 200, 200, 200)
		)

		println(p)


//		val store = new Store()
//		// define FD store
//		val size = 7
//		// define finite domain variables
//		val v = new Array[IntVar](size)
//
//		for (i <- 0 until size)
//			v(i) = new IntVar(store, "v" + i, 0, 100)
//
//		val flow = new IntVar(store, "c", 0, 100)
//
//		// define constraints
//		store.impose(new XgteqC(v(0), 0) )
//		store.impose(new XlteqC(v(0), 1) )
//
//		store.impose(new XgteqC(v(1), 0) )
//		store.impose(new XlteqC(v(1), 2) )
//
//		store.impose(new XgteqC(v(2), 0) )
//		store.impose(new XlteqC(v(2), 2) )
//
//		store.impose(new XgteqC(v(3), 0) )
//		store.impose(new XlteqC(v(3), 1) )
//
//		store.impose(new XgteqC(v(4), 0) )
//		store.impose(new XlteqC(v(4), 1) )
//
//		store.impose(new XplusYeqZ(v(0), v(2), v(5)))
//		store.impose(new XplusYeqZ(v(0), v(4), v(1)))
//		store.impose(new XplusYeqZ(v(1), v(3), v(6)))
//		store.impose(new XplusYeqZ(v(4), v(3), v(2)))
//
//		store.impose(new XeqY(flow, v(5)))
//
//
//		// search for a solution and print results
//		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
//		val select: SelectChoicePoint[IntVar] =
//			new InputOrderSelect[IntVar](store, v,
//				new IndomainMin[IntVar]())
//		val result: Boolean = search.labeling(store, select)
//
//		if (result)
//			println("Solution: " + v(0) + ", " + v(1) + ", " +
//				v(2) + ", " + v(3))
//		else
//			println("*** No")
	}
}
