package sae

/**
 *
 * This observer records all events that are passed to it.
 *
 * @author Ralf Mitschke
 */
class EventRecorder[-V] extends Observer[V]
{
    import EventRecorder._
    private var eventQueue: List[ObserverEvent] = Nil

    def events = eventQueue

    def eventsChronological = eventQueue.reverse

    def clearEvents() {
        eventQueue = Nil
    }

    def updated(oldV: V, newV: V) {
        eventQueue ::= UpdateEvent(oldV, newV)
    }

    def removed(v: V) {
        eventQueue ::= RemoveEvent(v)
    }

    def added(v: V) {
        eventQueue ::= AddEvent(v)
    }
}

object EventRecorder
{
    trait ObserverEvent

    case class AddEvent[T](value: T) extends ObserverEvent

    case class RemoveEvent[T](value: T) extends ObserverEvent

    case class UpdateEvent[T](oldValue: T, newValue: T) extends ObserverEvent
}