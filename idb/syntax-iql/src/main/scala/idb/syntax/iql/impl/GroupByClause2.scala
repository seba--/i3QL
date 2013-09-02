package idb.syntax.iql.impl

import idb.syntax.iql._

import idb.syntax.iql.IR._

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 08.08.13
 * Time: 15:29
 * To change this template use File | Settings | File Templates.
 */
case class GroupByClause2[Select : Manifest, DomainA : Manifest, DomainB : Manifest, GroupDomainA : Manifest, GroupDomainB : Manifest, GroupRange : Manifest, Range : Manifest] (
	grouping : (Rep[GroupDomainA], Rep[GroupDomainB]) => Rep[GroupRange],
 	groupClause : CAN_GROUP_CLAUSE_2[Select,DomainA, DomainB,Range])
extends GROUP_BY_CLAUSE_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range] {

}

