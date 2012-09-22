package sae.capabilities

/**
 * a view that can be queried to supply a singleton value
 */
trait SingletonValue[V]
{
    /**
     * If the view consists of a single value, Some(value) is returned, i.e. the value wrapped in a Option.
     * Otherwise this method returns None.
     * If only one distinct value is contained but in multiple instances None is returned.
     */
    def singletonValue: Option[V]
}