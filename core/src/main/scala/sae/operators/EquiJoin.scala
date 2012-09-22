package sae
package operators


/**
 * A join ....
 */
trait EquiJoin[DomainA, DomainB, Range, Key]
    extends Relation[Range]
{

    def left: Relation[DomainA]

    def right: Relation[DomainB]

    def leftKey: DomainA => Key

    def rightKey: DomainB => Key

    def projection: (DomainA, DomainB) => Range

    def isSet = left.isSet && right.isSet
}

