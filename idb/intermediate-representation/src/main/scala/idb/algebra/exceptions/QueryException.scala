package idb.algebra.exceptions

import idb.query.Host

/**
  * @author Mirko Köhler
  */
class QueryException(msg : String) extends RuntimeException(msg) {

}

class NonMatchingHostsException(a : Host, b : Host)
	extends QueryException("The hosts are not compatible: " + a + ", " + b)

class RemoteUnsupportedException
	extends QueryException("Cannot use remote functionality.")

class NoServerAvailableException
	extends QueryException("No server available for color.")

class UnknownHostDeployException
	extends QueryException("Cannot deploy query on unknown host.")
