import java.io.{BufferedReader, DataInputStream, DataOutputStream, InputStreamReader}
import java.net.{ServerSocket, Socket}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.control.Breaks.{break, breakable}

object MyServer extends App {
  val msg_error_clientAbruptDisconnection = "Client disconnected abruptly"
  println("Init : Starting server at port 1000")
  val server = new ServerSocket(1000)
  println("Init : Server started\n")

  println("Init : Reading prices from file")
  // Get the book prices first
  val bufferedSource = io.Source.fromFile("src/main/resources/price.txt")
  val lines = bufferedSource.getLines
  val bookPricesMap = lines.map(_.split(",").map(_.trim)).map(x => (x(0), x(1).toDouble)).toMap
  bufferedSource.close()
  println("Init : File read\n")

  println("Init : Accepting clients")
  processSocket(server.accept())

  runMenu()

  def processSocket(client: Socket): Unit = {
    Future({
      clientSession(client)
    })
    Future(processSocket(server.accept()))
  }

  private def clientSession(client: Socket): Unit = {
    val inStream = new DataInputStream(client.getInputStream)
    // DataInputStream.readLine() is a depreciated function. So I found the BufferedReader which works
    val inReader = new BufferedReader(new InputStreamReader(client.getInputStream))
    val outStream = new DataOutputStream(client.getOutputStream)
    // Client connection print
    val clientLocalPort = inStream.readInt()
    val clientId = "Client " + System.currentTimeMillis()
    println(s"$clientId : connection established, client local port : $clientLocalPort\n")

    breakable {
      while (true) {
        // Read the messages from the clients
        // Found the new version of readLine since it's depreciated https://docs.oracle.com/javase/7/docs/api/java/io/DataInputStream.html
        Option(inReader.readLine().trim.toLowerCase) match {
          case Some(clientMsg) =>
            clientMsg match {
              case "c" =>
                println(s"$clientId - Price Check : Started")
                if (checkPrice()) {
                  println(s"$clientId - Price Check : Ended")
                } else {
                  println(s"$clientId - Price Check : $msg_error_clientAbruptDisconnection")
                }
              case "e" =>
                println(s"$clientId - Exit : Client disconnected")
                break
              case _ =>
                println(s"$clientId : Unknown command from client : $clientMsg")
            }
          case None =>
            // No input means the client disconnected abruptly (Program terminated without application finished)
            println(s"$clientId : $msg_error_clientAbruptDisconnection")
            break
        }
      }

      def checkPrice(): Boolean = {
        Option({
          inReader.readLine()
        }) match {
          case Some(isbnCode) =>
            println(s"$clientId - Price Check : Client entered '$isbnCode'")
            val price = bookPricesMap(isbnCode)
            println(s"$clientId - Price Check : Price of $isbnCode is $price")
            outStream.writeDouble(price)
            true
          case None =>
            // No input means the client disconnected abruptly (Program terminated without application finished)
            false
        }
      }
    }
    client.close()
  }

  def runMenu(): Unit = {
    readLine("Menu : Enter any key to exit\n")
    server.close()
  }
}
