package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import de.tud.cs.st.bat.resolved.ObjectType
import unisson.query.code_model.SourceElement
import org.junit.{Assert, Test}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.FieldDeclaration

/**
 *
 * Author: Ralf Mitschke
 * Date: 18.01.12
 * Time: 12:05
 *
 */
class TestUnissonDatabaseUpdates
    extends ShouldMatchers
{

    import UnissonOrdering._

    @Test
    def testGlobalModelQueryChange() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleCV0 = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleCV0)

        val constraint = IncomingConstraint ("field_type", ensembleB, ensembleA)

        val globalModelV0 = Repository (ensembles)
        val model = Concern (ensembles, Set (constraint), "test")

        val result = sae.relationToResult (db.violations)

        db.addSlice (model)
        db.setRepository (globalModelV0)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        val e = ObjectType ("test/E")
        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration (d, "fieldInD", a)

        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.typeDeclarations.element_added (d)
        bc.fieldDeclarations.element_added (fieldRefDToA)
        bc.typeDeclarations.element_added (e)

        Assert.assertEquals (
            List (
                Violation (
                    constraint,
                    ensembleCV0,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            ),
            result.asList.sorted
        )

        val EnsembleCV1 = Ensemble ("C", "class_with_members('test','D')")
        val globalModelV1 = Repository (
            ensembleA, ensembleB, EnsembleCV1
        )

        db.updateRepository (globalModelV0, globalModelV1)

        Assert.assertEquals (List (
            Violation (
                constraint,
                EnsembleCV1,
                ensembleA,
                SourceElement (fieldRefDToA),
                SourceElement (a),
                "field_type",
                "test"
            )
        ),
            result.asList.sorted
        )

        val EnsembleCV2 = Ensemble ("C", "class_with_members('test','E')")
        val globalModelV2 = Repository (
            ensembleA, ensembleB, EnsembleCV2
        )

        db.updateRepository (globalModelV1, globalModelV2)

        Assert.assertEquals (Nil, result.asList.sorted)
    }

    @Test
    def testGlobalModelEnsembleChange() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")

        val constraint = GlobalIncomingConstraint ("field_type", ensembleB, ensembleA)

        val globalModelV0 = Repository (Set (ensembleA, ensembleB, ensembleC))
        val model = Concern (Set (ensembleA, ensembleB), Set (constraint), "test")

        val result = sae.relationToResult (db.violations)

        db.addSlice (model)
        db.setRepository (globalModelV0)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration (d, "fieldInD", a)

        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.typeDeclarations.element_added (d)
        bc.fieldDeclarations.element_added (fieldRefDToA)

        result.asList.sorted should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

        val globalModelV1 = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD))

        db.updateRepository (globalModelV0, globalModelV1)

        result.asList.sorted should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                ),
                Violation (
                    constraint,
                    ensembleD,
                    ensembleA,
                    SourceElement (fieldRefDToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

        val globalModelV2 = Repository (Set (ensembleA, ensembleB))

        db.updateRepository (globalModelV1, globalModelV2)

        result.asList.sorted should be (Nil)
    }


    @Test
    def testModelConstraintChange() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembles = Set (ensembleA, ensembleB, ensembleC)

        val constraintV0 = IncomingConstraint ("field_type", ensembleB, ensembleA)

        val globalModel = Repository (ensembles)
        val modelV0 = Concern (ensembles, Set (constraintV0), "test")

        val result = sae.relationToResult (db.violations)

        db.addSlice (modelV0)
        db.setRepository (globalModel)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)

        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)


        Assert.assertEquals (
            List ((constraintV0, "test")),
            db.slice_constraints.asList
        )

        Assert.assertEquals (
            List (
                Violation (
                    constraintV0,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            ),
            result.asList.sorted
        )

        val constraintV1 = IncomingConstraint ("field_type", ensembleC, ensembleA)
        val modelV1 = Concern (ensembles, Set (constraintV1), "test")

        db.updateSlice (modelV0, modelV1)

        Assert.assertEquals (
            List ((constraintV1, "test")),
            db.slice_constraints.asList
        )


        Assert.assertEquals (
            List (
                Violation (
                    constraintV1,
                    ensembleB,
                    ensembleA,
                    SourceElement (fieldRefBToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            ),
            result.asList.sorted
        )

        val modelV2 = Concern (ensembles, Set (), "test")

        db.updateSlice (modelV1, modelV2)

        Assert.assertEquals (
            Nil,
            db.slice_constraints.asList
        )


        Assert.assertEquals (
            Nil,
            result.asList.sorted
        )

    }


    @Test
    def testModelEnsembleChange() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ensembleA = Ensemble ("A", "class_with_members('test','A')")
        val ensembleB = Ensemble ("B", "class_with_members('test','B')")
        val ensembleC = Ensemble ("C", "class_with_members('test','C')")
        val ensembleD = Ensemble ("D", "class_with_members('test','D')")
        val constraint = IncomingConstraint ("field_type", ensembleB, ensembleA)

        val globalModel = Repository (Set (ensembleA, ensembleB, ensembleC, ensembleD))
        val modelV0 = Concern (Set (ensembleA, ensembleB, ensembleC), Set (constraint), "test")

        val result = sae.relationToResult (db.violations)

        db.addSlice (modelV0)
        db.setRepository (globalModel)

        val a = ObjectType ("test/A")
        val b = ObjectType ("test/B")
        val c = ObjectType ("test/C")
        val d = ObjectType ("test/D")
        val fieldRefBToA = FieldDeclaration (b, "fieldInB", a)
        val fieldRefCToA = FieldDeclaration (c, "fieldInC", a)
        val fieldRefDToA = FieldDeclaration (d, "fieldInD", a)

        bc.typeDeclarations.element_added (a)
        bc.typeDeclarations.element_added (b)
        bc.fieldDeclarations.element_added (fieldRefBToA)
        bc.typeDeclarations.element_added (c)
        bc.fieldDeclarations.element_added (fieldRefCToA)
        bc.typeDeclarations.element_added (d)
        bc.fieldDeclarations.element_added (fieldRefDToA)


        result.asList.sorted should be (
            List (
                Violation (
                    constraint,
                    ensembleC,
                    ensembleA,
                    SourceElement (fieldRefCToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

        val modelV1 = Concern (Set (ensembleA, ensembleB, ensembleD), Set (constraint), "test")

        db.updateSlice (modelV0, modelV1)

        result.asList.sorted should be (
            List (
                Violation (
                    constraint,
                    ensembleD,
                    ensembleA,
                    SourceElement (fieldRefDToA),
                    SourceElement (a),
                    "field_type",
                    "test"
                )
            )
        )

        val modelV2 = Concern (Set (ensembleA, ensembleB), Set (constraint), "test")

        db.updateSlice (modelV1, modelV2)

        result.asList.sorted should be (Nil)

    }

}