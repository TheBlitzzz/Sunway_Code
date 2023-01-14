import java.io.{BufferedReader, DataInputStream, DataOutputStream, File, FileOutputStream, InputStreamReader, PrintWriter}
import java.net.Socket
import scala.io.StdIn.readLine
import scala.util.{Failure, Success, Try}
import scala.util.control.Breaks.{break, breakable}

object MyClient extends App{
  val connectionAddressInput = readLine("Please enter the connection address\n")
  val connectionAddress = if (connectionAddressInput.isBlank) "127.0.0.1" else connectionAddressInput
  println(s"Init : Connecting to $connectionAddress")

  // Default connection val : 127.0.0.1
  // "172.19.110.22"
  Try({
    new Socket(connectionAddress, 1000)
  }) match {
    case Success(client) =>// DataInputRead.readLine() is a depreciated function. So I found the BufferedReader which works
      val inStream = new DataInputStream(client.getInputStream)
      val inReader = new BufferedReader(new InputStreamReader(client.getInputStream))
      val outStream = new DataOutputStream(client.getOutputStream)

      outStream.writeInt(client.getLocalPort)

      breakable({
        while (true) {
          readLine("Main Menu : What is your next command? (enter 'H' for help)\n").trim.toLowerCase match {
            case "h" =>
              println("Help : Started")

              println("Help : Enter 'C' to check the price of a book by entering the isbn code")
              println("Help : Enter 'E' to end the program")

              println("Help : Ended")
            case "c" =>
              println("Price check : Started")

              outStream.writeBytes("c\n")
              val isbn = readLine("Price check : Please enter the ISBN of the book you wish to check the price of\n")
              outStream.writeBytes(s"$isbn\n")
              val bookPrice = inStream.readDouble()
              println(s"Price check : The price is RM$bookPrice")

              println("Price check : Ended")
            case "e" =>
              outStream.writeBytes("e\n")
              println("Main Menu : Disconnecting")
              break
            case _ => println("Unknown command")
          }
        }
      })

      outStream.writeBytes("e\n")
      client.close()
    case Failure(exception) =>println(s"Init : Connection failed, error message : ${exception.getMessage}")
  }
}
