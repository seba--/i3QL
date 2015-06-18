import java.io._
import java.net.ServerSocket
import java.nio.file.{Files, FileSystem, Paths}

import scala.collection.mutable

/**
 * This class is used as a data source for the SparkStreaming benchmark.
 *
 * @author Mirko Köhler
 */
object DataProvider {

	def main(args: Array[String]) {
		val port = 9999
		val cache : mutable.MutableList[String] = mutable.MutableList.empty

		val dir0 = Paths.get("C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/res/dta_kernkorpus_2014-03-10")

		val dirstream = Files.newDirectoryStream(dir0)

		println("Cache files...")

		val it = dirstream.iterator
		while (it.hasNext) {
			val next = it.next()

			var reader = new BufferedReader(new FileReader(next.toFile))
			var s = reader.readLine()
			while(s != null) {
				cache += s
				s = reader.readLine()
			}
			reader.close()

		}
		dirstream.close()

		val serverSocket = new ServerSocket(port)
		println("Listening on port " + port)

		while (true) {
			val socket = serverSocket.accept()
			println("Got a new connection")
			val out = new OutputStreamWriter(socket.getOutputStream)
			println("All data sent.")
			try {
				cache.foreach(out.write)
			//	serverSocket.close()
			} catch {
				case e: IOException =>
					println("Client disconnected")
					socket.close()
			}
		}
		println("Finished.")
	}

}
