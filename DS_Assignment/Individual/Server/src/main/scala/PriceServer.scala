import java.io.{DataInputStream, DataOutputStream}
import java.net. {ServerSocket, Socket}
import scala.io.{BufferedSource, Source}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

object MyServer extends App {
  print ("Hello World")
}

//object PriceServer extends App {
//  val server = new ServerSocket(1000)
//  //open server socket
//
//  while (true) {
//    // create thread with client socket as parameter and remove it once service is provided
//    val client: Socket = server.accept()
//    // wait for client request
//
//    Future({
//      //store local socket references for processing
//      val is = new DataInputStream(client.getInputStream())
//      val os = new DataOutputStream(client.getOutputStream())
//      //create I/O streams to communicate to the client
//
//      val line: String = is.readLine()
//
//      def execute (txt: => Unit): Unit = ExecutionContext.global.execute( new Runnable {def run(): Unit = txt})
//
//      execute {
//        val file:BufferedSource = Source.fromFile("price.txt")
//        val lines = file.getLines.toList
//        val datas = lines.map(x => x.split(",")).map(xs => ((xs(0), xs(1).toDouble))).toMap
//
//          }
//      })
//    client.close()
//
//  }
//}
