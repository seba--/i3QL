package idb.syntax.iql

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 21.06.13
 * Time: 14:05
 * To change this template use File | Settings | File Templates.
 */
object CompilerDebug {

	def compile[Domain: Manifest, Range: Manifest] (clause: IQL_QUERY[Domain, Range]):Relation[Range] = {
		val ir = plan(clause)

		Predef.println("COMPILER-TREE -> " + ir)

		Compiler.compile(ir)
	}

}
