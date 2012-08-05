package sae.syntax.sql

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 05.08.12
 * Time: 13:55
 */
object DISTINCT
{

    def apply[Domain <: AnyRef, Range <: AnyRef](
                                                    projection: Domain => Range
                                                    ): DISTINCT_PROJECTION[Domain, Range] =
        new DISTINCT_PROJECTION[Domain, Range]
        {
            def function = projection
        }

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, Range <: AnyRef](
                                                                        projection: (DomainA, DomainB) => Range
                                                                        ): DISTINCT_PROJECTION[(DomainA, DomainB), Range] =
        new DISTINCT_PROJECTION[(DomainA, DomainB), Range]
        {
            def function = (tuple: (DomainA, DomainB)) => projection(tuple._1, tuple._2)
        }

    def apply[DomainA <: AnyRef, DomainB <: AnyRef, RangeA <: AnyRef, RangeB](
                                                                                 projectionA: DomainA => RangeA,
                                                                                 projectionB: DomainB => RangeB
                                                                                 ): DISTINCT_PROJECTION[(DomainA, DomainB), (RangeA, RangeB)] =
        new DISTINCT_PROJECTION[(DomainA, DomainB), (RangeA, RangeB)]
        {
            def function = (tuple: (DomainA, DomainB)) => (projectionA(tuple._1), projectionB(tuple._2))
        }


    def apply(x: STAR): DISTINCT_NO_PROJECTION.type = DISTINCT_NO_PROJECTION


}
