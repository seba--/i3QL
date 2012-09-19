package sae.test

import org.junit.{Assert, Test}
import sae._
import sae.syntax.sql._
import sae.operators.Conversions

/**
 *
 * Author: Ralf Mitschke
 * Date: 19.09.12
 * Time: 15:22
 *
 */
class TestVarious
{

    /**
     * This stems from a crazy situation in the basic block computation
     */
    @Test
    def basicBlockBugTestWithoutIndex() {
        val basicBlockEndPcs: LazyView[java.lang.Integer] = new DefaultLazyView[java.lang.Integer]
        val immediateBasicBlockSuccessorEdges: LazyView[(java.lang.Integer, java.lang.Integer)] = new DefaultLazyView[(java.lang.Integer, java.lang.Integer)]

        val fallThroughCaseSuccessors =
            SELECT((i: java.lang.Integer) => (i, Integer.valueOf(i + 1))) FROM basicBlockEndPcs WHERE NOT(
                EXISTS(
                    SELECT(*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: (java.lang.Integer, java.lang.Integer))
                            ._1) === (identity(_: java.lang.Integer))
                )
            )

        val basicBlockSuccessorEdges: LazyView[(java.lang.Integer, java.lang.Integer)] = SELECT(*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: LazyView[java.lang.Integer] = new DefaultLazyView[java.lang.Integer]

        val basicBlockStartPcs: LazyView[java.lang.Integer] =
            SELECT(*) FROM zeroBasicBlockStartPcs UNION_ALL (
                    SELECT((edge: (java.lang.Integer, java.lang.Integer)) => edge._2) FROM basicBlockSuccessorEdges
                    )
        val bordersAll: LazyView[(java.lang.Integer, java.lang.Integer)] = SELECT((start: java.lang.Integer,
                                                                                   end: java.lang.Integer) => (start, end)) FROM(basicBlockStartPcs, basicBlockEndPcs)

        val borders: MaterializedView[(java.lang.Integer, java.lang.Integer)] =
            Conversions.lazyViewToMaterializedView(
                SELECT(*) FROM (bordersAll) WHERE ((e: (java.lang.Integer, java.lang.Integer)) => (e._1 < e._2))
            )



        basicBlockEndPcs.element_added(113)
        immediateBasicBlockSuccessorEdges.element_added((113, 114))
        zeroBasicBlockStartPcs.element_added(0)
        Assert.assertEquals(
            List(
                (0, 113)
            ),
            borders.asList
        )

        basicBlockEndPcs.element_added(110)

        Assert.assertEquals(
            List(
                (0, 110),
                (0, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added(4)

        Assert.assertEquals(
            List(
                (0, 4),
                (0, 110),
                (0, 113),
                (5, 110),
                (5, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        immediateBasicBlockSuccessorEdges.element_added((4, 111))

        Assert.assertEquals(
            List(
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        val o = new MockObserver[(java.lang.Integer, java.lang.Integer)]
        borders.addObserver(o)

        basicBlockEndPcs.element_added(75)
        Assert.assertEquals(
            List(
                (0, 4),
                (0, 75),
                (0, 110),
                (0, 113),
                (76, 110),
                (76, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        immediateBasicBlockSuccessorEdges.element_added((75, 111))
        Assert.assertEquals(
            List(
                (0, 4),
                (0, 75),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

    }

    /**
     * This stems from a crazy situation in the basic block computation
     */
    @Test
    def basicBlockBugTestWithIndexOnBordersAll() {
        val basicBlockEndPcs: LazyView[(String, Int)] = new DefaultLazyView[(String, Int)]
        val immediateBasicBlockSuccessorEdges: LazyView[(String, Int, Int)] = new DefaultLazyView[(String, Int, Int)]

        val fallThroughCaseSuccessors =
            SELECT((e: (String, Int)) => (e._1, e._2, (e._2 + 1))) FROM basicBlockEndPcs WHERE NOT(
                EXISTS(
                    SELECT(*) FROM immediateBasicBlockSuccessorEdges WHERE (
                            (_: (String, Int, Int))._1) === ((_: (String, Int))._1) AND (
                            (_: (String, Int, Int))._2) === ((_: (String, Int))._2)
                )
            )

        val basicBlockSuccessorEdges: LazyView[(String, Int, Int)] = SELECT(*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: LazyView[(String, Int)] = new DefaultLazyView[(String, Int)]

        val basicBlockStartPcs: LazyView[(String, Int)] =
            SELECT(*) FROM zeroBasicBlockStartPcs UNION_ALL (
                    SELECT((edge: (String, Int, Int)) => (edge._1, edge._3)) FROM basicBlockSuccessorEdges
                    )
        val bordersAll: LazyView[(Int, Int)] = SELECT((start: (String, Int), end: (String, Int)) => (start._2, end
                ._2)) FROM(basicBlockStartPcs, basicBlockEndPcs) WHERE ((_: (String, Int))._1) === ((_: (String, Int))
                ._1)

        val borders: MaterializedView[(Int, Int)] =
            Conversions.lazyViewToMaterializedView(
                SELECT(*) FROM (bordersAll) WHERE ((e: (Int, Int)) => (e._1 < e._2))
            )


        basicBlockEndPcs.element_added(("nameForToken", 113))
        immediateBasicBlockSuccessorEdges.element_added(("nameForToken", 113, 114))
        zeroBasicBlockStartPcs.element_added(("nameForToken", 0))
        Assert.assertEquals(
            List(
                (0, 113)
            ),
            borders.asList
        )

        basicBlockEndPcs.element_added(("nameForToken", 110))

        Assert.assertEquals(
            List(
                (0, 110),
                (0, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added(("nameForToken", 4))

        Assert.assertEquals(
            List(
                (0, 4),
                (0, 110),
                (0, 113),
                (5, 110),
                (5, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        immediateBasicBlockSuccessorEdges.element_added(("nameForToken", 4, 111))

        Assert.assertEquals(
            List(
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        val o = new MockObserver[(Int, Int)]
        borders.addObserver(o)

        val m = new MockObserver[(String, Int, Int)]
        basicBlockSuccessorEdges.addObserver(m)

        immediateBasicBlockSuccessorEdges.element_added(("nameForToken", 75, 111))
        Assert.assertEquals(
            List(
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added(("nameForToken", 75))

        m.events.foreach(println)
        o.events.foreach(println)
        Assert.assertEquals(
            List(
                (0, 4),
                (0, 75),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )



    }


    /**
     * This stems from a crazy situation in the basic block computation
     */
    @Test
    def basicBlockBugWithLazyInit() {
        val basicBlockEndPcs: LazyView[java.lang.Integer] = new DefaultLazyView[java.lang.Integer]
        val immediateBasicBlockSuccessorEdges: LazyView[(java.lang.Integer, java.lang.Integer)] = new DefaultLazyView[(java.lang.Integer, java.lang.Integer)]

        val fallThroughCaseSuccessors =
            SELECT((i: java.lang.Integer) => (i, Integer.valueOf(i + 1))) FROM basicBlockEndPcs WHERE NOT(
                EXISTS(
                    SELECT(*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: (java.lang.Integer, java.lang.Integer))
                            ._1) === (identity(_: java.lang.Integer))
                )
            )

        val basicBlockSuccessorEdges: LazyView[(java.lang.Integer, java.lang.Integer)] = SELECT(*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: LazyView[java.lang.Integer] = new DefaultLazyView[java.lang.Integer]

        val basicBlockStartPcs: LazyView[java.lang.Integer] =
            SELECT(*) FROM zeroBasicBlockStartPcs UNION_ALL (
                    SELECT((edge: (java.lang.Integer, java.lang.Integer)) => edge._2) FROM basicBlockSuccessorEdges
                    )
        val bordersAll: LazyView[(java.lang.Integer, java.lang.Integer)] = SELECT((start: java.lang.Integer,
                                                                                   end: java.lang.Integer) => (start, end)) FROM(basicBlockStartPcs, basicBlockEndPcs)

        val borders: LazyView[(java.lang.Integer, java.lang.Integer)] =
            SELECT(*) FROM (bordersAll) WHERE ((e: (java.lang.Integer, java.lang.Integer)) => (e._1 < e._2))



        basicBlockEndPcs.element_added(113)
        immediateBasicBlockSuccessorEdges.element_added((113, 114))
        zeroBasicBlockStartPcs.element_added(0)
        Assert.assertEquals(
            List(
                (0, 113)
            ),
            borders.asList
        )

    }
}