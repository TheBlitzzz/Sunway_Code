package battleship.client.view.dialogs

import battleship.model.{ClientProfileModel, GameRoomModel}
import battleship.utils.{MyFxml, MyImmutableRecord}
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Dialog, TableColumn, TableView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class RoomInfoDialog(val roomNameReadOnlyTextField: TextField,
                     val serverAddressReadOnlyTextField: TextField,
                     val playerTableView: TableView[MyImmutableRecord],
                     val playerUsernameColumn : TableColumn[MyImmutableRecord, String],
                     val joinBtn: Button,
                      ) extends RoomInfoDialogController {

  def handleJoinBtnOnClick(e: ActionEvent): Unit = {
    dialog.setResult(true)
    dialog.close()
  }

  def handleCancelBtnOnClick(e: ActionEvent): Unit = {
    dialog.setResult(false)
    dialog.close()
  }
}

object RoomInfoDialogController {
  def apply(gameRoom: GameRoomModel, players: Traversable[ClientProfileModel], canJoin: Boolean) : Boolean = {
    val dialog = new Dialog[Boolean]()

    val (_, root, controller) = MyFxml.loadFxmlNode[javafx.scene.control.DialogPane, RoomInfoDialogController]("/battleship/client/view/dialogs/room_info_dialog.fxml")
    dialog.dialogPane = root

    controller.init(dialog, gameRoom, players, canJoin)

    dialog.showAndWait() match {
      case Some(x) => x.toString.toBoolean
      case None => false
    }
  }
}

trait RoomInfoDialogController {
  val roomNameReadOnlyTextField: TextField
  val serverAddressReadOnlyTextField: TextField
  val playerTableView: TableView[MyImmutableRecord]
  val playerUsernameColumn: TableColumn[MyImmutableRecord, String]
  val joinBtn: Button

  protected var dialog : Dialog[Boolean] = _


  def init(dialog: Dialog[Boolean], gameRoom: GameRoomModel, players: Traversable[ClientProfileModel], canJoin: Boolean): Unit = {
    this.dialog = dialog
    dialog.onHidden = () => {
      dialog.close()
    }

    val playerList = new ObservableBuffer[MyImmutableRecord]()
    playerList ++= players map {x => MyImmutableRecord(("Username", x.username))}

    roomNameReadOnlyTextField.text.value = gameRoom.idName
    serverAddressReadOnlyTextField.text.value = gameRoom.serverAddress

    MyImmutableRecord.bindToTable(playerList, playerTableView, playerUsernameColumn)
    if (canJoin) {
      joinBtn.disable = false
    } else {
      joinBtn.disable = true
    }
  }
}
