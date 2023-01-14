package battleship.server.view

import battleship.server.BattleshipServer
import battleship.utils.MyFxml
import javafx.scene.Parent
import javafx.scene.layout.{StackPane => jfxStackPane}
import scalafx.Includes._
import scalafx.event.ActionEvent
import scalafx.scene.control.Button
import scalafx.scene.layout.{BorderPane, VBox}
import scalafxml.core.macros.sfxml

@sfxml
class MainMenuView(val mainBorderPane : BorderPane,
                   val mainTabIndicatorVbox: VBox,
                  ) extends MainMenuController {
  toggleView(0)

  def handleBootServerBtnOnAction(e : ActionEvent) : Unit = {
    toggleView(0)
  }

  def handleViewServersBtnOnAction(e : ActionEvent) : Unit = {
    toggleView(1)
  }

  def handleViewClientsBtnOnAction(e : ActionEvent) : Unit = {
    toggleView(2)
  }

  def handleExitBtnOnAction(e: ActionEvent): Unit = {
    BattleshipServer.stage.close()
  }
}


object MainMenuController {
  def apply() : (Parent, MainMenuController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[MainMenuController]("/battleship/server/view/main_menu_view.fxml")
    (root, controller)
  }
}

trait MainMenuController {
  val mainBorderPane : BorderPane
  val mainTabIndicatorVbox: VBox

  protected def toggleView(tabViewIndex: Int) : Unit = {
    tabViewIndex match {
      case 0 =>
        val (root, _) = BootServerController()
        mainBorderPane.center = root
        mainTabIndicatorVbox.styleClass(1) = "indicator-boot-pos"
      case 1 =>
        val (root, _) = ServerClusterController()
        mainBorderPane.center = root
        mainTabIndicatorVbox.styleClass(1) = "indicator-server-pos"
      case 2 =>
        val (root, _) = ClientClusterController()
        mainBorderPane.center = root
        mainTabIndicatorVbox.styleClass(1) = "indicator-client-pos"
    }
  }
}
