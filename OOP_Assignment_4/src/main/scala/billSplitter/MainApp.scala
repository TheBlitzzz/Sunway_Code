package billSplitter

import billSplitter.model.{BillModel, PayeeBillModel, PayeeModel}
import billSplitter.util.{Database, FXMLUtil}
import billSplitter.view.RootLayoutController
import javafx.{scene => jfxs}
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene

object MainApp extends JFXApp {
  Database.setupDB()

  val payeeList = new ObservableBuffer[PayeeModel]()
  refreshPayeeList()

  val billList = new ObservableBuffer[BillModel]()
  refreshBillList()

  val payeeBillList = new ObservableBuffer[PayeeBillModel]()
  refreshPayeeBillList()

  private val loader = FXMLUtil.loadFXML("/billSplitter/view/Root Layout/RootLayoutView.fxml")
  // retrieve the root component from the FXML
  private val mainRoots = loader.getRoot[jfxs.Parent]
  private val mainController = loader.getController[RootLayoutController#Controller]

  // initialize stage
  stage = new PrimaryStage {
    title = "Bill Splitter App"
    scene = new Scene {
      root = mainRoots
    }
  }

  implicit val mainStage = stage

  mainController.primaryStage = stage

  def refreshPayeeList(): Unit = {
    payeeList.clear()
    payeeList ++= PayeeModel.getAllPayees
  }

  def refreshBillList(): Unit ={
    billList.clear()
    billList ++= BillModel.getAllBills
  }

  def refreshPayeeBillList(): Unit ={
    payeeBillList.clear()
    payeeBillList ++= PayeeBillModel.getAllPayeeBills
  }
}
