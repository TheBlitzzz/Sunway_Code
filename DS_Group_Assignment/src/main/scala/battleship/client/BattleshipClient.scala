package battleship.client

import akka.actor.typed.ActorSystem
import battleship.client.view.MainMenuController
import battleship.model.ClientProfileModel
import battleship.utils.{MyConfigurations, UpdateLoop}
import battleship.utils.MyConfigurations.ConfigChain
import scalafx.Includes._
import scalafx.application.JFXApp3
import scalafx.scene.Scene

object BattleshipClient extends JFXApp3 {
  private var _clientSystem : Option[ActorSystem[ClientGuardian.Command]] = None
  def clientSystem : Option[ActorSystem[ClientGuardian.Command]] = _clientSystem

  var profile: Option[ClientProfileModel] = None

  override def start(): Unit = {
    val (root, _) = MainMenuController()

    stage = new JFXApp3.PrimaryStage() {
      title = "Battleship Client"
      scene = new Scene(root)
    }
  }

  override def stopApp(): Unit = {
    disconnectFromServer()
    UpdateLoop.notifyAppHasTerminated()
    super.stopApp()
  }

  def connectToServer(username: String, localIpAddress : String, port: Int, clusterAddress : String): Unit = {
    clientSystem match {
      case Some(_) =>
        println("Already connected to a server!")
      case None =>
        val config = MyConfigurations.defaultConfig withCluster() withPort port withHostname localIpAddress withSeedNode clusterAddress

        val profileVal = ClientProfileModel(username, localIpAddress, port, System.currentTimeMillis(), Some(clusterAddress))
        profile = Some(profileVal)
        val mainSystem = ActorSystem(ClientGuardian(profileVal), "Battleship", config)
        _clientSystem = Some(mainSystem)
    }
  }

//  def connectToServer(username: String, localIpAddress : String, port: Int): Unit = {
//    clientSystem match {
//      case Some(_) =>
//        println("Already connected to a server!")
//      case None =>
//        val config = MyConfigurations.defaultConfig withPort port withHostname localIpAddress
//
//        val profileVal = ClientProfileModel(username, localIpAddress, port, System.currentTimeMillis(), None)
//        profile = Some(profileVal)
//        val mainSystem = ActorSystem(ClientGuardian(profileVal), "Battleship", config)
//        _clientSystem = Some(mainSystem)
//    }
//  }

  def disconnectFromServer(): Unit = {
    clientSystem match {
      case Some(x) => x.terminate()
      case None => println("Was not connected to a server")
    }
    profile = None
    _clientSystem = None
  }
}
