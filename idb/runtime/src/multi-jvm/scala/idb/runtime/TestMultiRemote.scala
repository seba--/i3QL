package idb.runtime

/**
 * @author Mirko Köhler
 */
class TestMultiRemote {

	object RemoteTestMultiJvmNode1 {
		println("Hello from node 1!")
	}

	object RemoteTestMultiJvmNode2 {
		println("Hello from node 2!")
	}

}
