package unisson.model.kinds

/**
 * 
 * Author: Ralf Mitschke
 * Created: 12.09.11 09:52
 *
 */

trait DependencyKind
{
    def designator : String

    def children : Seq[DependencyKind]

    def descendents : Seq[DependencyKind] =
    {
        this.children ++ this.children.flatMap( _.descendents )
    }

    def parent : Option[DependencyKind]

    def ancestors : Seq[DependencyKind] =
    {
        this.parent match {
            case None => Nil
            case Some(p) => List(p) ++ p.ancestors
        }
    }

    def isSubKindOf(kind : DependencyKind) : Boolean =
    {
        this.parent match {
            case None => false
            case Some(p) if(p == kind) => true
            case Some(p) if(p != kind) => p.isSubKindOf(kind)
        }
    }
}