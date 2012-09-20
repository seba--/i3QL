package unisson.query.compiler

import sae.bytecode.Database
import sae.Relation
import unisson.query.code_model.SourceElement
import unisson.query.parser.QueryParser
import unisson.query.UnissonQuery
import unisson.query.ast._
import sae.collections.EmptyResult
import unisson.query.ast.ClassSelectionQuery
import unisson.query.ast.OrQuery
import unisson.query.ast.ClassQuery
import unisson.query.ast.TransitiveQuery
import unisson.query.ast.FieldQuery
import unisson.query.ast.WithoutQuery
import unisson.query.ast.EmptyQuery
import unisson.query.ast.MethodQuery
import unisson.query.ast.TypeQuery
import unisson.query.ast.PackageQuery
import unisson.query.ast.SuperTypeQuery
import unisson.query.ast.ClassWithMembersQuery

/**
 *
 * Author: Ralf Mitschke
 * Date: 09.07.12
 * Time: 17:05
 *
 */
class BaseQueryCompiler(val db: Database)
    extends QueryCompiler
{
    val definitions = new QueryDefinitions(db)

    def parseAndCompile(query: String)(implicit decorator: QueryCompiler = this): Relation[SourceElement[AnyRef]] = {
        val parser = new QueryParser()
        val result = parser.parse(query)
        result match {
            case parser.Failure(msg, next) => {
                throw new IllegalArgumentException(msg + "\n" + next.pos.longString)
            }
            case parser.Success(queryAST, _) => decorator.compile(queryAST)
        }
    }

    /**
     *
     */
    def compile(query: UnissonQuery)(implicit decorator: QueryCompiler = this): Relation[SourceElement[AnyRef]] = {
        import definitions._
        query match {
            case ClassSelectionQuery(pn, sn) => `class`(pn, sn)
            case ClassQuery(classQuery) => `class`(decorator.compile(classQuery))
            case ClassWithMembersQuery(ClassSelectionQuery(pn, sn)) => class_with_members(pn, sn)
            case ClassWithMembersQuery(classQuery) => class_with_members(decorator.compile(classQuery))
            case PackageQuery(pn) => `package`(pn)
            case OrQuery(left, right) => decorator.compile(left) or decorator.compile(right)
            case WithoutQuery(left, right) => decorator.compile(left) without decorator.compile(right)
            case MethodQuery(classQuery, name,returnType,parameters @_*) => method(decorator.compile(classQuery), name, decorator.compile(returnType), parameters.map(decorator.compile):_*)
            case FieldQuery(classQuery, name,fieldType) => field(decorator.compile(classQuery), name, decorator.compile(fieldType))
            case TransitiveQuery(SuperTypeQuery(innerQuery)) => transitive_supertype(decorator.compile(innerQuery))
            case SuperTypeQuery(innerQuery) => supertype(decorator.compile(innerQuery))
            case TypeQuery(name) => typeQuery(name)
            case EmptyQuery() => new EmptyResult[SourceElement[AnyRef]]()
            case DerivedQuery() => new EmptyResult[SourceElement[AnyRef]]()
            case _ => throw new IllegalArgumentException("Unknown query type: " + query)
        }
    }
}