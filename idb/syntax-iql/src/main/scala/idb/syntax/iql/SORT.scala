package idb.syntax.iql

import idb.Relation
import idb.collections.impl.SortedBag

/**
  * Syntax for sorting compiled(!) relations.
  *
  * @author mirko
  */
//TODO: Change this to ORDER BY and add it to the LMS AST.
object SORT {

	trait Sortable[T] {
		def BY[Key](f : T => Key)(implicit ordering : Ordering[Key]) : Relation[T]
	}

	def apply[T](rel : Relation[T]) =
		new Sortable[T]() {
			override def BY[Key](f : T => Key)(implicit ordering : Ordering[Key]) : Relation[T] =
				new SortedBag[T, Key](rel, f)
		}

}
