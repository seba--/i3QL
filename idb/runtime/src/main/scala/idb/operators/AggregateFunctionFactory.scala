package idb.operators

/**
 * IMPORTANT: clients should NOT implement this interface
 * clients should implement:
 * -NotSelfMaintainableAggregateFunctionFactory
 * -SelfMaintainableAggregateFunctionFactory
 */
sealed trait AggregateFunctionFactory[Domain, AggregationValue, +AggregateFunctionType <: AggregateFunction[Domain, AggregationValue]] {
    def apply(): AggregateFunctionType
}

object AggregateFunctionFactory {

	def create[Domain, AggregateValue](
        start : AggregateValue,
        added : (Domain, AggregateValue, Seq[Domain]) => AggregateValue,
        removed : (Domain, AggregateValue, Seq[Domain]) => AggregateValue,
        updated : (Domain, Domain, AggregateValue, Seq[Domain]) => AggregateValue
    ) = {
        new NotSelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] {
            override def apply() : NotSelfMaintainableAggregateFunction[Domain, AggregateValue] = {
                new NotSelfMaintainableAggregateFunction[Domain, AggregateValue] {
                    private var aggregate : AggregateValue = start

                    def add(newD: Domain, data : Seq[Domain]): AggregateValue = {
                        val a = added(newD, aggregate, data)
                        aggregate = a
                        a
                    }

                    def remove(newD: Domain, data : Seq[Domain]): AggregateValue = {
                        val a = removed(newD, aggregate, data)
                        aggregate = a
                        a
                    }

                    def update(oldD: Domain, newD: Domain, data : Seq[Domain]): AggregateValue = {
                        val a = updated(oldD, newD, aggregate, data)
                        aggregate = a
                        a
                    }

                    def get : AggregateValue =
                        aggregate
                }
            }
        }
    }

	def create[Domain, AggregateValue](
		start : AggregateValue,
		added : (Domain, AggregateValue) => AggregateValue,
		removed : (Domain, AggregateValue) => AggregateValue,
		updated : (Domain, Domain, AggregateValue) => AggregateValue
	) = {
		new SelfMaintainableAggregateFunctionFactory[Domain,AggregateValue] {
			override def apply() : SelfMaintainableAggregateFunction[Domain, AggregateValue] = {
				new SelfMaintainableAggregateFunction[Domain, AggregateValue] {
					private var aggregate : AggregateValue = start

					def add(newD: Domain): AggregateValue = {
						val a = added(newD, aggregate)
						aggregate = a
						a
					}

					def remove(newD: Domain): AggregateValue = {
						val a = removed(newD, aggregate)
						aggregate = a
						a
					}

					def update(oldD: Domain, newD: Domain): AggregateValue = {
						val a = updated(oldD, newD, aggregate)
						aggregate = a
						a
					}

					def get : AggregateValue =
						aggregate
				}
			}
		}
	}
}

/**
 * Factory interface for a not self maintainable aggregation function
 */
trait NotSelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]
    extends AggregateFunctionFactory[Domain, AggregateValue, NotSelfMaintainableAggregateFunction[Domain, AggregateValue]] {

}

/**
 * Factory interface for a self maintainable aggregation function
 */
trait SelfMaintainableAggregateFunctionFactory[Domain, AggregateValue]
    extends AggregateFunctionFactory[Domain, AggregateValue, SelfMaintainableAggregateFunction[Domain, AggregateValue]] {

}
