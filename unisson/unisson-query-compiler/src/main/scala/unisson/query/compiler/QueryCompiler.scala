package unisson.query.compiler

import sae.LazyView
import unisson.query.code_model.SourceElement
import unisson.query.UnissonQuery
import sae.bytecode.Database

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:17
 *
 */
trait QueryCompiler
{

    def db: Database


    def definitions: QueryDefinitions

    /**
     * parse and compile the query in one pass
     */
    def parseAndCompile(query: String)(implicit decorator: QueryCompiler = this): LazyView[SourceElement[AnyRef]]

    /**
     * compile a query from a given parse tree.
     * Whenever the compiler descends in the parse tree the <code>decorator</code> is called.
     * Thus  <code>decorator</code> provides a hook, for extending a recursive compilation process.
     */
    def compile(query: UnissonQuery)(implicit decorator: QueryCompiler = this): LazyView[SourceElement[AnyRef]]

}