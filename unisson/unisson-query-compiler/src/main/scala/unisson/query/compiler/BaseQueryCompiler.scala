package unisson.query.compiler

import sae.bytecode.BytecodeDatabase
import sae.Relation
import unisson.query.code_model.SourceElementFactory
import unisson.query.parser.QueryParser
import unisson.query.UnissonQuery
import unisson.query.ast._
import sae.collections.EmptyResult
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, SourceElement}

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.07.12
 * Time: 17:05
 *
 */
class BaseQueryCompiler(val db: BytecodeDatabase)
    extends QueryCompiler
{
    val definitions = new QueryDefinitions (db)

    def parseAndCompile(query: String)(implicit decorator: QueryCompiler = this): Relation[ICodeElement] = {
        val parser = new QueryParser ()
        val result = parser.parse (query)
        result match {
            case parser.Failure (msg, next) => {
                throw new IllegalArgumentException (msg + "\n" + next.pos.longString)
            }
            case parser.Success (queryAST, _) => decorator.compile (queryAST)
        }
    }

    /**
     *
     */
    def compile(query: UnissonQuery)(implicit decorator: QueryCompiler = this): Relation[ICodeElement] = {
        import definitions._
        query match {
            case ClassSelectionQuery (pn, sn) => `class` (pn, sn)
            case ClassQuery (classQuery) => `class` (decorator.compile (classQuery))
            case ClassWithMembersQuery (ClassSelectionQuery (pn, sn)) => class_with_members (pn, sn)
            case ClassWithMembersQuery (classQuery) => class_with_members (decorator.compile (classQuery))
            case PackageQuery (pn) => `package` (pn)
            case OrQuery (left, right) => decorator.compile (left) or decorator.compile (right)
            case WithoutQuery (left, right) => decorator.compile (left) without decorator.compile (right)
            case MethodQuery (classQuery, name, returnType, parameters@_*) => method (decorator.compile (classQuery), name, decorator.compile (returnType), parameters.map (decorator.compile): _*)
            case FieldQuery (classQuery, name, fieldType) => field (decorator.compile (classQuery), name, decorator.compile (fieldType))
            case TransitiveQuery (SuperTypeQuery (innerQuery)) => transitive_supertype (decorator.compile (innerQuery))
            case SuperTypeQuery (innerQuery) => supertype (decorator.compile (innerQuery))
            case TypeQuery (name) => typeQuery (name)
            case EmptyQuery () => new EmptyResult[ICodeElement]()
            case DerivedQuery () => new EmptyResult[ICodeElement]()
            case _ => throw new IllegalArgumentException ("Unknown query type: " + query)
        }
    }
}