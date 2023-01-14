package battleship.client.view

import battleship.client.BattleshipClient
import battleship.client.view.dialogs.MyAlertDialogController
import battleship.utils.{FancyTitle, MyFxml, MyNetwork}
import javafx.scene.Parent
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{ComboBox, Label, TextField}
import scalafx.scene.layout.Pane
import scalafxml.core.macros.sfxml

import java.net.InetAddress
import scala.util.{Failure, Random, Success, Try}

@sfxml
class MainMenuView(val rootPane: Pane,
                   val seedNodeAddressTextField: TextField,
                   val localAddressComboBox: ComboBox[String],
                   val localPortTextField: TextField,
                   val usernameTextField: TextField,
                   val gameTitleLabel : Label,
                  ) extends MainMenuController {

//  def handleStartLocalBtnOnAction(e: ActionEvent): Unit = {
//    val (_, root, controller) = MyFxml.loadFxmlRoot[GameController]("/battleship/client/view/game_single_player_view.fxml")
//    val stage = BattleshipClient.stage
//    stage.getScene.root = root
//    controller.init(stage)
//  }

  def handleStartOnlineBtnOnAction(e: ActionEvent): Unit = {
    if (!inputClusterAddress.isBlank) {
      BattleshipClient.connectToServer(inputUsername, inputLocalAddress, inputPort, inputClusterAddress)
      val (root, _) = LobbyController()
      rootPane.getScene.root = root
    } else {
//      BattleshipClient.connectToServer(inputUsername, inputLocalAddress, inputPort)
//      val (root, _) = LobbyController()
//      rootPane.getScene.root = root
      MyAlertDialogController("Required input", "Please enter a valid seed node address!")
    }
  }

  def handleExitBtnOnAction(e: ActionEvent): Unit = {
    BattleshipClient.disconnectFromServer()
    BattleshipClient.stage.close()
  }
}

object MainMenuController {
  def apply(): (Parent, MainMenuController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[MainMenuController]("/battleship/client/view/main_menu_view.fxml")
    (root, controller)
  }
}

trait MainMenuController {
  val rootPane : Pane
  val localAddressComboBox: ComboBox[String]
  val localPortTextField: TextField
  val seedNodeAddressTextField: TextField
  val usernameTextField: TextField
  val gameTitleLabel : Label

  BattleshipClient.disconnectFromServer()

  FancyTitle.applyFancyTitleEffectOn(gameTitleLabel)

  localPortTextField.text.value = "2552"

  val networkAddressList: ObservableBuffer[String] = new ObservableBuffer[String]
  networkAddressList.append(InetAddress.getLocalHost.getHostAddress)
  localAddressComboBox.setItems(networkAddressList)
  localAddressComboBox.selectionModel.value.select(0)
  MyNetwork.populateComboBoxWithNetworkInterfaces(networkAddressList, localAddressComboBox)



  def inputLocalAddress: String = {
    val inputText = localAddressComboBox.value.value
    if (inputText.isBlank) {
      InetAddress.getLocalHost.getHostAddress
    } else {
      inputText
    }
  }

  def inputPort: Int = {
    Try {
      localPortTextField.text.value.toInt
    } match {
      case Success(port) =>
        if (port >= 0) {
          port
        } else {
          2551
        }
      case Failure(exception) =>
        println("Invalid Port! Defaulting to 2551")
        println(exception.getMessage)
        2551
    }
  }

  def inputClusterAddress : String = {
    seedNodeAddressTextField.text.value
  }

  def inputUsername : String = {
    val inputText = usernameTextField.text.value

    if (inputText.isBlank) {
      RandomisedNames()
    } else {
      inputText
    }

  }

  object RandomisedNames {
    val randomNames: Array[String] = Array[String]("Hunter", "Sea Captain", "Wolf", "Swimmer", "Sailor", "Marine")

    def apply(seed: Long = System.currentTimeMillis()): String = {
      val prng = new Random(seed)
      "Anonymous " + randomNames(prng.nextInt(randomNames.length))
    }
  }
}
