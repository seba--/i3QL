package sae.test

import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.bytecode.model.dependencies.inner_class
import sae.collections.QueryResult
import de.tud.cs.st.bat.ObjectType
import sae.syntax.RelationalAlgebraSyntax._

/**
 *
 * Author: Ralf Mitschke
 * Created: 08.06.11 14:52
 *
 */
class TestInnerClassesCode
{

    @Test
    def testInnerClassRelation()
    {
        val db = new BytecodeDatabase

        val inner_classes: QueryResult[inner_class] = db.inner_classes

        val classes = List(
            "sae/test/code/innerclass/MyRootClass$1.class",
            "sae/test/code/innerclass/MyRootClass$1MyInnerPrinter.class",
            "sae/test/code/innerclass/MyRootClass$2.class",
            "sae/test/code/innerclass/MyRootClass$InnerPrinterOfX.class",
            "sae/test/code/innerclass/MyRootClass$1$InnerPrinterOfAnonymousClass.class",
            "sae/test/code/innerclass/MyRootClass$InnerPrinterOfX$InnerPrettyPrinter.class",
            "sae/test/code/innerclass/MyRootClass.class"
        )

        val t = db.transformerForClassfileResources(classes)

        t.processAllFacts()

        val result = inner_classes.asList

        val expected = List(
            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass$InnerPrinterOfX"),
                ObjectType("sae/test/code/innerclass/MyRootClass$InnerPrinterOfX$InnerPrettyPrinter"),
                true,
                Some("InnerPrettyPrinter")
            ),
            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass"),
                ObjectType("sae/test/code/innerclass/MyRootClass$InnerPrinterOfX"),
                true,
                Some("InnerPrinterOfX")
            ),
            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass"),
                ObjectType("sae/test/code/innerclass/MyRootClass$1"),
                false,
                None
            ),
            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass$1"),
                ObjectType("sae/test/code/innerclass/MyRootClass$1$InnerPrinterOfAnonymousClass"),
                true,
                Some("InnerPrinterOfAnonymousClass")
            ),

            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass"),
                ObjectType("sae/test/code/innerclass/MyRootClass$2"),
                false,
                None
            ),
            inner_class(
                ObjectType("sae/test/code/innerclass/MyRootClass"),
                ObjectType("sae/test/code/innerclass/MyRootClass$1MyInnerPrinter"),
                false,
                Some("MyInnerPrinter")
            )
        )
        assertEquals(expected, result)
    }
}