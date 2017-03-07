package idb.algebra.remote.placement

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
	): Unit = {

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

		//Define servers. Value = load on that server, Domain = min/max load
		val s0 = new IntVar(store, "client", new IntervalDomain(0, 10))
		val s1 = new IntVar(store, "s1", new IntervalDomain(0, 150))
		val s2 = new IntVar(store, "s2", new IntervalDomain(0, 50))
		val s3 = new IntVar(store, "s3", new IntervalDomain(0, 200))

		val allServers = Array(s0, s1, s2, s3)

		//Define maximum loads
		val loads = Array(10, 10, 10, 100, 0)

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

		placementTest1()

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
