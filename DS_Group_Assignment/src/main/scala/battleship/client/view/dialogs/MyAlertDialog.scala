package battleship.client.view.dialogs

import battleship.utils.MyFxml
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.{Dialog, Label, TextArea}
import scalafxml.core.macros.sfxml

@sfxml
class MyAlertDialog(val titleLabel: Label,
                    val messageReadOnlyTextArea: TextArea,
                   ) extends MyAlertDialogController {
  def handleOkBtnOnClick(e: ActionEvent): Unit = {
    dialog.setResult(true)
    dialog.close()
  }
}

object MyAlertDialogController {
  def apply( title: String, messageBody: String) : Unit = {
    val dialog = new Dialog[Boolean]()

    val (_, root, controller) = MyFxml.loadFxmlNode[javafx.scene.control.DialogPane, MyAlertDialogController]("/battleship/client/view/dialogs/my_alert_dialog.fxml")
    dialog.dialogPane = root

    controller.init(dialog, title, messageBody)
    dialog.showAndWait()
  }
}

trait MyAlertDialogController {
  val titleLabel: Label
  val messageReadOnlyTextArea: TextArea

  protected var dialog : Dialog[Boolean] = _


  def init(dialog: Dialog[Boolean], title: String, messageBody: String): Unit = {
    this.dialog = dialog
    dialog.onHidden = () => {
      dialog.setResult(true)
      dialog.close()
    }

    titleLabel.text.value = title
    messageReadOnlyTextArea.text.value = messageBody
  }
}
