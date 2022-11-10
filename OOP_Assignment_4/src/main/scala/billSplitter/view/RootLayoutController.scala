package billSplitter.view

import billSplitter.MainApp
import billSplitter.MainApp.{billList, payeeList}
import billSplitter.model.{BillModel, InspectorItem, PayeeModel}
import billSplitter.util.{AlertUtil, Database, FXMLUtil, InspectorItemViewFactory}
import billSplitter.view.dialog.{BillDialog, PayeeDialog}
import javafx.{scene => jfxs}
import scalafx.Includes._
import scalafx.beans.value.ObservableValue
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.event.subscriptions.Subscription
import scalafx.scene.control._
import scalafx.scene.layout.BorderPane
import scalafx.scene.shape.SVGPath
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.util.{Failure, Success}

@sfxml
class RootLayoutController
(
  private val contentRoot : BorderPane,
  private val inspectedItemsListView : ListView[InspectorItem]
)
{
  implicit var primaryStage : Stage = null

  private var pageIndex : PageIndex.Value = PageIndex.None
  private var inspectorIndex : PageIndex.Value = PageIndex.PayeeDetails
  private var payeeDetailsController : PayeeDetailsController#Controller = null
  private var billDetailsController : BillDetailsController#Controller = null
  private var inspectedItemListSelectedItemOnChange : Subscription = null

  inspectedItemsListView.cellFactory = InspectorItemViewFactory.listCellFactory

// https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ContextMenu.html
// https://javadoc.io/doc/org.scalafx/scalafx_2.10/1.0.0-M2/scalafx/scene/control/ContextMenu.html
  inspectedItemsListView.contextMenu = new ContextMenu() {
    items.append(
      MenuItem.sfxMenuItem2jfx(new MenuItem("Delete") {
        onAction = deleteInspectorItem
      }),
    )
  }

  showPayeeList()

  // Separate the function (in case if it is needed elsewhere), so that the buttons always bind to the same function
  def handlePayeeListButton_OnAction(action : ActionEvent) : Unit = {
    showPayeeList()
  }

  def handleBillListButton_OnAction(action : ActionEvent) : Unit = {
    showBillList()
  }

  def handleAddButton_OnAction(action : ActionEvent) : Unit = {
    inspectorIndex match {
      case PageIndex.PayeeDetails => showAddPayeeDialog()
      case PageIndex.BillDetails => showAddBillDialog()
    }
  }

  def handleResetDBMenuItem_OnAction(action : ActionEvent) : Unit = {
    Database.resetDB()
    MainApp.refreshPayeeList()
    MainApp.refreshBillList()
    MainApp.refreshPayeeBillList()

    showPayeeList()
  }

  def handleCloseMenuItem_OnAction(action : ActionEvent) : Unit = {
    // https://stackoverflow.com/questions/12153622/how-to-close-a-javafx-application-on-window-close
    primaryStage.close()
  }

  def showPayeeList() : Unit = {
    inspectorIndex = PageIndex.PayeeDetails

    if (pageIndex != PageIndex.PayeeDetails) {
      setContentToNothingSelected()
    }
    removePreviousSubscription()

    inspectedItemsListView.setItems(ObservableBuffer[InspectorItem](payeeList))
    inspectedItemListSelectedItemOnChange = inspectedItemsListView.selectionModel().selectedItem.onChange((_, _, item) => {
      // Load the FXML file
      if (pageIndex != PageIndex.PayeeDetails) {
        pageIndex = PageIndex.PayeeDetails
        val loader = FXMLUtil.loadFXML("/billSplitter/view/Payee Details/PayeeDetailsView.fxml")
        contentRoot.setCenter(loader.getRoot[jfxs.Parent])
        payeeDetailsController = loader.getController[PayeeDetailsController#Controller]
        payeeDetailsController.primaryStage = primaryStage
      }
      val payee = payeeList.find(_.id == item.uniqueId)
      payeeDetailsController.readInspectorItemDetails(payee, () => {
        setContentToNothingSelected()
      })
    })
  }

  def showBillList() : Unit = {
    inspectorIndex = PageIndex.BillDetails

    if (pageIndex != PageIndex.BillDetails) {
      setContentToNothingSelected()
    }
    removePreviousSubscription()

    inspectedItemsListView.setItems(ObservableBuffer[InspectorItem](billList))
    inspectedItemListSelectedItemOnChange = inspectedItemsListView.selectionModel().selectedItem.onChange((_, _, item) => {
      // Load the FXML file
      if (pageIndex != PageIndex.BillDetails) {
        pageIndex = PageIndex.BillDetails
        val loader = FXMLUtil.loadFXML("/billSplitter/view/Bill Details/BillDetailsView.fxml")
        contentRoot.setCenter(loader.getRoot[jfxs.Parent])
        billDetailsController = loader.getController[BillDetailsController#Controller]
      }
      val bill = billList.find(_.id == item.uniqueId)
      billDetailsController.readInspectorItemDetails(bill, () => {
        setContentToNothingSelected()
      })
    })
  }

  private def setContentToNothingSelected() : Unit = {
    pageIndex = PageIndex.None
    val loader = FXMLUtil.loadFXML("/billSplitter/view/Root Layout/NothingSelectedView.fxml")
    contentRoot.setCenter(loader.getRoot[jfxs.layout.BorderPane])

    payeeDetailsController = null
    billDetailsController = null
  }

  private def removePreviousSubscription() : Unit = {
    // Found this by looking into the source code of onChange
    if (inspectedItemListSelectedItemOnChange != null) {
      inspectedItemListSelectedItemOnChange.cancel()
      inspectedItemListSelectedItemOnChange = null
    }
  }

  private def showAddPayeeDialog() = {
    val newPayee = new PayeeModel(-1, "", "", "")
    val isNewPayeeCreated = PayeeDialog.showDialog(primaryStage, newPayee, true)
    if (isNewPayeeCreated) {
      newPayee.save() match {
        case Success(x) => {
          MainApp.refreshPayeeList()
          showPayeeList()
          inspectedItemsListView.selectionModel().selectLast()
        }
      case Failure(x) => {
          AlertUtil.showDatabaseError("Database failed to save new Payee profile!")
        }
      }
    }
  }

  private def showAddBillDialog() = {
    val newBill = new BillModel(-1, "", "", System.currentTimeMillis(), 0, 0, 0)
    val dialogController = BillDialog.showDialog(primaryStage, newBill, true)
    if (dialogController.okClicked) {
      newBill.save() match {
        case Success(x) => {
          MainApp.refreshBillList()
          val bill = billList.last
          dialogController.createPayeeBills(bill.id)

          showBillList()
          inspectedItemsListView.selectionModel().selectLast()
        }
        case Failure(x) => {
          AlertUtil.showDatabaseError("Database failed to save new bill details! Error Message : " + x.toString)
        }
      }
    }
  }

  private def deleteInspectorItem(action: ActionEvent): Unit = {
    val selectedItem = inspectedItemsListView.selectionModel().selectedItemProperty().value
    if (selectedItem == null) return

    inspectorIndex match {
      case PageIndex.PayeeDetails => {
        payeeList.find(_.id == selectedItem.uniqueId) match {
          case Some(x) => {
            x.delete() match {
              case Success(x) => Unit
              case Failure(e) => AlertUtil.showDatabaseError(s"Database failed to delete Payee profile!\nError message: ${e.toString}")
            }
          }
        }
        MainApp.refreshPayeeList()
        showPayeeList()
      }
      case PageIndex.BillDetails => {
        billList.find(_.id == selectedItem.uniqueId) match {
          case Some(x) => {
            x.delete() match {
              case Success(x) => Unit
              case Failure(e) => AlertUtil.showDatabaseError(s"Database failed to delete Bill record!\nError message: ${e.toString}")
            }
          }
        }
        MainApp.refreshBillList()
        showBillList()
      }
    }
    setContentToNothingSelected()
  }

  // https://www.scala-lang.org/api/2.13.x/scala/Enumeration.html
  object PageIndex extends Enumeration {
    type PageIndex = Value

    val None, PayeeDetails, BillDetails = Value
  }
}
