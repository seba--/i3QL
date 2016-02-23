package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBase, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.query.{QueryEnvironment, NoColor}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteBase
	extends RelationalAlgebraIRBase
	with RelationalAlgebraIRRemoteOperators{

	override def root[Domain : Manifest] (relation : Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
		//Adds a remote node as child of the root if it has another remote description
		if (relation.color != NoColor)
			root(remote(relation, NoColor, relation.color))
		else
			super.root(relation)
	}



}
