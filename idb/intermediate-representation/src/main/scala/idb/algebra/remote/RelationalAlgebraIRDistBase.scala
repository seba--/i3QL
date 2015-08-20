package idb.algebra.remote

import idb.algebra.ir.{RelationalAlgebraIRBase, RelationalAlgebraIRBasicOperators, RelationalAlgebraIRRemoteOperators}

/**
 * @author Mirko Köhler
 */
trait RelationalAlgebraIRDistBase
	extends RelationalAlgebraIRBase
	with RelationalAlgebraIRRemoteOperators{

	override def root[Domain : Manifest] (relation : Rep[Query[Domain]]): Rep[Query[Domain]] = {
		//Adds a remote node as child of the root if it has another remote description
		if (relation.remoteDesc != DefaultDescription)
			root(remote(relation, DefaultDescription, relation.remoteDesc))
		else
			super.root(relation)
	}



}
