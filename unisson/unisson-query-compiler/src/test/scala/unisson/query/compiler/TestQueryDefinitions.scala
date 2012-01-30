package unisson.query.compiler

import org.scalatest.matchers.ShouldMatchers
import sae.bytecode.{BytecodeDatabase, Database}
import org.junit.Test
import de.tud.cs.st.bat.ObjectType
import sae.collections.QueryResult
import unisson.query.code_model.SourceElement

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.01.12
 * Time: 10:58
 *
 */
class TestQueryDefinitions
        extends ShouldMatchers
{
    import sae.collections.Conversions._
    import SourceElement.ordering


    @Test
    def testClassQuery(){
        val bc: Database = new BytecodeDatabase()
        val queries = new QueryDefinitions(bc)

        val result:QueryResult[SourceElement[AnyRef]] = queries.`class`("test", "A")

        val a = ObjectType("test/A")
        val b = ObjectType("test/B")
        bc.classfiles.element_added(a)
        bc.classfiles.element_added(b)


        result.asList.sorted should be(
            List(
                SourceElement(a)
            )
        )


    }

}