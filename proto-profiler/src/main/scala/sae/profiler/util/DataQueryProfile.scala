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

    private var counters = HashMap[LazyView[_ <: AnyRef], CountingObserver[_]]()

    // we make the structure of parents explicit here, so we do not need to compute them later
    //private var parents = HashMap[LazyView[_], LazyView[_]]()

    // we make the structure of parents explicit here, so we do not need an additional visitor later
    //private var children = HashMap[LazyView[_], List[LazyView[_]]]()

    private var queries = List[LazyView[_ <: AnyRef]]()

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


    object DataProfileToTikZConverter extends TikZTreeConverter
    {
        protected def makeEdgeLabel[Domain <: AnyRef, Parent <: AnyRef](view: LazyView[Domain], parent: LazyView[Parent]) =
            "node[above] {" + counters(view).count + "}"
    }

    val beginTiKz = "\n\\begin{tikzpicture}\n"

    val endTikZ = "\\end{tikzpicture}\n\n"

    def toTikZ : String =
    {
        (
            "" /:
            queries.map( (view : LazyView[_ <: AnyRef]) =>
                beginTiKz + "\\" + DataProfileToTikZConverter(view) + ";\n" + endTikZ
            )
        )(_ + _)

    }
}