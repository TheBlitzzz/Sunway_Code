package billSplitter.view.dialog

import billSplitter.model.PayeeModel
import billSplitter.util.FXMLUtil
import scalafx.scene.Scene
import scalafx.Includes._
import scalafx.stage.{Modality, Stage}
import javafx.{scene => jfxs}

object PayeeDialog {
  def showDialog(stageOwner: Stage, payee : PayeeModel, isAdd : Boolean): Boolean = {
    val loader = FXMLUtil.loadFXML("/billSplitter/view/Payee Details/PayeePopUp.fxml")
    val parentNode = loader.getRoot[jfxs.Parent]
    val dialogController = loader.getController[PayeeDialogController#Controller]
    val dialog = new Stage() {
      initModality(Modality.ApplicationModal)
      initOwner(stageOwner)
      scene = new Scene() {
        root = parentNode
      }
    }
    dialogController.init(dialog, payee, isAdd)
    dialog.showAndWait()
    dialogController.okClicked
  }
}
