package sae.profiler.util

import sae.{IndexedView, MaterializedView, Relation}
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy
import sae.operators._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 17.06.11 11:17
 *
 * The static query analyzer determines the individual constituents of a query.
 * This means which operators are part of the query and which of these are materialized, or indexed
 */
class BasicQueryAnalyzer
    extends QueryAnalyzer
{
    type T = Unit


    val profile = new BasicQueryProfile

    def apply[Domain <: AnyRef]( view : Relation[Domain] )
    {
        analyze[Domain](view)
    }

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.indexedViews += 1
        childContinuation
    }

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.materializedViews += 1
        childContinuation
    }

    def differenceView[Domain <: AnyRef, Parent <: AnyRef](view: Difference[Domain], parent: Option[Relation[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.differences += 1
        leftContinuation
        rightContinuation
    }

    def intersectionView[Domain <: AnyRef, Parent <: AnyRef](view: Intersection[Domain], parent: Option[Relation[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.intersections += 1
        leftContinuation
        rightContinuation
    }

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[Relation[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.unions += 1
        leftContinuation
        rightContinuation
    }

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[Relation[Parent]], leftContinuation: => Unit, rightContinuation: => Unit)
    {
        profile.equiJoins += 1
        leftContinuation
        rightContinuation
    }

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.duplicateEliminations += 1
        childContinuation
    }

    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.projections += 1
        childContinuation
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.selections += 1
        childContinuation
    }

    def baseView[Domain <: AnyRef, Parent <: AnyRef](view: Relation[Domain], parent: Option[Relation[Parent]])
    {
        profile.baseRelations += 1
    }

    def transitiveClosureView[Domain <: AnyRef, Vertex <: AnyRef, Parent <: AnyRef](view: TransitiveClosure[Domain, Vertex], parent: Option[Relation[Parent]], childContinuation: => Unit)
    {
        profile.transitiveClosures += 1
        childContinuation
    }
}

