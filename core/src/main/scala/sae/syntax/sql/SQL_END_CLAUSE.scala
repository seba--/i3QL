package sae.syntax.sql

import sae.LazyView
import sae.operators.SetDuplicateElimination

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 20:57
 *
 */
trait SQL_END_CLAUSE[Domain <: AnyRef]
{

    def compile() : LazyView[Domain]


    protected def withDistinct(result : LazyView[Domain], distinct : Boolean) : LazyView[Domain] = {
        if (distinct){
            new SetDuplicateElimination[Domain](result)
        }
        else{
            result
        }
    }
}