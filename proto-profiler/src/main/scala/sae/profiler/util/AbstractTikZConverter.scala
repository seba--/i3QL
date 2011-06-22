package sae.profiler.util

import sae.LazyView

/**
 * 
 * Author: Ralf Mitschke
 * Created: 22.06.11 13:13
 *
 */

abstract class AbstractTikZConverter
        extends QueryAnalyzer
{

    type T = String

    private def leafFunc() : String = ""

    private def joinResults : (String, String) => String = _ + _

    def apply[Domain <: AnyRef]( view : LazyView[Domain] )
    {
        analyze[Domain](view)(leafFunc, joinResults)
    }
}