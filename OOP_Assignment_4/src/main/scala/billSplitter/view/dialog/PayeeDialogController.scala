package billSplitter.view.dialog

import billSplitter.model.PayeeModel
import billSplitter.util.AlertUtil
import billSplitter.util.InputValidationUtil.nullChecking
import scalafx.event.ActionEvent
import scalafx.scene.control.{Button, Label, TextArea, TextField}
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

@sfxml
class PayeeDialogController
(
  private val titleLabel : Label,
  private val firstNameTextField : TextField,
  private val lastNameTextField : TextField,
  private val notesTextArea : TextArea,
  private val applyButton: Button,
){
  private implicit var dialogStage : Stage = null
  private var targetPayee : PayeeModel = null
  var okClicked = false

  def handleApplyButton_OnClicked(action :ActionEvent) {
    val applySuccess = updateValuesToTarget()
    if (applySuccess) {
      dialogStage.close()
    }
  }

  def handleCancelButton_OnClicked(action :ActionEvent) {
    dialogStage.close()
  }

  def init(stage: Stage, payeeModel: PayeeModel, isAddingNewPayee: Boolean): Unit = {
    dialogStage = stage
    targetPayee = payeeModel
    if (isAddingNewPayee) {
      titleLabel.text = "Add New Payee"
      applyButton.text = "Add"
    } else {
      titleLabel.text = "Edit Payee"
      applyButton.text = "Apply"
    }

    firstNameTextField.text = payeeModel.firstNameProp.value
    lastNameTextField.text = payeeModel.lastNameProp.value
    notesTextArea.text = payeeModel.notesProp.value
  }

  private def updateValuesToTarget(): Boolean = {
    if (isInputValid) {
      // Only bind the values if user applies it.
      targetPayee.firstNameProp <== firstNameTextField.text
      targetPayee.lastNameProp <== lastNameTextField.text
      targetPayee.notesProp <== notesTextArea.text

      okClicked = true
      return true
    }
    false
  }

  private def isInputValid : Boolean = {
    var errorMessage = ""

    if (nullChecking(firstNameTextField.text.value)) {
      errorMessage += "First name cannot be empty!\n"
    }
    if (nullChecking(lastNameTextField.text.value)) {
      errorMessage += "Last name cannot be empty!\n"
    }

    if (errorMessage.isEmpty) {
      true
    } else {
      AlertUtil.showInvalidInputAlert(errorMessage)
      false
    }
  }
}
