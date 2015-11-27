package idb.query

/**
 * @author Mirko Köhler
 */
class UnsupportedByQueryContextException(message: String, val queryContext : QueryContext) extends Exception(message)  {


}
