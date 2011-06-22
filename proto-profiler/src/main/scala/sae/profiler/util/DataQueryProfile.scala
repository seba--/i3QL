package sae.profiler.util

import collection.immutable.HashMap
import sae.LazyView
import sae.operators._
import sae.collections.BagResult
import sae.operators.Conversions.HashIndexedViewProxy

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.06.11 11:11
 *
 * A query profile, that tracks the data flowing through the query execution tree.
 */
class DataQueryProfile {

    private var counters = HashMap[LazyView[_], CountingObserver[_]]()

    // we make the structure of parents explicit here, so we do not need to compute them later
    //private var parents = HashMap[LazyView[_], LazyView[_]]()

    // we make the structure of parents explicit here, so we do not need an additional visitor later
    //private var children = HashMap[LazyView[_], List[LazyView[_]]]()

    private var queries = List[LazyView[_]]()

    def addOperator( op : LazyView[_ <: AnyRef], parent : Option[LazyView[_]] )
    {
        if( parent == None )
        {
            queries ::= op
        }

        if( ! counters.isDefinedAt( op ) )
        {
            val counter = new CountingObserver[Any]()
            op.addObserver(counter)
            counters += { op -> counter}
        }
    }


    object DataProfileToTikZConverter extends AbstractTikZConverter
    {
        def makeChild[Parent <: AnyRef](value : String, hasParent : Boolean, edgeCount : Int) =
            if( !hasParent ) value + "\n"
            else "child { \n" +
                    value + "\n" +
                    "edge from parent\n" +
                    "node[above] {" + edgeCount + "}" +
                    "}\n"

        def baseView[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[base] {Base}", parent != None, counters(view).count)
        }

        def indexedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: HashIndexedViewProxy[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[proxy] {Index}", parent != None, counters(view).count)
        }

        def materializedProxyView[Domain <: AnyRef, Parent <: AnyRef](view: BagResult[Domain], parent: Option[LazyView[Parent]]) =
            makeChild("node[proxy] {Materialization}", parent != None, counters(view).count )

        def differenceView[Domain <: AnyRef, Parent <: AnyRef](view: Difference[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {∖}", parent != None, counters(view).count)
        }

        def intersectionView[Domain <: AnyRef, Parent <: AnyRef](view: Intersection[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {∩}", parent != None, counters(view).count)
        }

        def unionView[Range <: AnyRef, DomainA <: Range, DomainB <: Range, Parent <: AnyRef](view: Union[Range, DomainA, DomainB], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {∪}", parent != None, counters(view).count )
        }


        def equiJoinView[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef, Key <: AnyRef, Parent <: AnyRef](view: EquiJoin[DomainA, DomainB, Range, Key], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {⋈}", parent != None, counters(view).count)
        }

        def duplicateEliminationView[Domain <: AnyRef, Parent <: AnyRef](view: DuplicateElimination[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {δ}", parent != None, counters(view).count)
        }

        def projectionView[Domain <: AnyRef, Range <: AnyRef, Parent <: AnyRef](view: Projection[Domain, Range], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {Π}", parent != None, counters(view).count)
        }

        def selectionView[Domain <: AnyRef, Parent <: AnyRef](view: Selection[Domain], parent: Option[LazyView[Parent]])
        {
            makeChild("node[operator] {σ}", parent != None, counters(view).count)
        }
    }

    def toTikZ : String =
    {
        "\\begin{tikzpicture}[grow=left]\n" +
        queries.foreach( (view : LazyView[_]) =>
            "\\" + DataProfileToTikZConverter(view.asInstanceOf) + "\n"
        )
        "\\end{tikzpicture}"
    }
}