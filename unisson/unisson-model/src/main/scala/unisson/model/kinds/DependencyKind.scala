package unisson.model.kinds

/**
 *
 * Author: Ralf Mitschke
 * Created: 12.09.11 09:52
 *
 */

trait DependencyKind
        extends KindExpr
{
    def designator: String

    def asVespucciString = designator

    override def toString = designator

    override def hashCode() = designator.hashCode() * 487;

    override def equals(obj: Any) : Boolean = {
        if( obj == null )
            return false;
        if( !obj.isInstanceOf[DependencyKind] )
            return false;
        obj.asInstanceOf[DependencyKind].designator == this.designator
    }

}