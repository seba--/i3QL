package idb.query

/**
 * @author Mirko Köhler
 */
class UnsupportedByQueryEnvironmentException(message: String, val env : QueryEnvironment) extends Exception(message)  {


}
