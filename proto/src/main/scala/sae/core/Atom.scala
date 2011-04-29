package sae.core

import sae.Observable
import sae.Observer
import sae.View

/**
 * An atom is a value in the underlying database.
 * All atoms are attributed a unique key. 
 * Note that the value of the atom is not necessarily unique.
 */
trait Atom[V]
{
	val value : V
}