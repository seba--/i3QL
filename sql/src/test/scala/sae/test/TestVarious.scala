package sae.test

import org.junit.{Assert, Test}
import sae._
import sae.syntax.sql._
import sae.operators.Conversions
import sae.EventRecorder.{AddEvent, RemoveEvent}

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
        val basicBlockEndPcs: Relation[java.lang.Integer] = new BagExtent[java.lang.Integer]
        val immediateBasicBlockSuccessorEdges: Relation[(java.lang.Integer, java.lang.Integer)] = new BagExtent[(java.lang.Integer, java.lang.Integer)]

        val fallThroughCaseSuccessors =
            SELECT ((i: java.lang.Integer) => (i, Integer.valueOf (i + 1))) FROM basicBlockEndPcs WHERE NOT (
                EXISTS (
                    SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: (java.lang.Integer, java.lang.Integer))
                        ._1) === (identity (_: java.lang.Integer))
                )
            )

        val basicBlockSuccessorEdges: Relation[(java.lang.Integer, java.lang.Integer)] = SELECT (*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: Relation[java.lang.Integer] = new BagExtent[java.lang.Integer]

        val basicBlockStartPcs: Relation[java.lang.Integer] =
            SELECT (*) FROM zeroBasicBlockStartPcs UNION_ALL (
                SELECT ((edge: (java.lang.Integer, java.lang.Integer)) => edge._2) FROM basicBlockSuccessorEdges
                )
        val bordersAll: Relation[(java.lang.Integer, java.lang.Integer)] = SELECT ((start: java.lang.Integer,
                                                                                    end: java.lang.Integer) => (start, end)) FROM (basicBlockStartPcs, basicBlockEndPcs)

        val borders: LazyInitializedQueryResult[(java.lang.Integer, java.lang.Integer)] =
            Conversions.lazyViewToMaterializedView (
                SELECT (*) FROM (bordersAll) WHERE ((e: (java.lang.Integer, java.lang.Integer)) => (e._1 < e._2))
            )



        basicBlockEndPcs.element_added (113)
        immediateBasicBlockSuccessorEdges.element_added ((113, 114))
        zeroBasicBlockStartPcs.element_added (0)
        Assert.assertEquals (
            List (
                (0, 113)
            ),
            borders.asList
        )

        basicBlockEndPcs.element_added (110)

        Assert.assertEquals (
            List (
                (0, 110),
                (0, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added (4)

        Assert.assertEquals (
            List (
                (0, 4),
                (0, 110),
                (0, 113),
                (5, 110),
                (5, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        immediateBasicBlockSuccessorEdges.element_added ((4, 111))

        Assert.assertEquals (
            List (
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        val o = new EventRecorder[(java.lang.Integer, java.lang.Integer)]
        borders.addObserver (o)

        basicBlockEndPcs.element_added (75)
        Assert.assertEquals (
            List (
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

        immediateBasicBlockSuccessorEdges.element_added ((75, 111))
        Assert.assertEquals (
            List (
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
        val basicBlockEndPcs: Relation[(String, Int)] = new BagExtent[(String, Int)]
        val immediateBasicBlockSuccessorEdges: Relation[(String, Int, Int)] = new BagExtent[(String, Int, Int)]

        val fallThroughCaseSuccessors =
            SELECT ((e: (String, Int)) => (e._1, e._2, (e._2 + 1))) FROM basicBlockEndPcs WHERE NOT (
                EXISTS (
                    SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE (
                        (_: (String, Int, Int))._1) === ((_: (String, Int))._1) AND (
                        (_: (String, Int, Int))._2) === ((_: (String, Int))._2)
                )
            )

        val basicBlockSuccessorEdges: Relation[(String, Int, Int)] = SELECT (*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: Relation[(String, Int)] = new BagExtent[(String, Int)]

        val basicBlockStartPcs: Relation[(String, Int)] =
            SELECT (*) FROM zeroBasicBlockStartPcs UNION_ALL (
                SELECT ((edge: (String, Int, Int)) => (edge._1, edge._3)) FROM basicBlockSuccessorEdges
                )
        val bordersAll: Relation[(Int, Int)] = SELECT ((start: (String, Int), end: (String, Int)) => (start._2, end
            ._2)) FROM (basicBlockStartPcs, basicBlockEndPcs) WHERE ((_: (String, Int))._1) === ((_: (String, Int))
            ._1)

        val borders: LazyInitializedQueryResult[(Int, Int)] =
            Conversions.lazyViewToMaterializedView (
                SELECT (*) FROM (bordersAll) WHERE ((e: (Int, Int)) => (e._1 < e._2))
            )


        basicBlockEndPcs.element_added (("nameForToken", 113))
        immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 113, 114))
        zeroBasicBlockStartPcs.element_added (("nameForToken", 0))
        Assert.assertEquals (
            List (
                (0, 113)
            ),
            borders.asList
        )

        basicBlockEndPcs.element_added (("nameForToken", 110))

        Assert.assertEquals (
            List (
                (0, 110),
                (0, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added (("nameForToken", 4))

        Assert.assertEquals (
            List (
                (0, 4),
                (0, 110),
                (0, 113),
                (5, 110),
                (5, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 4, 111))

        Assert.assertEquals (
            List (
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        val o = new EventRecorder[(Int, Int)]
        borders.addObserver (o)

        val m = new EventRecorder[(String, Int, Int)]
        basicBlockSuccessorEdges.addObserver (m)

        immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 75, 111))
        Assert.assertEquals (
            List (
                (0, 4),
                (0, 110),
                (0, 113),
                (111, 113),
                (111, 113),
                (111, 113)
            ),
            borders.asList.sorted
        )

        basicBlockEndPcs.element_added (("nameForToken", 75))

        m.events.foreach (println)
        o.events.foreach (println)
        Assert.assertEquals (
            List (
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
        val basicBlockEndPcs: Relation[java.lang.Integer] = new BagExtent[java.lang.Integer]
        val immediateBasicBlockSuccessorEdges: Relation[(java.lang.Integer, java.lang.Integer)] = new BagExtent[(java.lang.Integer, java.lang.Integer)]

        val fallThroughCaseSuccessors =
            SELECT ((i: java.lang.Integer) => (i, Integer.valueOf (i + 1))) FROM basicBlockEndPcs WHERE NOT (
                EXISTS (
                    SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE ((_: (java.lang.Integer, java.lang.Integer))
                        ._1) === (identity (_: java.lang.Integer))
                )
            )

        val basicBlockSuccessorEdges: Relation[(java.lang.Integer, java.lang.Integer)] = SELECT (*) FROM immediateBasicBlockSuccessorEdges UNION_ALL (fallThroughCaseSuccessors)

        val zeroBasicBlockStartPcs: Relation[java.lang.Integer] = new BagExtent[java.lang.Integer]

        val basicBlockStartPcs: Relation[java.lang.Integer] =
            SELECT (*) FROM zeroBasicBlockStartPcs UNION_ALL (
                SELECT ((edge: (java.lang.Integer, java.lang.Integer)) => edge._2) FROM basicBlockSuccessorEdges
                )
        val bordersAll: Relation[(java.lang.Integer, java.lang.Integer)] = SELECT ((start: java.lang.Integer,
                                                                                    end: java.lang.Integer) => (start, end)) FROM (basicBlockStartPcs, basicBlockEndPcs)

        val borders: Relation[(java.lang.Integer, java.lang.Integer)] =
            SELECT (*) FROM (bordersAll) WHERE ((e: (java.lang.Integer, java.lang.Integer)) => (e._1 < e._2))



        basicBlockEndPcs.element_added (113)
        immediateBasicBlockSuccessorEdges.element_added ((113, 114))
        zeroBasicBlockStartPcs.element_added (0)
        Assert.assertEquals (
            List (
                (0, 113)
            ),
            borders.asList
        )

    }


    /**
     * This stems from a crazy situation in the basic block computation
     */
    @Test
    def basicBlockBugTestSimple() {
        var i = 0
        while (i < 10)
        {
            import sae.syntax.RelationalAlgebraSyntax._

            val basicBlockEndPcs: Relation[(String, Int)] = new BagExtent[(String, Int)]
            val immediateBasicBlockSuccessorEdges: Relation[(String, Int, Int)] = new BagExtent[(String, Int, Int)]


           val fallThroughCaseSuccessors  =
               SELECT ((e: (String, Int)) => (e._1, e._2, (e._2 + 1))) FROM basicBlockEndPcs WHERE NOT (
                   EXISTS (
                       SELECT (*) FROM immediateBasicBlockSuccessorEdges WHERE (
                           (_: (String, Int, Int))._1) === ((_: (String, Int))._1) AND (
                           (_: (String, Int, Int))._2) === ((_: (String, Int))._2)
                   )
               )

/*
            val keyProjection = δ (Π ((e: (String, Int, Int)) => (e._1, e._2))(immediateBasicBlockSuccessorEdges))

            val join = (
                (
                    basicBlockEndPcs,
                    identity (_: (String, Int))
                    ) ⋈ (
                    identity (_: (String, Int)),
                    keyProjection
                    )
                )
            {
                (left: (String, Int), right: (String, Int)) => left
            }

            //val fallThroughCaseSuccessorsNegation = (basicBlockEndPcs ∖ join)

            val fallThroughCaseSuccessorsNegation =
                (
                    (
                        basicBlockEndPcs,
                        identity (_: (String, Int))
                        ) ⊳ (
                        (e: (String, Int, Int)) => (e._1, e._2),
                        immediateBasicBlockSuccessorEdges
                        )
                    )

            //val fallThroughCaseSuccessors = Π ((e: (String, Int)) => (e._1, e._2, e._2 + 1))(fallThroughCaseSuccessorsNegation)
*/
            val basicBlockSuccessorEdges: Relation[(String, Int, Int)] = immediateBasicBlockSuccessorEdges ∪ (fallThroughCaseSuccessors)


            val m = new EventRecorder[AnyRef]
            basicBlockEndPcs.addObserver (m)
            //immediateBasicBlockSuccessorEdges.addObserver(m)
            //keyProjection.addObserver(m)
            //join.addObserver(m)
            //fallThroughCaseSuccessorsNegation.addObserver(m)
            //fallThroughCaseSuccessors.addObserver(m)
            basicBlockSuccessorEdges.addObserver (m)

            immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 4, 111))
            basicBlockEndPcs.element_added (("nameForToken", 4))

/*
            immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 75, 111))
            basicBlockEndPcs.element_added (("nameForToken", 75))

            immediateBasicBlockSuccessorEdges.element_added (("nameForToken", 81, 111))
            basicBlockEndPcs.element_added (("nameForToken", 81))
*/


            //m.eventsChronological.foreach (println)

            //println ("----------------------------------")

            println(i)

            if (m.events.contains (RemoveEvent ("nameForToken", 4, 5))) {
                //Assert.assertTrue (m.events.contains (AddEvent ("nameForToken", 4, 5)))
            }

            /*
            if (m.events.contains (RemoveEvent ("nameForToken", 75, 76))) {
                Assert.assertTrue (m.events.contains (AddEvent ("nameForToken", 75, 76)))
            }

            if (m.events.contains (RemoveEvent ("nameForToken", 81, 82))) {
                Assert.assertTrue (m.events.contains (AddEvent ("nameForToken", 81, 82)))
            }
            */
            i += 1
        }

    }


    def observerChain[T <: AnyRef](o: Observable[T]): Seq[Observer[_ <: AnyRef]] = {

        var indexObservers: Seq[Observer[_ <: AnyRef]] = Seq ()
        if (o.isInstanceOf[IndexedViewOLD[_ <: AnyRef]]) {
            val indexed = o.asInstanceOf[IndexedViewOLD[_ <: AnyRef]]
            indexObservers =
                (for (index <- indexed.indices) yield
                {
                    Seq (index._2.asInstanceOf[Observer[_ <: AnyRef]]) ++ observerChain (index._2)
                }).flatten.toSeq
        }

        if (o.isInstanceOf[Observable[_ <: AnyRef]])
        {
            val observers: Seq[Observer[_ <: AnyRef]] =
                (for (x <- o.observers; if x.isInstanceOf[Observable[_ <: AnyRef]]) yield {
                    Seq (x) ++ observerChain (x.asInstanceOf[Observable[_ <: AnyRef]])
                }).flatten.toSeq
            indexObservers ++ observers
        }
        else
        {
            indexObservers
        }
    }
}