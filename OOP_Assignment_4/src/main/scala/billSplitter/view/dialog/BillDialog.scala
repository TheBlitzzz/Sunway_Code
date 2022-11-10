package billSplitter.view.dialog

import billSplitter.model.BillModel
import billSplitter.util.FXMLUtil
import javafx.{scene => jfxs}
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.stage.{Modality, Stage}

object BillDialog {
  def showDialog(stageOwner: Stage, bill : BillModel, isAdd : Boolean): BillDialogController#Controller = {
    val loader = FXMLUtil.loadFXML("/billSplitter/view/Bill Details/BillPopUp.fxml")
    val parentNode = loader.getRoot[jfxs.Parent]
    val dialogController = loader.getController[BillDialogController#Controller]
    val dialog = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(stageOwner)
      scene = new Scene() {
        root = parentNode
      }
    }
    dialogController.init(dialog, bill, isAdd)
    dialog.showAndWait()
    dialogController
  }
}
