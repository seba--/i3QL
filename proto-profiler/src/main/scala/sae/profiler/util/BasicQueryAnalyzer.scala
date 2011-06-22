package sae.profiler.util

import sae.{IndexedView, MaterializedView, LazyView}
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

    private def leafFunc()
    {
        profile.baseRelations += 1
    }

    private def joinResults : (Unit, Unit) => Unit = (_, _) => {}

    def apply[Domain <: AnyRef]( view : LazyView[Domain] )
    {
        analyze[Domain](view)(leafFunc, joinResults)
    }

    def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[LazyView[Parent]])
    {
        profile.indexedViews += 1
    }

    def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[LazyView[Parent]])
    {
        profile.materializedViews += 1
    }

    def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[LazyView[Parent]])
    {
        profile.equiJoins += 1
    }

    def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[LazyView[Parent]])
    {
        profile.duplicateEliminations += 1
    }


    def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[LazyView[Parent]])
    {
        profile.projections += 1
    }

    def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[LazyView[Parent]])
    {
        profile.selections += 1
    }

    def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[LazyView[Parent]])
    {
        profile.unions += 1
    }
}

