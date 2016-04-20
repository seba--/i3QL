package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBase, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}
import idb.query.colors.Color
import idb.query.{QueryEnvironment}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRRemoteBase
	extends RelationalAlgebraIRBase
	with RelationalAlgebraIRRemoteOperators{

	override def root[Domain : Manifest] (relation : Rep[Query[Domain]])(implicit queryEnvironment : QueryEnvironment): Rep[Query[Domain]] = {
		//Adds a remote node as child of the root if it has another remote description
		if (relation.color != Color.NO_COLOR)
			root(remote(relation, Color.NO_COLOR, relation.color))
		else
			super.root(relation)
	}



}
