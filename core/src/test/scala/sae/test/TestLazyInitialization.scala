package sae.test

import org.scalatest.matchers.ShouldMatchers
import sae.collections.Table
import org.junit.{Assert, Test}
import sae.{IndexedView, MockObserver, Observable}


/**
 *
 * Author: Ralf Mitschke
 * Date: 17.01.12
 * Time: 17:27
 *
 */
class TestLazyInitialization
        extends ShouldMatchers
{

    import sae.syntax.RelationalAlgebraSyntax._

    import sae.MockObserver._

    @Test
    def testDifferenceLazyInitOnLeftAddEvent() {

        val a = new Table[String]
        val b = new Table[String]

        val difference = a ∖ b

        a += "Hello"

        difference.size should be(1)
    }

    @Test
    def testDifferenceLazyInitOnRightAddEvent() {
        val a = new Table[String]
        val b = new Table[String]
        a += "Hello"

        val difference = a ∖ b

        b += "Hello"

        difference.size should be(0)
    }

    @Test
    def testEventsOnDifference() {
        val a = new Table[String]
        val b = new Table[String]
        val difference = a ∖ b

        val o = new MockObserver[String]()
        difference.addObserver(o)

        a += "Hello"

        difference.asList should be(List("Hello"))
        o.events should be(List(AddEvent("Hello")))
    }


    @Test
    def testIndexInitialization() {
        val a = new Table[String]
        import sae.operators.Conversions._
        val indexed : IndexedView[String] = lazyViewToIndexedView(a)

        val index = indexed.index(identity[String])

        a += "Hello"

        a.asList should be(List("Hello"))
        index.asList should be(List(("Hello", "Hello")))

    }

    @Test
    def testIndexInitializationOnDifference() {
        val a = new Table[String]
        val b = new Table[String]
        val difference = a ∖ b
        import sae.operators.Conversions._
        val indexed : IndexedView[String] = lazyViewToIndexedView(difference)

        val index = indexed.index(identity[String])

        a += "Hello"

        difference.asList should be(List("Hello"))
        index.asList should be(List(("Hello", "Hello")))

    }

    @Test
    def testIndexInitializationOnFreshJoin() {
        val a = new Table[String]
        val b = new Table[String]
        val join = ((a, identity[String]_) ⋈ (identity[String]_, b)) { (s1:String,  s2:String) => s1}
        import sae.operators.Conversions._
        val indexed : IndexedView[String] = lazyViewToIndexedView(join)

        val index = indexed.index(identity[String])

        a += "Hello"
        join.asList should be(Nil)
        index.asList should be(Nil)


        b += "Hello"
        join.asList should be(List("Hello"))
        index.asList should be(List(("Hello", "Hello")))
    }

    @Test
    def testIndexInitializationOnExistingJoin() {
        // test that when an element is about to be added and the underlying relation will contain the element,
        // that we do not double add the element
        val a = new Table[String]
        a += "Hello"
        val b = new Table[String]

        val join = ((a, identity[String]_) ⋈ (identity[String]_, b)) { (s1:String,  s2:String) => s1}
        import sae.operators.Conversions._
        val indexed : IndexedView[String] = lazyViewToIndexedView(join)

        val index = indexed.index(identity[String])

        b += "Hello"
        join.asList should be(List("Hello"))
        index.asList should be(List(("Hello", "Hello")))
    }

    @Test
    def testDifferenceInJoin() {
        val a = new Table[String]
        val b = new Table[String]
        val c = new Table[String]
        val difference = a ∖ b

        val o = new MockObserver[String]()
        difference.addObserver(o)

        val join = ((difference, identity(_: String)) ⋈(identity(_: String), c)) {(s1: String, s2: String) => (s1, s2)}

        a += "Hello"
        difference.asList should be(List("Hello"))
        join.asList should be(Nil)


        c += "Hello"
        o.events should be(List(AddEvent("Hello")))
        difference.asList should be(List("Hello"))
        join.asList should be(List(("Hello", "Hello")))
    }

    @Test
    def testDifferenceWithDoubledSource() {
        val source = new Table[String]
        val doubledSource = new Table[String]

        val join = ((source, identity(_: String)) ⋈(identity(_: String), doubledSource)) {(s1: String,
                                                                                           s2: String) =>
            s1
        }

        val firstObserver = doubledSource.observers.head

        var difference = join ∖ doubledSource

        // we try to construct the situation where the right observer is notified first.
        // if this situation can not be constructed anymore ignore the test
        var i = 0
        while (doubledSource.observers.toList(0) == firstObserver) {
            i += 1
            // this should not take more than 10 tries, the test never got stuck here, but we should check anyway
            Assert
                    .assertTrue("could not construct the test prerequisite, that right side is notified first. We made " + i + "attempts", i < 1000)
            difference.clearObserversForChildren(
                (o: Observable[_ <: AnyRef]) => {
                    o != join
                }
            )
            // should be the case anyway through the tests in TestObserverManipulation
            join.observers should have size (0)
            difference = join ∖ doubledSource
        }

        val hello = "Hello"
        source += hello
        source += hello
        doubledSource += hello

        join.asList should be(
            List(hello, hello)
        )
        difference.asList should be(
            List(hello)
        )

        source -= hello
        source -= hello
        doubledSource -= hello

        join.asList should be(Nil)

        difference.asList should be(Nil)

        source += hello
        source += hello
        doubledSource += hello

        join.asList should be(
            List(hello, hello)
        )
        difference.asList should be(
            List(hello)
        )
    }


    @Test
    def testJoinWithLazyInitalization() {
        type Ensemble = String
        type Constraint = (String, String)
        type Context = String

        // simulation of a serious test that went bad with ensembles, which we emulate here by strings
        val local_ensembles = new Table[(Ensemble, Context)]
        val local_incoming = new Table[(Constraint, Context)]

        val ensemblesAndConstraintsInSameContext = (
                (
                        local_ensembles,
                        (_: (Ensemble, Context))._2
                        ) ⋈(
                        (_: (Constraint, Context))._2,
                        local_incoming
                        )
                ) {(e: (Ensemble, Context), c: (Constraint, Context)) => (e._1, c._1, e._2)}

        val ensembleA = ("A", "context")
        val ensembleB = ("B", "context")
        val ensembleC = ("C", "context")

        local_ensembles += ensembleA
        local_ensembles += ensembleB
        local_ensembles += ensembleC

        val constraintB_A = (("B", "A"), "context")
        local_incoming += constraintB_A

        ensemblesAndConstraintsInSameContext.asList.sorted should be(
            List(
                ("A", ("B", "A"), "context"),
                ("B", ("B", "A"), "context"),
                ("C", ("B", "A"), "context")
            )
        )

        local_ensembles -= ensembleA
        local_ensembles -= ensembleB
        local_ensembles -= ensembleC
        local_incoming -= constraintB_A

        ensemblesAndConstraintsInSameContext.asList should be(Nil)
    }

    @Test
    def testAntiSemiJoinAdditionsToDoubledSource() {
        type Ensemble = String
        type Constraint = (String, String)
        type Context = String

        // simulation of a serious test that went bad with ensembles, which we emulate here by strings
        val local_ensembles = new Table[(Ensemble, Context)]
        val local_incoming = new Table[(Constraint, Context)]


        val ensemblesAndConstraintsInSameContext = (
                (
                        local_ensembles,
                        (_: (Ensemble, Context))._2
                        ) ⋈(
                        (_: (Constraint, Context))._2,
                        local_incoming
                        )
                ) {(e: (Ensemble, Context), c: (Constraint, Context)) => (e._1, c._1, e._2)}

        // filter obviously allowed combinations
        // Allowed are all (A, Incoming(_, A) and (A, Incoming(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (Ensemble, Constraint, Context)) =>
            (e._1 != e._2._2 && e._2._1 != e._1)
        }(ensemblesAndConstraintsInSameContext)


        val firstObserver = local_incoming.observers.head

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
         * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
         */

        // there is a specific problem if the right relation of the not exists operator notifies the not exists first
        // hence we construct this relation until we are sure the not exists is notified first

        var disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        (e: (Ensemble, Constraint, Context)) => (e._1, e._2._2, e._3)
                        ) ⊳(
                        (c: (Constraint, Context)) => (c._1._1, c._1._2, c._2),
                        local_incoming
                        )

                )

        // we try to construct the situation where the right observer is notified first.
        // if this situation can not be constructed anymore ignore the test
        var i = 0
        while (local_incoming.observers.toList(0) == firstObserver) {
            i += 1
            // this should not take more than 10 tries, the test never got stuck here, but we should check anyway
            Assert
                    .assertTrue("could not construct the test prerequisite, that right side is notified first. We made " + i + "attempts", i < 10000)
            disallowedEnsemblesPerConstraint.clearObserversForChildren(
                (o: Observable[_ <: AnyRef]) => {
                    o != filteredEnsemblesWithConstraints
                }
            )
            // should be the case anyway through the tests in TestObserverManipulation
            filteredEnsemblesWithConstraints.observers should have size (0)
            disallowedEnsemblesPerConstraint = (
                    (
                            filteredEnsemblesWithConstraints,
                            (e: (Ensemble, Constraint, Context)) => (e._1, e._2._2, e._3)
                            ) ⊳(
                            (c: (Constraint, Context)) => (c._1._1, c._1._2, c._2),
                            local_incoming
                            )

                    )
        }

        val ensembleA = ("A", "context")
        val ensembleB = ("B", "context")
        val ensembleC = ("C", "context")

        local_ensembles += ensembleA
        local_ensembles += ensembleB
        local_ensembles += ensembleC

        val constraintB_A = (("B", "A"), "context")
        local_incoming += constraintB_A

        disallowedEnsemblesPerConstraint.asList should be(
            List(
                ("C", ("B", "A"), "context")
            )
        )

        local_ensembles -= ensembleA
        local_ensembles -= ensembleB
        local_ensembles -= ensembleC
        local_incoming -= constraintB_A

        ensemblesAndConstraintsInSameContext.asList should be(Nil)

        disallowedEnsemblesPerConstraint.asList should be(
            Nil
        )

        local_ensembles += ensembleA
        local_ensembles += ensembleB
        local_ensembles += ensembleC

        val constraintC_A = (("C", "A"), "context")
        local_incoming += constraintC_A

        disallowedEnsemblesPerConstraint.asList should be(
            List(
                ("B", ("C", "A"), "context")
            )
        )

    }

    @Test
    def testAntiSemiJoinAdditionsWithNeighboringDoubledSource() {
        type Ensemble = String
        type Constraint = (String, String)
        type Context = String

        // simulation of a serious test that went bad with ensembles, which we emulate here by strings
        val local_ensembles = new Table[(Ensemble, Context)]
        val local_incoming = new Table[(Constraint, Context)]


        val ensemblesAndConstraintsInSameContext = (
                (
                        local_ensembles,
                        (_: (Ensemble, Context))._2
                        ) ⋈(
                        (_: (Constraint, Context))._2,
                        local_incoming
                        )
                ) {(e: (Ensemble, Context), c: (Constraint, Context)) => (e._1, c._1, e._2)}

        // filter obviously allowed combinations
        // Allowed are all (A, Incoming(_, A) and (A, Incoming(A, _)
        val filteredEnsemblesWithConstraints = σ {(e: (Ensemble, Constraint, Context)) =>
            (e._1 != e._2._2 && e._2._1 != e._1)
        }(ensemblesAndConstraintsInSameContext)


        val firstObserver = local_incoming.observers.head

        /**
         * all disallowed combinations taking all constraints to an ensemble into account
         * for all (Z,Y) where Z,Y in Ensembles and Incoming(_,Y, ctx) ;
         * if !exists (Z,Y) with Incoming(Z,Y, ctx) or GlobalIncoming(Z,Y, ctx) then Z may not use Y
         */

        // there is a specific problem if the right relation of the not exists operator notifies the not exists first
        // hence we construct this relation until we are sure the not exists is notified first

        var disallowedEnsemblesPerConstraint = (
                (
                        filteredEnsemblesWithConstraints,
                        (e: (Ensemble, Constraint, Context)) => (e._1, e._2._2, e._3)
                        ) ⊳(
                        (c: (Constraint, Context)) => (c._1._1, c._1._2, c._2),
                        local_incoming
                        )

                )


        val ensembleA = ("A", "context")
        val ensembleB = ("B", "context")
        val ensembleC = ("C", "context")
        val ensembleD = ("D", "context")

        local_ensembles += ensembleA
        local_ensembles += ensembleB
        local_ensembles += ensembleC

        val constraintB_A = (("B", "A"), "context")
        local_incoming += constraintB_A

        disallowedEnsemblesPerConstraint.asList should be(
            List(
                ("C", ("B", "A"), "context")
            )
        )

        local_ensembles -= ensembleA
        local_ensembles -= ensembleB
        local_ensembles -= ensembleC
        local_incoming -= constraintB_A

        ensemblesAndConstraintsInSameContext.asList should be(Nil)

        disallowedEnsemblesPerConstraint.asList should be(Nil)

        local_ensembles += ensembleA
        local_ensembles += ensembleB
        local_ensembles += ensembleD
        local_incoming += constraintB_A

        disallowedEnsemblesPerConstraint.asList should be(
            List(
                ("D", ("B", "A"), "context")
            )
        )

        local_ensembles -= ensembleA
        local_ensembles -= ensembleB
        local_ensembles -= ensembleD
        local_incoming -= constraintB_A

        ensemblesAndConstraintsInSameContext.asList should be(Nil)

        disallowedEnsemblesPerConstraint.asList should be(Nil)

    }

}