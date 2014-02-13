package idb.syntax

import idb.syntax.iql.IR._
import idb.syntax.iql.compilation.CompilerBinding
import idb.syntax.iql.planning.{SubQueryToAlgebra, ClauseToAlgebra}
import scala.language.implicitConversions
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

    // TODO behaves strange
    //def infix_AND (lhs: Rep[Boolean], rhs: Rep[Boolean])(implicit pos: SourceContext) = boolean_and (lhs, rhs)

    // TODO behaves strange
    //def infix_OR (lhs: Rep[Boolean], rhs: Rep[Boolean])(implicit pos: SourceContext) = boolean_or (lhs, rhs)

    case class InfixBooleanOps (lhs: Rep[Boolean])
    {
        def AND (rhs: Rep[Boolean]) = boolean_and (lhs, rhs)

        def OR (rhs: Rep[Boolean]) = boolean_or (lhs, rhs)
    }

    implicit def booleanToInfixOps (lhs: Rep[Boolean]) =
        InfixBooleanOps (lhs)

    //def infix_unary_NOT(x: Rep[Boolean])(implicit pos: SourceContext) = boolean_negate(x) // TODO behaves strange
    def NOT (x: Rep[Boolean])(implicit pos: SourceContext) = boolean_negate (x)


    // implicit conversions

    implicit def extentToQuery[Domain] (ext: Extent[Domain])(
        implicit mDom: Manifest[Domain],
        mExt: Manifest[Extent[Domain]]
    ): Rep[Query[Domain]] = extent (ext)


    implicit def relationToQuery[Domain] (rel: Relation[Domain])(
        implicit mDom: Manifest[Domain],
        mExt: Manifest[Relation[Domain]]
    ): Rep[Query[Domain]] = relation (rel)


    case class QueryInfixOps[Range: Manifest] (query: Rep[Query[Range]])
    {
        def UNION[OtherRange <: Range : Manifest] (other: Rep[Query[OtherRange]]): Rep[Query[Range]] =
            unionMax (query, other)

        def UNION[OtherRange <: Range : Manifest]  (all: ALL_QUERY[OtherRange]): Rep[Query[Range]] =
            unionAdd (query, all.query)
    }

    implicit def queryToInfixOps[Range: Manifest] (query: Rep[Query[Range]]) =
        QueryInfixOps (query)

    implicit def relationToInfixOps[Range: Manifest] (query: Relation[Range]) =
        QueryInfixOps (relation (query))


    implicit def clause1ToInfixOps[Select: Manifest, Domain <: GroupDomain : Manifest, GroupDomain: Manifest,
    GroupRange <: Select : Manifest, Range: Manifest] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ) = queryToInfixOps (plan (clause))

    implicit def clause2ToInfixOps[Select: Manifest, DomainA <: GroupDomainA : Manifest,
    DomainB <: GroupDomainB : Manifest, GroupDomainA: Manifest, GroupDomainB: Manifest,
    GroupRange <: Select : Manifest, Range: Manifest] (
        clause: IQL_QUERY_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range]
    ) = queryToInfixOps (plan (clause))

    implicit def plan[Select: Manifest, Domain <: GroupDomain : Manifest, GroupDomain: Manifest,
    GroupRange <: Select : Manifest, Range: Manifest] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    def planWithContext[Select: Manifest, Domain <: GroupDomain : Manifest, GroupDomain: Manifest,
    GroupRange <: Select : Manifest, Range: Manifest, ContextRange] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    )(
        context: Rep[Query[ContextRange]],
        contextParameter: Rep[ContextRange],
        contextManifest: Manifest[ContextRange]
    ): Rep[Query[ContextRange]] = {
        SubQueryToAlgebra (
            clause, context, contextParameter
        )(
            implicitly[Manifest[Select]], implicitly[Manifest[Domain]], implicitly[Manifest[GroupDomain]],
            implicitly[Manifest[GroupRange]], implicitly[Manifest[Range]], contextManifest
        )
    }

    def planSubQueryWithContext[Select, Domain <: GroupDomain, GroupDomain, GroupRange <: Select, Range, ContextRange] (
        selectType: Manifest[Select],
        domainType: Manifest[Domain],
        groupDomainType: Manifest[GroupDomain],
        groupRangeType: Manifest[GroupRange],
        rangeType: Manifest[Range]
    )(
        subQuery: SubQuery[Range],
        context: Rep[Query[ContextRange]],
        contextParameter: Rep[ContextRange]
    ): Rep[Query[ContextRange]] = subQuery match {
        case q1: IQL_QUERY_1[Select@unchecked, Domain@unchecked, GroupDomain@unchecked, GroupRange@unchecked,
            Range@unchecked] =>
            SubQueryToAlgebra (
                q1, context, contextParameter
            )(
                selectType, domainType, groupDomainType, groupRangeType, rangeType, contextParameter.tp
            )
        case _ => throw new UnsupportedOperationException
    }


    implicit def plan[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)


    implicit def plan[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        DomainC <: GroupDomainC : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupDomainC: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange,
            Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    implicit def plan[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        DomainC <: GroupDomainC : Manifest,
        DomainD <: GroupDomainD : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupDomainC: Manifest,
        GroupDomainD: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC,
            GroupDomainD, GroupRange, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)

    implicit def plan[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        DomainC <: GroupDomainC : Manifest,
        DomainD <: GroupDomainD : Manifest,
        DomainE <: GroupDomainE : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupDomainC: Manifest,
        GroupDomainD: Manifest,
        GroupDomainE: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB,
            GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range]
    ): Rep[Query[Range]] =
        ClauseToAlgebra (clause)


    implicit def compile[Range: Manifest] (
	    query: Rep[Query[Range]]
	): Relation[Range] = {
        val res = CompilerBinding.compile (query)
    //    CompilerBinding.reset
        res
    }

    implicit def compile[
        Select: Manifest,
        Domain <: GroupDomain : Manifest,
        GroupDomain: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_1[Select, Domain, GroupDomain, GroupRange, Range]
    ): Relation[Range] = compile (plan (clause))

    implicit def compile[Select: Manifest, DomainA <: GroupDomainA : Manifest, DomainB <: GroupDomainB : Manifest,
    GroupDomainA: Manifest, GroupDomainB: Manifest,
    GroupRange <: Select : Manifest, Range: Manifest] (
        clause: IQL_QUERY_2[Select, DomainA, DomainB, GroupDomainA, GroupDomainB, GroupRange, Range]
    ): Relation[Range] = compile (plan (clause))

    implicit def compile[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        DomainC <: GroupDomainC : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupDomainC: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_3[Select, DomainA, DomainB, DomainC, GroupDomainA, GroupDomainB, GroupDomainC, GroupRange,
            Range]
    ): Relation[Range] = compile (plan (clause))

    implicit def compile[
      Select: Manifest,
      DomainA <: GroupDomainA : Manifest,
      DomainB <: GroupDomainB : Manifest,
      DomainC <: GroupDomainC : Manifest,
      DomainD <: GroupDomainD : Manifest,
      GroupDomainA: Manifest,
      GroupDomainB: Manifest,
      GroupDomainC: Manifest,
      GroupDomainD: Manifest,
      GroupRange <: Select : Manifest,
      Range: Manifest
    ] (
        clause: IQL_QUERY_4[Select, DomainA, DomainB, DomainC, DomainD, GroupDomainA, GroupDomainB, GroupDomainC,
            GroupDomainD, GroupRange, Range]
    ): Relation[Range] = compile (plan (clause))

    implicit def compile[
        Select: Manifest,
        DomainA <: GroupDomainA : Manifest,
        DomainB <: GroupDomainB : Manifest,
        DomainC <: GroupDomainC : Manifest,
        DomainD <: GroupDomainD : Manifest,
        DomainE <: GroupDomainE : Manifest,
        GroupDomainA: Manifest,
        GroupDomainB: Manifest,
        GroupDomainC: Manifest,
        GroupDomainD: Manifest,
        GroupDomainE: Manifest,
        GroupRange <: Select : Manifest,
        Range: Manifest
    ] (
        clause: IQL_QUERY_5[Select, DomainA, DomainB, DomainC, DomainD, DomainE, GroupDomainA, GroupDomainB,
            GroupDomainC, GroupDomainD, GroupDomainE, GroupRange, Range]
    ): Relation[Range] = compile (plan (clause))

    def reset() {
        CompilerBinding.reset
    }




}
