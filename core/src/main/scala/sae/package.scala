import sae.collections.{SetResult, BagResult}

/**
 *
 * @author Ralf Mitschke
 *
 */
package object sae
{

    implicit def lazyViewToResult[V <: AnyRef](relation: Relation[V]): QueryResult[V] =
        relation match {
            case col: QueryResult[V] => col
            case _ if relation.isSet => new SetResult (relation)
            case _ if !relation.isSet => new BagResult (relation)
        }
}
