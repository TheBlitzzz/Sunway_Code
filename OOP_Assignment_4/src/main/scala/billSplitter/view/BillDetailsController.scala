package billSplitter.view

import billSplitter.MainApp
import billSplitter.MainApp._
import billSplitter.model.{BillModel, InspectorItem, PayeeBillModel}
import billSplitter.util.AlertUtil
import billSplitter.util.InputValidationUtil.doubleToString
import billSplitter.view.dialog.BillDialog
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafx.scene.control.cell.CheckBoxTableCell
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.util.{Failure, Success}


@sfxml
class BillDetailsController
(
  private val titleLabel : Label,
  private val descriptionTextArea : TextArea,
  private val billDateDatePicker : DatePicker,
  private val totalBeforeTaxAndTipsLabel : Label,
  private val taxRateLabel : Label,
  private val tipRateLabel : Label,
  private val totalLabel : Label,
  private val billPerPayeeLabel : Label,
  private val payeeTableView : TableView[PayeeBillModel],
  private val payeeNameTableColumn : TableColumn[PayeeBillModel, String],
  private val payeeStatusTableColumn : TableColumn[PayeeBillModel, Boolean],
) {
  implicit var primaryStage : Stage = null
  private var targetBill : BillModel = null
  private var onInvalidBill : () => Unit = null
  private var billPayees: List[PayeeBillModel] = null

  // https://stackoverflow.com/questions/15477449/integer-columns-in-scalafx-tableview
  payeeNameTableColumn.cellValueFactory = prop => {
    new StringProperty(payeeList.find(_.id == prop.value.payeeId) match {
      case Some(x) => x.fullName
      case None => "Payee was deleted!"
    })
  }
  payeeStatusTableColumn.cellFactory = CheckBoxTableCell.forTableColumn[PayeeBillModel, Boolean]((x : Int) => {
    payeeTableView.getItems.get(x).hasPaidProp
  })

  def handleEditButton_OnAction(action: ActionEvent): Unit = {
    showEditDialog()
  }

  def handleUpdatePayeeStatus_OnAction(action: ActionEvent): Unit = {
    savePayeeStatus()
  }

  def readInspectorItemDetails(inspectorItem : Option[InspectorItem], onInvalid: () => Unit) : Unit = {
    onInvalidBill = onInvalid
    targetBill = inspectorItem match {
      case Some(x) => billList.find(_.id == x.uniqueId) match {
        case Some(x) => x
        case None => {
          onInvalidBill()
          return
        }
      }
      case None => {
        onInvalidBill()
        return
      }
    }

    billPayees = PayeeBillModel.getPayeeBillByBillId(targetBill.id)

    titleLabel.text = targetBill.titleProp.value
    descriptionTextArea.text = targetBill.descriptionProp.value
    billDateDatePicker.value = targetBill.billLocalDate
    totalBeforeTaxAndTipsLabel.text = doubleToString(targetBill.totalProp.value)
    taxRateLabel.text = doubleToString(targetBill.taxRateProp.value)
    tipRateLabel.text = doubleToString(targetBill.tipRateProp.value)
    totalLabel.text = doubleToString(targetBill.calculatedTotalAfterTaxAndTips)
    billPerPayeeLabel.text = doubleToString(targetBill.calculatedTotalAfterTaxAndTips / billPayees.size)

    payeeTableView.setItems(ObservableBuffer(billPayees))
  }

  private def showEditDialog(): Unit = {
    val controller = BillDialog.showDialog(primaryStage, targetBill, false)
    if (controller.okClicked) {
      targetBill.save() match {
        case Success(x) => {
          MainApp.refreshPayeeList()
          readInspectorItemDetails(Option(targetBill), onInvalidBill)
        }
        case Failure(x) => {
          AlertUtil.showDatabaseError("Database failed to update Payee profile!")
        }
      }
    }
  }

  private def savePayeeStatus(): Unit = {
    billPayees.foreach(x => {
      x.save() match {
        case Success(x) =>
        case Failure(x) => AlertUtil.showDatabaseError("Error updating payee bill status! Error Message : " + x.toString)
      }
    })
  }
}
