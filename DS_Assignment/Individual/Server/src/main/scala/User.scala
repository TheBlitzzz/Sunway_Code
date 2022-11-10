import java.io.{DataInputStream, DataOutputStream, File, FileOutputStream, PrintWriter}
import java.net.Socket

object User extends App{

  var exit = false
  println("Connecting..")

  while (!exit){

    val client = new Socket ( "127.0.0.1", 1000)

    val is = new DataInputStream(client.getInputStream())
    val os = new DataOutputStream(client.getOutputStream())

    println(s"\n" + s"Connected to port ${client.getLocalPort}")
    Thread.sleep(1000)

    os.writeBytes(s"${scala.io.StdIn.readLine("Enter ISBN number: ")} \n")
    val price = is.readLine()

    if (price == null) {
      println(s"ISBN number not found.")
    }else {
        Thread.sleep(1000)
        println(s"Price: RM$price"+"\n")
      }
    val continue = scala.io.StdIn.readLine("Search another price? (Y/N): ").toUpperCase()
    Thread.sleep(2000)

    if (continue == "N"){
      println("Disconnecting...")
      exit = true
      Thread.sleep(500)
      println("Disconnected")
    }
    client.close()
  }
}