package billSplitter.view

import billSplitter.MainApp
import billSplitter.MainApp.payeeList
import billSplitter.model.{InspectorItem, PayeeModel}
import billSplitter.util.AlertUtil
import billSplitter.view.dialog.PayeeDialog
import scalafx.event.ActionEvent
import scalafx.scene.control.{Label, TextArea}
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.util.{Failure, Success}

@sfxml
class PayeeDetailsController
(
  private val fullNameLabel : Label,
  private val firstNameLabel : Label,
  private val lastNameLabel: Label,
  private val notesTextArea : TextArea,
){
  implicit var primaryStage : Stage = null
  private var targetPayee : PayeeModel = null
  private var onInvalidPayee : () => Unit = null

  def handleEditButton_OnAction(action: ActionEvent): Unit = {
    showEditDialog()
  }

  def readInspectorItemDetails (inspectorItem : Option[InspectorItem], onInvalid: () => Unit) : Unit = {
    onInvalidPayee = onInvalid
    targetPayee = inspectorItem match {
      case Some(x) => payeeList.find(_.id == x.uniqueId) match {
        case Some(x) => x
        case None => {
          onInvalidPayee()
          return
        }
      }
      case None => {
        onInvalidPayee()
        return
      }
    }

    fullNameLabel.text = s"${targetPayee.fullName}#${targetPayee.id}"
    firstNameLabel.text = targetPayee.firstNameProp.value
    lastNameLabel.text = targetPayee.lastNameProp.value
    notesTextArea.text = targetPayee.notesProp.value
  }

  def showEditDialog(): Unit = {
    val isPayeeEdited = PayeeDialog.showDialog(primaryStage, targetPayee, false)
    if (isPayeeEdited) {
      targetPayee.save() match {
        case Success(x) => {
          MainApp.refreshPayeeList()
          readInspectorItemDetails(Option(targetPayee), onInvalidPayee)
        }
        case Failure(x) => {
          AlertUtil.showDatabaseError("Database failed to update Payee profile!")
        }
      }
    }
  }
}
