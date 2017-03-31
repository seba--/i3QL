package idb.syntax.iql

import akka.actor.ActorPath

import idb.algebra.print.RelationalAlgebraPrintPlan
import idb.algebra.remote.PlacementStrategy
import idb.query.{Host, QueryEnvironment, RemoteHost}
import idb.syntax.iql
import idb.syntax.iql.IR._
import idb.syntax.iql.runtime.{CompilerBinding, RemoteUtils}

/**
 * @author Mirko Köhler
 */
object ROOT {

	def UNSAFE[Domain : Manifest](host : ActorPath, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] = {
		val relation : Relation[Domain] = query
		UNSAFE(host, relation)
	}

	def UNSAFE[Domain : Manifest](host : ActorPath, relation : Relation[Domain])(implicit env : QueryEnvironment) : Relation[Domain] = {
		val ref = RemoteUtils.deploy(env.system, host)(relation)
		val r = RemoteUtils.fromWithDeploy(env.system, ref)
		r
	}

	def UNSAFE[Domain : Manifest](host : RemoteHost, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, query)


	def UNSAFE[Domain : Manifest](host : RemoteHost, relation : Relation[Domain])(implicit env : QueryEnvironment) : Relation[Domain] =
		UNSAFE(host.path, relation)

	def apply[Domain : Manifest](rootHost : RemoteHost, query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] = {

		Predef.println(s"### ROOT: Executing placement algorithm. query = $query")
		object Placement extends PlacementStrategy {
			val IR = idb.syntax.iql.IR
		}
		 val q = Placement.transform(root(query, rootHost))

		val printer = new RelationalAlgebraPrintPlan {
			override val IR = idb.syntax.iql.IR
		}
		Predef.println(printer.quoteRelation(q))

		Predef.println("### ROOT: Compiling query...")
		val relation : Relation[Domain] = q
		val RemoteHost(_, queryPath) = q.host

		Predef.println("ROOT: Deploying relation...")
		val ref = RemoteUtils.deploy(env.system, queryPath)(relation)
		val r = RemoteUtils.fromWithDeploy(env.system, ref)

		Predef.println("ROOT: Finished...")
		r
//		relation
	}

	def apply[Domain : Manifest](query : Rep[Query[Domain]])(implicit env : QueryEnvironment) : Relation[Domain] = {
		val r = compile(query)
		CompilerBinding.initialize(r)
		r
	}
}
