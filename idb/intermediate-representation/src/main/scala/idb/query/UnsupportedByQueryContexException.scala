package idb.query

/**
 * @author Mirko Köhler
 */
class UnsupportedByQueryEnvironmentException(message: String, val queryEnvironment : QueryEnvironment) extends Exception(message)  {


}
