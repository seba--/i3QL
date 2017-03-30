package idb.algebra.remote.placement

import java.util

import idb.algebra.QueryTransformerAdapter
import idb.algebra.base.RelationalAlgebraBase
import idb.algebra.exceptions.NoServerAvailableException
import idb.algebra.ir.{RelationalAlgebraIRAggregationOperators, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators, RelationalAlgebraIRSetTheoryOperators}
import idb.algebra.remote.taint.QueryTaint
import idb.lms.extensions.RemoteUtils
import idb.query.{Host, QueryEnvironment}

import scala.collection.mutable

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


	override def transform[Domain: Manifest](relation: IR.Rep[IR.Query[Domain]])(implicit env: QueryEnvironment): IR.Rep[IR.Query[Domain]] = {
		//Defines whether the query tree should be use fragments bigger then single operators
		val USE_FRAGMENTS : Boolean = false

		println("global Defs = ")
		IR.globalDefsCache.toList.sortBy(t => t._1.id).foreach(println)

		//Prepare data for CSP Solver function
		val operatorList : Seq[(IR.Rep[IR.Query[_]], Int, Option[Host], Seq[IR.Rep[IR.Query[_]]], Seq[Int])] = operatorListFrom(relation)._2
		val hostList = env.hosts.toSeq

		def hostId(h : Host) : Int =
			hostList.indexWhere(h1 => h1 == h)

		val operators : mutable.Buffer[(Int, Option[Int], Set[Int])] = mutable.Buffer.empty

		for (op <- operatorList) {
			operators += ((
				op._2,
				op._3.map(h => hostId(h)),
				env.findHostsFor(taintOf(op._1).ids).map(h => hostId(h))
			))
		}

		val links : mutable.Buffer[(Int, Int, Int)] = mutable.Buffer.empty

		{
			var index = 0
			for (op <- operatorList) {
				for (linkFromChild <- op._4.zip(op._5)) {
					val childIndex = operatorList.indexWhere(t => t._1 == linkFromChild._1)
					links += ((childIndex, index, linkFromChild._2))
				}
				index = index + 1
			}
		}

		val servers = hostList.map(h => env.priorityOf(h))

		val fragments : Set[Set[Int]] = if (USE_FRAGMENTS) fragmentOperators(links) else Set.empty

		//Compute placement using the CSP solver
		println("operatorList = " + operatorList)
		println("hostList = " + hostList)
		println("fragments = " + fragments)
		val placement : Seq[Int] = computePlacement(operators, links, servers, fragments)

		if (placement == null)
			throw new NoServerAvailableException()

		//Translate results back to the AST
		implicit val placementMap : mutable.Map[IR.Rep[IR.Query[_]], Host] = mutable.Map.empty

		{
			var index = 0
			for (hostId <- placement) {
				placementMap.put(operatorList(index)._1, hostList(hostId))
				index = index + 1
			}
		}

		println("placement Map = ")
		placementMap.toList.foreach(println)

		return super.transform(addRemotes(relation))

	}

	private def addRemotes[Domain : Manifest](
		query: IR.Rep[IR.Query[Domain]]
	)(
		implicit env: QueryEnvironment, placement : mutable.Map[IR.Rep[IR.Query[_]], Host]
	): IR.Rep[IR.Query[Domain]] = {
		import IR._

		def distributeUnary[TA : Manifest, T : Manifest](child : Rep[Query[TA]], build : Rep[Query[TA]] => Rep[Query[T]]) : Rep[Query[T]] = {
			val host = placement(query)
			if (host != placement(child))
				build(remote(addRemotes(child), host))
			else
				build(addRemotes(child))
		}

		def distributeBinary[TA : Manifest, TB : Manifest, T : Manifest](c1 : Rep[Query[TA]], c2 : Rep[Query[TB]], build : (Rep[Query[TA]], Rep[Query[TB]]) => Rep[Query[T]]) : Rep[Query[T]] = {
			val host = placement(query)
			val h1 = placement(c1)
			val h2 = placement(c2)

			if (host == h1 && host == h2)
				build(addRemotes(c1), addRemotes(c2))
			else if (host == h1 && host != h2)
				build(addRemotes(c1), remote(addRemotes(c2), host))
			else if (host != h1 && host == h2)
				build(remote(addRemotes(c1), host), addRemotes(c2))
			else
				build(remote(addRemotes(c1), host), remote(addRemotes(c2), host))
		}

		query match {
			//Base
			case QueryTable(_, _, _, _) => query
			case QueryRelation(_, _, _, _) => query
			case Def (Root(r, h)) => distributeUnary(r, (q : Rep[Query[Domain]]) => root(q, h))
			case Def (Materialize(r)) => distributeUnary(r, (q : Rep[Query[Domain]]) => materialize(q))

			//Basic Operators
			case Def (Selection(r, f)) => distributeUnary(r, (q : Rep[Query[Domain]]) => selection(q, f))
			case Def (Projection(r, f)) => distributeUnary(r, (q : Rep[Query[Any]]) => projection(q, f))
			case Def (CrossProduct(r1, r2)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => crossProduct(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def (EquiJoin(r1, r2, eqs)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => equiJoin(q1, q2, eqs)).asInstanceOf[Rep[Query[Domain]]]
			case Def (DuplicateElimination(r)) => distributeUnary(r, (q : Rep[Query[Domain]]) => duplicateElimination(q))
			case Def (Unnest(r, f)) => distributeUnary(r, (q : Rep[Query[Any]]) => unnest(q, f)).asInstanceOf[Rep[Query[Domain]]]

			//Set theory operators
			case Def (UnionAdd(r1, r2)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => unionAdd(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def (UnionMax(r1, r2)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => unionMax(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def (Intersection(r1, r2)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => intersection(q1, q2)).asInstanceOf[Rep[Query[Domain]]]
			case Def (Difference(r1, r2)) => distributeBinary(r1, r2, (q1 : Rep[Query[Any]], q2 : Rep[Query[Any]]) => difference(q1, q2)).asInstanceOf[Rep[Query[Domain]]]

			//Aggregation operators
			case Def (AggregationSelfMaintained(r, gr, start, fa, fr, fu, ck, conv)) =>
				distributeUnary(r, (q : Rep[Query[Any]]) => aggregationSelfMaintained(q, gr, start, fa, fr, fu, ck, conv))
			case Def (AggregationNotSelfMaintained(r, gr, start, fa, fr, fu, ck, conv)) =>
				distributeUnary(r, (q : Rep[Query[Any]]) => aggregationNotSelfMaintained(q, gr, start, fa, fr, fu, ck, conv))

			//Remote
			case Def(Reclassification(r, t)) => distributeUnary(r, (q : Rep[Query[Domain]]) => reclassification(q, t))
			case Def(Declassification(r, t)) => distributeUnary(r, (q : Rep[Query[Domain]]) => declassification(q, t))
			case Def(ActorDef(_, _, _)) => query

		}
	}

	//Result: Outgoing link strength, (Query, Load usage, Pinned to Host, Children, incoming link strengths)
	private def operatorListFrom(query: IR.Rep[IR.Query[_]]) : (Int, List[(IR.Rep[IR.Query[_]], Int, Option[Host], Seq[IR.Rep[IR.Query[_]]], Seq[Int])]) = {

		println("operator from list " + query)

		import IR._

		query match {
			//Base
			case QueryTable(_, _, _, h) =>
				(1000, scala.List((query, 0, Some(h), scala.Seq.empty, scala.Seq.empty)))
			case QueryRelation(_, _, _, h) =>
				(1000, scala.List((query, 0, Some(h), scala.Seq.empty, scala.Seq.empty)))
			case Def(Root(r, h)) =>
				val t = operatorListFrom(r)
				(t._1, (query, 0, Some(h), scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def (Materialize(r)) =>
				val t = operatorListFrom(r)
				(t._1, (query, 2, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)

			//Basic Operators
			case Def(Selection(r, _)) =>
				val t = operatorListFrom(r)
				(t._1 / 2, (query, 1, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def(Projection(r, _)) =>
				val t = operatorListFrom(r)
				(9 * t._1 / 10, (query, 1, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def(CrossProduct(r1, r2)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				(t1._1 * t2._1, (query, 8, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))
			case Def(EquiJoin(r1, r2, _)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				(2 * scala.math.min(t1._1, t2._1), (query, 4, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))
			case Def (DuplicateElimination(r)) =>
				val t = operatorListFrom(r)
				(3 * t._1 / 4, (query, 2, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def (Unnest(r, _)) =>
				val t = operatorListFrom(r)
				(5 * t._1, (query, 1, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)

			//Set theory operators
			case Def (UnionAdd(r1, r2)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				(t1._1 + t2._1, (query, 1, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))
			case Def (UnionMax(r1, r2)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				(t1._1 + t2._1, (query, 4, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))
			case Def (Intersection(r1, r2)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				((t1._1 + t2._1) / 2, (query, 4, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))
			case Def (Difference(r1, r2)) =>
				val t1 = operatorListFrom(r1)
				val t2 = operatorListFrom(r2)
				((t1._1 + t2._1) / 2, (query, 4, None, scala.Seq(r1, r2), scala.Seq(t1._1, t2._1)) :: (t1._2 ++ t2._2))

			//Aggregation operators
			case Def (AggregationSelfMaintained(r, _, _, _, _, _, _, _)) =>
				val t = operatorListFrom(r)
				(2 * t._1, (query, 3, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def (AggregationNotSelfMaintained(r, _, _, _, _, _, _, _)) =>
				val t = operatorListFrom(r)
				(2 * t._1, (query, 3, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)

			//Remote
			case Def(Reclassification(r, _)) =>
				val t = operatorListFrom(r)
				(t._1, (query, 0, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def (Declassification(r, _)) =>
				val t = operatorListFrom(r)
				(t._1, (query, 0, None, scala.Seq(r), scala.Seq(t._1)) :: t._2)
			case Def(ActorDef(_, h, _)) =>
				(1000, scala.List((query, 0, Some(h), scala.Seq.empty, scala.Seq.empty)))
		}
	}


	private def fragmentOperators(links : Seq[(Int, Int, Int)]): Set[Set[Int]] = {
		val children = new mutable.HashMap[Int, mutable.Set[Int]] with mutable.MultiMap[Int, Int]

		for (l <- links) {
			children.addBinding(l._2, l._1)
		}

		def childrenOf(op : Int) : Set[Int] = {
			children.get(op) match {
				case None => Set.empty
				case Some(s) => s.toSet
			}
		}

		var root : Int = -1
		for (op <- children.keys) {
			if (!children.values.exists(s => s.contains(op))) {
				root = op
			}
		}

		def createFragment(op : Int, root : Set[Int], current : Set[Int]) : Set[Set[Int]] = {
			val c = childrenOf(op)
			if (c.isEmpty)
				Set(current + op)
			else if (c.size == 1) {
				val e = c.head
				if (root.contains(op))
					createFragment(e, root, Set.empty) + (current + op)
				else
					createFragment(e, root, current + op)
			} else {
				c.map(i => createFragment(i, root, Set.empty)).fold(Set.empty)((s1, s2) => s1 ++ s2) + (current + op)
			}
		}

		println("root = " + root)
		println("children = " + children)
		createFragment(root, Set(root), Set.empty)

	}



	private def computePlacement(
        //operator = (load on server, pinned? on server, allowed servers)
        operators : Seq[(Int, Option[Int], Set[Int])],
        //operator links = (from, to, network load)
        links : Seq[(Int, Int, Int)],
        //servers = (load multiplier)
        servers : Seq[Int],
        //fragmented operators -- operators in the same set are put on the same host
        fragments : Set[Set[Int]]
    ): Seq[Int] = {
		import org.jacop.core._
		import org.jacop.constraints._
		import org.jacop.search._
		import org.jacop.constraints.binpacking.Binpacking

		println("Input: ")
		println("operators: " + operators)
		println("links: " + links)
		println("servers: " + servers)


		val numOperators = operators.size
		val numLinks = links.size
		val numServers = servers.size

		//Create global store
		val store = new Store()

		//Define IntVar for each operator. Value = Server number, Domain = correct server numbers
		//Define load for each operator
		val operatorVars = new Array[IntVar](numOperators)
		val operatorResources = new Array[Int](numOperators)

		{
			var i = 0
			for (operator <- operators) {
				operatorVars(i) = new IntVar(store, "op" + i, 0, numServers - 1)
				operatorResources(i) = operator._1
				//Pin operator on table if needed
				operator._2 match {
					case Some(s) => store.impose(new XeqC(operatorVars(i), s))
					case None =>
				}

				for (id <- 0 until numServers) {
					if (!operator._3.contains(id)) {
						store.impose(new XneqC(operatorVars(i), id))
					}
				}

				i = i + 1
			}
		}

		val maxServerLoad = operatorResources.sum

		//Define IntVar for each servers. Value = load on that server, Domain = min/max load
		val serverVars = new Array[IntVar](numServers)

		{
			var i = 0
			for (s <- servers) {
				serverVars(i) = new IntVar(store, "s" + i, 0, maxServerLoad)
				i = i + 1
			}
		}

		//Define operator links
		val linkVars = new Array[IntVar](numLinks)
		var maxBandwidth = 0

		{
			var i = 0
			for (link <- links) {
				linkVars(i) = new IntVar(store, "l" + i, 0, link._3)
				maxBandwidth += link._3
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

		//Define fragment constraints
		for (sameHost <- fragments) {
			var previousOperator : Option[Int] = None
			for (op <- sameHost) {
				previousOperator match {
					case Some(op2) =>
						store.impose(new XeqY(operatorVars(op), operatorVars(op2)))
					case None =>
				}
				previousOperator = Some(op)
			}
		}

		//Define bin packing constraint (= load on all servers)
		store.impose(new Binpacking(operatorVars, serverVars, operatorResources))

		//Define network cost
		val networkCost = new IntVar(store, "network-cost", 0, maxBandwidth)
		store.impose(new SumInt(store, linkVars, "==", networkCost))

		//Define load usage cost
		val loadVars = new Array[IntVar](numServers)

		{
			var i = 0
			for (s <- servers) {
				loadVars(i) = new IntVar(store, "load" + i, 0, maxServerLoad * servers(i))
				store.impose(new XmulCeqZ(serverVars(i), s, loadVars(i)))
				i = i + 1
			}

		}

		val loadCost = new IntVar(store, "load-cost", 0, maxServerLoad * servers.max * numServers)
		store.impose(new SumInt(store, loadVars, "==", loadCost))

		//Define overall cost
		val cost = new IntVar(store, "cost", 0, maxBandwidth * (maxServerLoad * servers.max))
		store.impose(new XmulYeqZ(loadCost, networkCost, cost))

		//Search for a solution and print results
		val search: Search[IntVar] = new DepthFirstSearch[IntVar]()
		val select: SelectChoicePoint[IntVar] =
			new InputOrderSelect[IntVar](store, operatorVars,
				new IndomainMin[IntVar]())
		val result: Boolean = search.labeling(store, select, cost)

		println("Store >>>\n" + store + "\n<<< Store")

		if (result) {
			println("Solution:")
			for (op <- operatorVars)
				println(op)
			operatorVars.map(op => op.value())
		} else {
			println("*** No")
			return null
		}
	}


}
