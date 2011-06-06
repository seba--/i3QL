package sae.functions

import sae.{Observer, Observable, MaterializedView}

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.05.11 17:21
 *
 */
// TODO give a general definition af maintained functions that also applies to the aggregations
trait ElementOf[Domain <: AnyRef]
        extends (Domain => Boolean)
            with Observable[(Domain, Boolean)]
            with Observer[Domain]
{
    val relation: MaterializedView[Domain]

    def apply(v: Domain): Boolean = relation.contains(v)

    /*
    def andThenIncremental[A]( g : (Domain => Boolean) with Observer[Domain]) = {
        this addObserver andThenObserver(g)
        new ElementOf[Domain] {
            val relation = ElementOf.this.relation

        }
    }

    def andThenObserver(o : Observer[Domain]) = new Observer[(Domain, Boolean)] {
        def added(v: (Domain, Boolean))
        {

        }

        def removed(v: (Domain, Boolean))
        {

        }

        def updated(oldV: (Domain, Boolean), newV: (Domain, Boolean))
        {

        }
    }
    */

    def added(v: Domain)
    {
        element_added((v, relation.contains(v)))
    }

    def removed(v: Domain)
    {
        element_removed((v, relation.contains(v)))
    }

    def updated(oldV: Domain, newV: Domain)
    {
        val oldE = relation.contains(oldV)
        val newE = relation.contains(newV)
        if( oldE != newE )
        {
            element_updated((oldV, oldE), (newV, newE))
        }
    }

    override def toString() = "<∈ " + relation.toString + ">"

}


object ElementOf
{
    def apply[Domain <: AnyRef](r: MaterializedView[Domain]) = new ElementOf[Domain]
    {
        r addObserver this

        val relation = r
    }
}