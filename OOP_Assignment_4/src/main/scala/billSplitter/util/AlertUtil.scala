package billSplitter.util

import scalafx.scene.control.{Alert, ButtonType}
import scalafx.stage.Stage

object AlertUtil {

  def showInvalidInputAlert(errorMessage: String)(implicit ownerStage : Stage): Option[ButtonType] = {
   new Alert(Alert.AlertType.Error){
      initOwner(ownerStage)
      title = "Invalid Fields"
      headerText = "Please correct invalid fields"
      contentText = errorMessage
    }.showAndWait()
  }

  def showDatabaseError(errorMessage: String)(implicit ownerStage : Stage): Option[ButtonType] = {
    new Alert(Alert.AlertType.Warning) {
      initOwner(ownerStage)
      title = "Failed to Save"
      headerText = "Database Error"
      contentText = errorMessage
    }.showAndWait()
  }
}
