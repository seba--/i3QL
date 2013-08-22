package idb.syntax

import idb.syntax.iql.IR._
import scala.language.implicitConversions
import idb.syntax.iql.planning.{SubQueryToAlgebra, ClauseToAlgebra}
import idb.syntax.iql.compilation.CompilerBinding
import scala.reflect.SourceContext

/**
 *
 * @author Ralf Mitschke
 *
 */
package object iql
{
    // additional keywords. More are available via objects in the package

    val * : STAR_KEYWORD = impl.StarKeyword

    def infix_AND (lhs: Rep[Boolean], rhs: Rep[Boolean])(implicit pos: SourceContext) = boolean_and (lhs, rhs)

    def infix_OR (lhs: Rep[Boolean], rhs: Rep[Boolean])(implicit pos: SourceContext) = boolean_or (lhs, rhs)

    //def infix_unary_NOT(x: Rep[Boolean])(implicit pos: SourceContext) = boolean_negate(x) // TODO behaves strangely
    def NOT (x: Rep[Boolean])(implicit pos: SourceContext) = boolean_negate (x)


    // implicit conversions

    implicit def extentToQuery[Domain] (ext: Extent[Domain])(
        implicit mDom: Manifest[Domain],
        mExt: Manifest[Extent[Domain]]
    ): Rep[Query[Domain]] = extent (ext)


    implicit def plan[Select : Manifest, Domain <: GroupDomain : Manifest, GroupDomain : Manifest, GroupRange <: Select : Manifest, Range : Manifest] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    def planWithContext[Select : Manifest, Domain <: GroupDomain : Manifest, GroupDomain : Manifest, GroupRange <: Select : Manifest, Range : Manifest, ContextRange] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    )(
        context: Rep[Query[ContextRange]],
        contextParameter: Rep[ContextRange],
        contextManifest: Manifest[ContextRange]
    ): Rep[Query[ContextRange]] = {
        SubQueryToAlgebra (
            clause, context, contextParameter
        )(
            implicitly[Manifest[Select]], implicitly[Manifest[Domain]], implicitly[Manifest[GroupDomain]], implicitly[Manifest[GroupRange]], implicitly[Manifest[Range]], contextManifest
        )
    }

    def planSubQueryWithContext[Select, Domain <: GroupDomain, GroupDomain, GroupRange <: Select, Range, ContextRange] (
        selectType: Manifest[Select],
        domainType: Manifest[Domain],
		groupDomainType : Manifest[GroupDomain],
		groupRangeType : Manifest[GroupRange],
        rangeType: Manifest[Range]
    )(
        subQuery: SubQuery[Range],
        context: Rep[Query[ContextRange]],
        contextParameter: Rep[ContextRange]
    ): Rep[Query[ContextRange]] = subQuery match {
        case q1: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range] =>
            SubQueryToAlgebra (
                q1, context, contextParameter
            )(
                selectType, domainType, groupDomainType, groupRangeType, rangeType, contextParameter.tp
            )
        case _ => throw new UnsupportedOperationException
    }


    implicit def plan[SelectA: Manifest, SelectB: Manifest, DomainA <: SelectA : Manifest,
    DomainB <: SelectB : Manifest, Range: Manifest] (
        clause: IQL_QUERY_2[SelectA, SelectB, DomainA, DomainB, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    implicit def plan[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, Range: Manifest] (
        clause: IQL_QUERY_3[DomainA, DomainB, DomainC, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    implicit def plan[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, Range: Manifest] (
        clause: IQL_QUERY_4[DomainA, DomainB, DomainC, DomainD, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    implicit def plan[DomainA: Manifest, DomainB: Manifest, DomainC: Manifest, DomainD: Manifest, DomainE: Manifest,
    Range: Manifest] (
        clause: IQL_QUERY_5[DomainA, DomainB, DomainC, DomainD, DomainE, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)


    implicit def compile[Select : Manifest, Domain <: GroupDomain : Manifest, GroupDomain : Manifest, GroupRange <: Select : Manifest, Range : Manifest] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ): Relation[Range] =
        CompilerBinding.compile (plan (clause))

    implicit def compile[SelectA: Manifest, SelectB: Manifest, DomainA <: SelectA : Manifest,
    DomainB <: SelectB : Manifest, Range: Manifest] (
        clause: IQL_QUERY_2[SelectA, SelectB, DomainA, DomainB, Range]
    ): Relation[Range] =
        CompilerBinding.compile (plan (clause))

}
