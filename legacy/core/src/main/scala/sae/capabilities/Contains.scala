package sae.capabilities

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.05.11 17:31
 *
 */
trait Contains[V]
{
    def contains(element: V): Boolean
}