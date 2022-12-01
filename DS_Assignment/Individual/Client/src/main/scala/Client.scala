import java.io.{BufferedReader, DataInputStream, DataOutputStream, InputStreamReader}
import java.net.Socket
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.io.StdIn.readLine
import scala.util.control.Breaks.{break, breakable}

object Client extends App {
  val messageTerminator = "\n"
  private var serverInput = ""

  breakable {
    val client = new Socket("127.0.0.1", 1000)
    val inStream = new DataInputStream(client.getInputStream)
    val inReader = new BufferedReader(new InputStreamReader(client.getInputStream))
    val outStream = new DataOutputStream(client.getOutputStream)

    outStream.writeBytes(s"Client Connecting$messageTerminator")

    Future({
      listenForServer(inReader)
    })

    while (true) {
      readLine("Enter your command (enter 'H' for list of command)\n").trim.toLowerCase match {
        case "h" =>
          println("All commands are case insensitive")
          println("Enter 'Write Message' or 'W' to send a message to the server")
          println("Enter 'Check Price' or 'P' to start looking up the price of a book given its ISBN code")
          println("Enter 'Exit' or 'E' to terminate the program")
          println("--------------------------------------------------------------------------------------------------\n")
        case "write message" | "w" =>
          talkToServer(outStream)
        case "check price" | "p" =>
          val isbnCode = readLine("Enter ISBN code you wish to look up\n")
          outStream.writeBytes(isbnCode + messageTerminator)

          val price = inStream.readDouble()
          println(s"Price is $price")
        case "exit" | "e" =>
          outStream.writeBytes(s"d$messageTerminator")
          client.close()
          break
        case _ => println("Command invalid")
      }
    }
  }

  private def talkToServer(outStream: DataOutputStream) = {
    outStream.writeBytes(s"w$messageTerminator")

    println("Starting session")
    val firstMessage = readLine("Type your messages here (Enter '\\x' or press 'enter' to exit)\n")
    if (firstMessage != "\\x") {
      outStream.writeBytes(firstMessage + messageTerminator)

      breakable {
        while (true) {
          val message = readLine()
          if (message == "\\x" | message.isBlank) {
            break
          }
          outStream.writeBytes(message + messageTerminator)
        }
      }
    }

    outStream.writeBytes(s"\\x$messageTerminator")
  }

  def listenForServer(inReader : BufferedReader): Unit = {
    while (true) {
      serverInput = inReader.readLine()
      println(serverInput)
    }
  }
}
