package battleship.client.view

import battleship.client.BattleshipClient
import battleship.utils.{MyFxml, MyImmutableRecord}
import javafx.scene.Parent
import scalafx.event.ActionEvent
import scalafx.scene.control.{Label, TextArea}
import scalafx.scene.layout.{Pane, StackPane}
import scalafxml.core.macros.sfxml
import scalafx.Includes._

@sfxml
class GameOverView (val rootPane: Pane,
                    val gameOverTitleLabel: Label,
                    val metricsTitleTextArea: TextArea,
                    val metricsValueTextArea: TextArea,
                   ) extends GameOverController {

  def handleBackBtnOnAction(e: ActionEvent): Unit = {
    val (root, _) = MainMenuController()
    rootPane.getScene.root = root
  }

}

object GameOverController {
  def apply(playerWon: Boolean, metrics: Array[(String, String)]): (Parent, GameOverController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[GameOverController]("/battleship/client/view/game_over_view.fxml")
    controller.init(playerWon, metrics)
    (root, controller)
  }
}

trait GameOverController {
  val rootPane: Pane
  val gameOverTitleLabel: Label
  val metricsTitleTextArea: TextArea
  val metricsValueTextArea: TextArea

  def init(playerWon: Boolean, metrics: Array[(String, String)]): Unit = {
    if (playerWon) {
      gameOverTitleLabel.text = "VICTORY"
    } else {
      gameOverTitleLabel.text = "DEFEAT"
    }

    val newLine = sys.props("line.separator")
    metricsTitleTextArea.text = ""
    metricsValueTextArea.text = ""
    metrics foreach { metric =>
      metricsTitleTextArea appendText s"${metric._1}$newLine"
      metricsValueTextArea appendText s"->     ${metric._2}$newLine"
    }
  }
}
