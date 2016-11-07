package idb.algebra.exceptions

import idb.query.Host
import idb.query.colors.ColorId

/**
  * @author Mirko Köhler
  */
class RemoteException(msg : String) extends RuntimeException(msg) {

}

class NonMatchingHostsException(a : Host, b : Host)
	extends RemoteException("The hosts are not compatible: " + a + ", " + b)

class RemoteUnsupportedException
	extends RemoteException("Cannot use remote functionality.")

class NoServerAvailableException(s : String)
	extends RemoteException(s) {

	def this() = this("No server available.")

	def this(colors : Iterable[ColorId]) =
		this("No server available for colors " + colors)
}

class InsufficientRootPermissionsException(root : Any, rootPermissions : Any, requiredPermissions : Any)
	extends RemoteException(s"Root '$root' lacks permissions for '$requiredPermissions'; only has permissions for '$rootPermissions'.")

class UnknownHostDeployException
	extends RemoteException("Cannot deploy query on unknown host.")
