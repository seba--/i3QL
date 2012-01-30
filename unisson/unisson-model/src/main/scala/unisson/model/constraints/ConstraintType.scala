package unisson.model.constraints

import de.tud.cs.st.vespucci.model._

/**
 *
 * Author: Ralf Mitschke
 * Date: 31.12.11
 * Time: 15:39
 *
 */
object ConstraintType extends Enumeration
{
    val Outgoing, Incoming, NotAllowed, GlobalOutgoing, GlobalIncoming, Expected, Violation, IncomingAndOutgoing = Value

    /**
     * projection of the constrinat to its type
     */
    def apply(constraint: IConstraint) = constraint match {
        case _: IGlobalIncoming => GlobalIncoming
        case _: IGlobalOutgoing => GlobalOutgoing
        case _: IIncoming => Incoming
        case _: IOutgoing => Outgoing
        case _: IExpected => Expected
        case _: INotAllowed => NotAllowed
        case _: IDocumentedViolation => Violation
        case _: IInAndOut => IncomingAndOutgoing
    }

}