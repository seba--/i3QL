package sae.syntax.sql.impl

import sae.LazyView
import sae.syntax.sql._
import ast.{Compiler, FromClause2, SelectClause2}

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:08
 *
 */
case class FromClause2Syntax[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](selectClause: SelectClause2[_ >: DomainA <: AnyRef, _ >: DomainB <: AnyRef, Range],
                                                                                    relationA: LazyView[DomainA],
                                                                                    relationB: LazyView[DomainB])
    extends FROM_CLAUSE_2[DomainA, DomainB, Range]
{

    def WHERE(predicate: (DomainA) => Boolean) = null

    def WHERE(predicate: WHERE_CLAUSE_PREDICATE_TYPE_SWITCH[DomainB]) = null

    def WHERE[RangeA <: AnyRef, RangeB <: AnyRef](join: JOIN_CONDITION[DomainA, DomainB, RangeA, RangeB]) = null

    def WHERE[UnboundDomain <: AnyRef, RangeA <: AnyRef, UnboundRange <: AnyRef](join: JOIN_CONDITION_UNBOUND_RELATION_1[DomainA, UnboundDomain, RangeA, UnboundRange]) = null

    def compile() = Compiler (
        FromClause2[DomainA, DomainB, Range](// currently we deliberately "forget" the type of the selection, i.e., we could parametrize this type but makes no sense
            selectClause,
            relationA,
            relationB
        )
    )
}