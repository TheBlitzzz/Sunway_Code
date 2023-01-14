package battleship.server

import akka.actor.typed.ActorSystem
import akka.cluster.typed.Cluster
import battleship.server.view.MainMenuController
import battleship.utils.MyConfigurations
import battleship.utils.MyConfigurations.ConfigChain
import battleship.utils.MyNetwork.findNetworkAddresses
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene

object BattleshipServer extends JFXApp3 {
  private var serverSystem: Option[ActorSystem[ServerGuardian.Command]] = None

  val serverMem: ServerMem = new ServerMem

  findNetworkAddresses()

  override def start(): Unit = {
    val (root, controller) = MainMenuController()

    stage = new JFXApp3.PrimaryStage() {
      title = "Battleship Server"
      scene = new Scene(root)
    }
  }

  override def stopApp(): Unit = {
    stopServer()
    super.stopApp()
  }

  def startServer(ipAddress: String, port: Int, clusterIpAddress: String): Unit = {
    serverSystem match {
      case Some(_) =>
        println("Server already running!")
      case None =>
        val config = MyConfigurations.defaultConfig withCluster() withPort port withHostname ipAddress withSeedNode clusterIpAddress

        val mainSystem = ActorSystem(ServerGuardian(ipAddress, port, System.currentTimeMillis()), "Battleship", config)
        val serverCluster = Cluster(mainSystem)
        mainSystem ! ServerGuardian.StartLobby(serverCluster)

        serverSystem = Some(mainSystem)
    }
  }

  def stopServer(): Unit = {
    serverSystem match {
      case Some(x) =>
        x.terminate()
      case None =>
        println("Server was not active")
    }
    serverSystem = None
  }
}
