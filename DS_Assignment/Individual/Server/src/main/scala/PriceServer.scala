import java.io.{BufferedReader, DataInputStream, DataOutputStream, InputStreamReader}
import java.net.{ServerSocket, Socket}
import scala.io.{BufferedSource, Source}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn.readLine
import scala.util.control.Breaks.{break, breakable}

object MyServer extends App {
  val msg_error_clientAbruptDisconnection = "Client disconnected abruptly"

  println("Starting server")
  val server = new ServerSocket(1000)
  println("Server started\n")

  println("Reading prices from file")
  // Get the book prices first
  val bufferedSource = io.Source.fromFile("src/main/resources/price.txt")
  val lines = bufferedSource.getLines
  val bookPricesMap = lines.map(_.split(",").map(_.trim)).map(x => (x(0), x(1).toDouble)).toMap
  bufferedSource.close()
  println("File read\n")

  println("Accepting clients")
  processSocket(server.accept())

  runMenu()

  def processSocket(client: Socket): Unit = {
    Future({
      clientSession(client)
    })
    Future(processSocket(server.accept()))
  }

  private def clientSession(client: Socket): Unit = {
    // val inStream = new DataInputStream(client.getInputStream)
    val outStream = new DataOutputStream(client.getOutputStream)
    val inReader = new BufferedReader(new InputStreamReader(client.getInputStream))

    // Client connection print
    print(inReader.readLine())
    outStream.writeBytes("Connection established\n")

    breakable {
      while (true) {
        // Read the messages from the clients
        // Found the new version of readline since it's depreciated https://docs.oracle.com/javase/7/docs/api/java/io/DataInputStream.html
        Option(inReader.readLine().trim.toLowerCase) match {
          case Some(x) =>
            x match {
              case "w" =>
                println("Client is talking")
                talkToClient(inReader)
                println("Client stopped talking")
              case "c" =>
                println("Client is looking up the price of a book")
                checkPrice(inReader, outStream)
                println("Client has stopped looking up books")
              case "d" =>
                println("Client disconnected")
                client.close()
                break
            }
          case None =>
            println(msg_error_clientAbruptDisconnection)
            client.close()
            break
        }
      }
    }
  }

  def runMenu(): Unit = {
    readLine("Enter any key to exit\n")
    server.close()
  }

  private def talkToClient(inReader: BufferedReader): Unit = {
    while (true) {
      Option(inReader.readLine()) match {
        case Some(message) =>
          if (message == "\\x" | message.isBlank) {
            return
          } else {
            println(s"Received '$message' from client")
          }
        case None =>
          println(msg_error_clientAbruptDisconnection)
      }
    }
  }

  private def checkPrice(inReader: BufferedReader, outStream: DataOutputStream): Unit = {
    val isbnCode = inReader.readLine()
    println(s"Client entered '$isbnCode'")
    val price = bookPricesMap(isbnCode)
    println(s"Price of $isbnCode is $price")
    outStream.writeDouble(price)
  }
}
