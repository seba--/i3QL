package unisson

import sae.collections.QueryResult
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax._
import de.tud.cs.st.bat.ObjectType
import sae.bytecode.model.{Method, Field}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Flashcards_0_2_7_Test
{

    def create_queries = new Queries(new BytecodeDatabase())

    def create_ui_layer(queries : Queries): QueryResult[SourceElement[_]] =
        queries.`package`("de.tud.cs.se.flashcards.ui")

    def create_main (queries : Queries): QueryResult[SourceElement[_]] =
        queries.class_with_members("de.tud.cs.se.flashcards.Main")

    def create_learning_strategies (queries : Queries): QueryResult[SourceElement[_]] =
        queries.`package`("de.tud.cs.se.flashcards.model.learning")


    @Test
    def count_ensemble_elements()
    {
        val queries = this.create_queries;
        val ui_layer = create_ui_layer(queries)
        val learning_strategies = create_learning_strategies(queries)
        val main = create_main(queries)

        queries.db.addArchiveAsResource("flashcards-0.2.7.jar")

        val sortedUILayer = ui_layer.asList.sortBy{
            case SourceElement( ObjectType(name) ) => "1" + name
            case SourceElement( Field(ObjectType(cls), name, _) ) => "2" + cls + name
            case SourceElement( Method(ObjectType(cls), name, _, _) ) => "3" + cls + name
        }

        assertEquals(5, main.size)

        assertEquals(190, ui_layer.size)

        assertEquals(124, learning_strategies.size)

    }

}