package battleship.server.view

import battleship.server.BattleshipServer
import battleship.server.BattleshipServer.serverMem
import battleship.utils.{MyFxml, MyNetwork}
import javafx.scene.Parent
import scalafx.beans.property.{BooleanProperty, ObjectProperty}
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, ComboBox, TextField}
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import java.net.InetAddress
import scala.util.{Failure, Success, Try}

@sfxml
class BootServerView(val startServerBtn: Button,
                     val stopServerBtn: Button,
                     val ipAddressComboBox: ComboBox[String],
                     val portTextField: TextField,
                     val clusterAddressTextField: TextField,
                     val serverStatusReadOnlyTextField: TextField,
                    ) extends BootServerController {
  def handleStartServerBtnOnAction(e: ActionEvent): Unit = {
    BattleshipServer.startServer(inputIpAddress, inputPort, inputClusterAddress)
    serverMem.clusterAddress = inputClusterAddress
    serverMem.localAddress = s"$inputIpAddress:$inputPort"
    serverMem.serverStarted = true
    serverStartedProp.value = true
  }

  def handleStopServerBtnOnAction(e: ActionEvent): Unit = {
    BattleshipServer.stopServer()
    ServerClusterController.observedServers.clear()
    serverMem.clusterAddress = ""
    serverMem.localAddress = ""
    serverMem.serverStarted = false
    serverStartedProp.value = false
  }
}

object BootServerController {
  def apply() : (Parent, BootServerController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[BootServerController]("/battleship/server/view/boot_server_view.fxml")
    (root, controller)
  }
}

trait BootServerController {
  val startServerBtn: Button
  val stopServerBtn: Button
  val ipAddressComboBox: ComboBox[String]
  val portTextField: TextField
  val clusterAddressTextField: TextField
  val serverStatusReadOnlyTextField: TextField

  portTextField.text.value = "2551"

  val networkAddressList: ObservableBuffer[String] = new ObservableBuffer[String]
  networkAddressList.append(InetAddress.getLocalHost.getHostAddress)
  MyNetwork.populateComboBoxWithNetworkInterfaces(networkAddressList, ipAddressComboBox)
  ipAddressComboBox.setItems(networkAddressList)

  val serverStartedProp: ObjectProperty[Boolean] = new ObjectProperty[Boolean]()
  serverStartedProp.onChange((_, _, newVal) => onServerStatusChanged(newVal))
  serverStartedProp.value = serverMem.serverStarted

  protected def onServerStatusChanged(serverStarted : Boolean): Unit = {
    if (serverStarted) {
      startServerBtn.disable = true
      stopServerBtn.disable = false
      serverStatusReadOnlyTextField.text.value = s"Joined cluster at ${serverMem.clusterAddress}, local address at ${serverMem.localAddress}"
    } else {
      startServerBtn.disable = false
      stopServerBtn.disable = true
      serverStatusReadOnlyTextField.text.value = "Not running"
    }
  }

  def inputIpAddress: String = {
    val inputText = ipAddressComboBox.value.value
    if (inputText.isBlank) {
      InetAddress.getLocalHost.getHostAddress
    } else {
      inputText
    }
  }

  def inputPort: Int = {
    Try {
      portTextField.text.value.toInt
    } match {
      case Success(port) =>
        port
      case Failure(exception) =>
        println("Invalid Port! Defaulting to 2551")
        println(exception.getMessage)
        2551
    }
  }

  def inputClusterAddress: String = {
    val inputText = clusterAddressTextField.text.value

    if (inputText.isBlank) {
      s"$inputIpAddress:$inputPort"
    } else {
      inputText
    }
  }
}
