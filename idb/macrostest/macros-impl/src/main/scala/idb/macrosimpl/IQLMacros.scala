package idb.macrosimpl

import idb.syntax.iql.IQL_QUERY
import idb.Relation
import scala.reflect.macros.Context

import language.experimental.macros
import idb.syntax.iql.impl._
import idb.syntax.iql.impl.FromClause1
import idb.syntax.iql.impl.WhereClause1
import idb.syntax.iql.impl.FromClause2
import idb.syntax.iql.impl.SelectClause1
import idb.syntax.iql.impl.SelectClause2

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.06.13
 * Time: 16:07
 * To change this template use File | Settings | File Templates.
 */
object IQLMacros {

/*	def compile[Domain,Range](query : IQL_QUERY[Domain,Range]) : Relation[Range] = macro compile_impl[Range]

	def compile_impl[Range : c.WeakTypeTag]	(c : Context)
											   (query : c.Expr[IQL_QUERY[Domain,Range]]) : c.Expr[Relation[Range]] = {

		import c.universe._

		println( showRaw( query.tree ) )

		reify{ null }
	}            */


}
