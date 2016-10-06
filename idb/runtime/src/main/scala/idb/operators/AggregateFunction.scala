package idb.operators

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 * -NotSelfMaintainableAggregateFunction
 * -SelfMaintainableAggregateFunction
 */
trait AggregateFunction[Domain, Result]
{
    def add(newD: Domain, data: Seq[Domain]): Result

    def remove(newD: Domain, data: Seq[Domain]): Result

    def update(oldD: Domain, newD: Domain, data: Seq[Domain]): Result

	def get : Result
}


/**
 * Interface for a not self maintainable aggregate function like min
 */
trait NotSelfMaintainableAggregateFunction[Domain, Result]
    extends AggregateFunction[Domain, Result]
{

}

/**
 * Interface for a self maintainable aggregate function like count
 */
trait SelfMaintainableAggregateFunction[Domain, Result]
    extends AggregateFunction[Domain, Result]
{

    final override def add(newD: Domain, data: Seq[Domain]): Result = {
        add (newD)
    }

    final override def remove(newD: Domain, data: Seq[Domain]): Result = {
        remove (newD)
    }

    final override def update(oldD: Domain, newD: Domain, data: Seq[Domain]): Result = {
        update (oldD, newD)
    }

    def add(newD: Domain): Result

    def remove(newD: Domain): Result

    def update(oldD: Domain, newD: Domain): Result

}


