package battleship.client.view.dialogs

import battleship.client.ClientGuardian
import battleship.utils.MyFxml
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Dialog, TextField}
import scalafxml.core.macros.sfxml

import scala.util.matching.Regex

@sfxml
class CreateRoomDialog(val roomNameTextField : TextField,
                       val errorReadOnlyTextField: TextField,
                      ) extends CreateRoomDialogController {

  def handleCreateBtnOnClick(e: ActionEvent): Unit = {
    val inputText = roomNameTextField.text.value
    if (validateRoomName(inputText)) {
      println(inputText)
      dialog.setResult(inputText)
      dialog.close()
    }
  }

  def handleCancelBtnOnClick(e: ActionEvent): Unit = {
    dialog.setResult("")
    dialog.close()
  }
}

object CreateRoomDialogController {
  def apply() : String = {
    val dialog = new Dialog[String]()

    val (_, root, controller) = MyFxml.loadFxmlNode[javafx.scene.control.DialogPane, CreateRoomDialogController]("/battleship/client/view/dialogs/create_room_dialog.fxml")
    dialog.dialogPane = root

    controller.init(dialog)

    dialog.showAndWait() match {
      case Some(x) => x.toString
      case None => ""
    }
  }
}

trait CreateRoomDialogController {
  val roomNameTextField : TextField
  val errorReadOnlyTextField : TextField

  protected var dialog : Dialog[String] = _

  // ensures the name is valid for an actor
  // ASCII char only, no special chars other than : -_*$+:@&=,!~;.
  // Finally got the regex here https://regex101.com/
  private val noSpecialVarRegex = new Regex("""[^a-zA-Z0-9-_*$+:@&=,!~;.]""")

  roomNameTextField.textProperty.onChange((_, _, roomName) => {
    if (validateRoomName(roomName)){
      errorReadOnlyTextField.disable = true
    } else {
      errorReadOnlyTextField.disable = false
    }
  })
  roomNameTextField.text.value = "MyRoom"

  def init(dialog: Dialog[String]): Unit = {
    this.dialog = dialog
    dialog.onHidden = () => {
      dialog.close()
    }
  }

  def validateRoomName(roomName : String) : Boolean = {
    errorReadOnlyTextField.text.value = ""

    if (roomName.isBlank) {
      errorReadOnlyTextField.text.value = "Error : 'Room Name' is a required text field"
      return false
    }

    noSpecialVarRegex findFirstIn roomName match {
      case Some(x) =>
        errorReadOnlyTextField.text.value = s"Error : 'Room Name' cannot contain '$x'"
        return false
      case None =>
    }

    ClientGuardian.roomsInLobby foreach {
      case Some(gameRoom) =>
        if (roomName == gameRoom.idName) {
          errorReadOnlyTextField.text.value = s"Error : Room with name '$roomName' already exists!"
          return false
        }
      case None =>
    }

    true
  }
}
