package sae

/**
 * 
 * Author: Ralf Mitschke
 * Created: 31.05.11 17:31
 *
 */
trait Contains[V <: AnyRef]
{
    def contains(element : V) : Boolean
}