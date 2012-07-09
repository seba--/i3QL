package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import unisson.query.code_model.SourceElement
import sae.collections.{Conversions, QueryResult}
import org.junit.Test
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.vespucci.model.IEnsemble
import de.tud.cs.st.vespucci.interfaces.ICodeElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 02.01.12
 * Time: 16:22
 *
 */
class TestUnissonDatabaseEnsembleElements
        extends ShouldMatchers
{

    import UnissonOrdering._

    @Test
    def testDirectEnsembleElements() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ensembleA = Ensemble("A", "class('test','A')")
        val ensembleB = Ensemble("B", "class('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val result: QueryResult[(IEnsemble, ICodeElement)] = Conversions
                .lazyViewToResult(db.ensemble_elements)

        db.addGlobalModel(GlobalArchitectureModel(ensembles))

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)

        result.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a)),
                (ensembleB, SourceElement(b))
            )
        )

    }


    @Test
    def testChildrenAndDerivedParentEnsembleElements() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class('test','A1')")
        val ensembleA2 = Ensemble("A2", "class('test','A2')")
        val ensembleA = Ensemble("A", "derived", ensembleA1, ensembleA2)
        val ensembleB = Ensemble("B", "class('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val result: QueryResult[(IEnsemble, ICodeElement)] = Conversions
                .lazyViewToResult(db.ensemble_elements)

        db.addGlobalModel(GlobalArchitectureModel(ensembles))

        val a = ObjectType("test/A")
        val a1 = ObjectType("test/A1")
        val a2 = ObjectType("test/A2")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)

        result.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(a2)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2)),
                (ensembleB, SourceElement(b))
            )
        )

    }


    @Test
    def testChildrenAndDirectParentEnsembleElements() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)


        val ensembleA1 = Ensemble("A1", "class('test.a','A1')")
        val ensembleA2 = Ensemble("A2", "class('test.a','A2')")
        val ensembleA = Ensemble("A", "package('test.a')", ensembleA1, ensembleA2)
        val ensembleB = Ensemble("B", "class('test','B')")
        val ensembles = Set(ensembleA, ensembleB)

        val result: QueryResult[(IEnsemble, ICodeElement)] = Conversions
                .lazyViewToResult(db.ensemble_elements)

        db.addGlobalModel(GlobalArchitectureModel(ensembles))

        val a = ObjectType("test.a/A")
        val a1 = ObjectType("test.a/A1")
        val a2 = ObjectType("test.a/A2")
        val b = ObjectType("test/B")
        bc.declared_types.element_added(a)
        bc.declared_types.element_added(b)
        bc.declared_types.element_added(a1)
        bc.declared_types.element_added(a2)

        result.asList.sorted should be(
            List(
                (ensembleA, SourceElement(a)),
                (ensembleA, SourceElement(a1)),
                (ensembleA, SourceElement(a2)),
                (ensembleA1, SourceElement(a1)),
                (ensembleA2, SourceElement(a2)),
                (ensembleB, SourceElement(b))
            )
        )

    }
}