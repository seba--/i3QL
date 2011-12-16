package unisson.query.compiler

import unisson.query.ast._
import sae.LazyView
import sae.bytecode.Database
import unisson.query.code_model.SourceElement
import sae.collections.EmptyResult
import unisson.query.parser.QueryParser
import java.lang.IllegalArgumentException
import unisson.query.UnissonQuery

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:17
 *
 */
class QueryCompiler(val db: Database)
{

    val definitions = new QueryDefinitions(db)

    def parseAndCompile(query: String): LazyView[SourceElement[AnyRef]] = {
        val parser = new QueryParser()
        val result = parser.parse(query);
        result match {
            case parser.Failure(msg, next) => {
                throw new IllegalArgumentException(msg + "\n" + next.pos.longString)
            }
            case parser.Success(queryAST, _) => compile(queryAST)
        }
    }

    /**
     *
     */
    def compile(query: UnissonQuery): LazyView[SourceElement[AnyRef]] = {
        import definitions._
        query match {
            case ClassSelectionQuery(pn, sn) => `class`(pn, sn)
            case ClassQuery(classQuery) => `class`(compile(classQuery))
            case ClassWithMembersQuery(ClassSelectionQuery(pn, sn)) => class_with_members(pn, sn)
            case ClassWithMembersQuery(classQuery) => class_with_members(compile(classQuery))
            case PackageQuery(pn) => `package`(pn)
            case OrQuery(left, right) => compile(left) or compile(right)
            case WithoutQuery(left, right) => compile(left) without compile(right)
            case TransitiveQuery(SuperTypeQuery(innerQuery)) => transitive_supertype(compile(innerQuery))
            case SuperTypeQuery(innerQuery) => supertype(compile(innerQuery))
            case EmptyQuery() => new EmptyResult[SourceElement[AnyRef]]()
            case _ => throw new IllegalArgumentException("Unknown query type: " + query)
        }
    }

}