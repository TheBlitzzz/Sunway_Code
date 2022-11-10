package billSplitter.view.dialog

import billSplitter.MainApp.payeeList
import billSplitter.model.{BillModel, PayeeBillModel, PayeeModel}
import billSplitter.util.AlertUtil
import billSplitter.util.InputValidationUtil.{doubleToString, nullChecking, parseToDouble}
import scalafx.beans.property.{BooleanProperty, StringProperty}
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.util.{Failure, Success}

@sfxml
class BillDialogController
(
  private val dialogTitleLabel : Label,
  private val titleTextField : TextField,
  private val descriptionTextArea : TextArea,
  private val billDateDatePicker: DatePicker,
  private val totalTextField: TextField,
  private val taxRateTextField: TextField,
  private val tipRateTextField: TextField,
  private val payeeTitleLabel : Label,
  private val payeeTableView : TableView[(PayeeModel, BooleanProperty)],
  private val payeeNameTableColumn : TableColumn[(PayeeModel, BooleanProperty), String],
  private val payeeIsIncludedTableColumn : TableColumn[(PayeeModel, BooleanProperty), Boolean],
  private val applyButton: Button,
){
  private implicit var dialogStage : Stage = null
  private var targetBill : BillModel = null
  private var isAdding : Boolean = false
  var okClicked = false
  private val potentialPayees = payeeList.map({(_, BooleanProperty(false))})

  payeeNameTableColumn.cellValueFactory = prop => {
    new StringProperty(payeeList.find(_.id == prop.value._1.id) match {
      case Some(x) => x.fullName
      case None => "Payee was deleted!"
    })
  }

  // Saw the function here
  // https://stackoverflow.com/questions/15903452/issue-binding-checkbox-in-javafx-tableview
  payeeIsIncludedTableColumn.cellFactory = CheckBoxTableCell.forTableColumn[(PayeeModel, BooleanProperty), Boolean]((x : Int) => {
    potentialPayees(x)._2
  })

  payeeTableView.setItems(potentialPayees)

  def handleApplyButton_OnClicked(action :ActionEvent): Unit = {
    val applySuccess = updateValuesToTarget()
    if (applySuccess) {
      dialogStage.close()
    }
  }

  def handleCancelButton_OnClicked(action :ActionEvent) {
    dialogStage.close()
  }

  def init(stage: Stage, billModel: BillModel, isAddingNewBill: Boolean): Unit = {
    dialogStage = stage
    targetBill = billModel
    isAdding = isAddingNewBill
    if (isAdding) {
      dialogTitleLabel.text = "Add New Bill"
      applyButton.text = "Add"
      payeeTableView.visible = true
      payeeTableView.disable = false
      payeeTitleLabel.visible = true

      totalTextField.setEditable(true)
      tipRateTextField.setEditable(true)
      taxRateTextField.setEditable(true)
    } else {
      dialogTitleLabel.text = "Edit Bill"
      applyButton.text = "Apply"
      payeeTableView.visible = false
      payeeTableView.disable = true
      payeeTitleLabel.visible = false

      totalTextField.setEditable(false)
      tipRateTextField.setEditable(false)
      taxRateTextField.setEditable(false)
    }

    titleTextField.text = targetBill.titleProp.value
    descriptionTextArea.text = targetBill.descriptionProp.value
    billDateDatePicker.value = targetBill.billLocalDate
    totalTextField.text = doubleToString(targetBill.totalProp.value)
    taxRateTextField.text = doubleToString(targetBill.taxRateProp.value)
    tipRateTextField.text = doubleToString(targetBill.tipRateProp.value)
  }

  def createPayeeBills(actualBillId : Int): ObservableBuffer[PayeeBillModel] = {
    val payees = potentialPayees.filter(_._2.value)
    payees.map(item => {
      val payeeBill = new PayeeBillModel(item._1.id, actualBillId, false)
      payeeBill.save() match {
        case Success(x) =>
        case Failure(x) => AlertUtil.showDatabaseError("Unable to save payee bill status! Error message : " + x.toString)
      }
      payeeBill
    })
  }

  private def updateValuesToTarget(): Boolean = {
    if (isInputValid) {
      // Only bind the values if user applies it.
      targetBill.titleProp <== titleTextField.text
      targetBill.descriptionProp <== descriptionTextArea.text
      targetBill.billLocalDate = billDateDatePicker.value.value
      targetBill.totalProp.value = totalTextField.text().toDouble
      targetBill.taxRateProp.value = taxRateTextField.text().toDouble
      targetBill.tipRateProp.value = tipRateTextField.text().toDouble
      okClicked = true
      return true
    }
    false
  }

  private def isInputValid : Boolean = {
    var errorMessage = ""

    if (nullChecking(titleTextField.text.value)) {
      errorMessage += "Title cannot be empty!\n"
    }
    if (billDateDatePicker.value == null) {
      errorMessage += "Date is empty!\n"
    }
    if (nullChecking(taxRateTextField.text.value)) {
      errorMessage += "Tax Rate cannot be empty!\n"
    } else {
      parseToDouble(taxRateTextField.text.value) match {
        case Success(x) => if (x < 0) errorMessage += "Tax Rate cannot be negative!\n"
        case Failure(exception) => errorMessage += "Tax Rate is not a number!"
      }
    }
    if (nullChecking(tipRateTextField.text.value)) {
      errorMessage += "Tip Rate cannot be empty!\n"
    } else {
      parseToDouble(tipRateTextField.text.value) match {
        case Success(x) => if (x < 0) errorMessage += "Tip Rate cannot be negative!\n"
        case Failure(exception) => errorMessage += "Tip Rate is not a number!"
      }
    }
    if (nullChecking(totalTextField.text.value)) {
      errorMessage += "Total cannot be empty!\n"
    } else {
      parseToDouble(totalTextField.text.value) match {
        case Success(x) => if (x <= 0) errorMessage += "Total cannot be negative or zero!\n"
        case Failure(exception) => errorMessage += "Total is not a number!"
      }
    }

    if (isAdding) {
      potentialPayees.find(_._2.value) match {
        case Some(x) => Unit
        case None => errorMessage += "Your bill has no Payees!"
      }
    }

    if (errorMessage.isEmpty) {
      true
    } else {
      AlertUtil.showInvalidInputAlert(errorMessage)
      false
    }
  }
}
