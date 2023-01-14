package battleship.client.view

import akka.actor.typed.ActorSystem
import battleship.client.view.dialogs.MyAlertDialogController
import battleship.client.view.nodes.{FleetDrawer, FleetGridController}
import battleship.client.{BattleshipClient, ClientGuardian}
import battleship.model.{ClientProfileModel, PlayerModel, WarshipDesignModel}
import battleship.utils.{MyFxml, UpdateLoop}
import battleship.utils.maths.{Rect, Vector2, Vector2Int}
import javafx.scene.Parent
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.event.subscriptions.Subscription
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.TextArea
import scalafx.scene.layout.{HBox, Pane, VBox}
import scalafx.scene.paint.Color
import scalafx.scene.transform.Transform.rotate
import scalafx.stage.Stage
import scalafxml.core.macros.sfxml

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.{implicitConversions, postfixOps}

@sfxml
class GameView(val rootPane: Pane,
               val gridContainer1: VBox,
               val gridContainer2: VBox,
               val battleshipPlacementControlPanel: HBox,
               val gameEventReadOnlyTextArea: TextArea,
               val battleshipFireControlPanel: HBox,
              ) extends GameController {
  def handleMoveWarshipLeftBtnOnAction(e: ActionEvent): Unit = {
    moveSelectedWarship(Vector2Int(-1 * localController.rootPane.scaleX.value.toInt, 0))
  }

  def handleMoveWarshipRightBtnOnAction(e: ActionEvent): Unit = {
    moveSelectedWarship(Vector2Int(1 * localController.rootPane.scaleX.value.toInt, 0))
  }

  def handleMoveWarshipUpBtnOnAction(e: ActionEvent): Unit = {
    moveSelectedWarship(Vector2Int.down)
  }

  def handleMoveWarshipDownBtnOnAction(e: ActionEvent): Unit = {
    moveSelectedWarship(Vector2Int.up)
  }

  def handleRotateWarshipBtnOnAction(e: ActionEvent): Unit = {
    rotateSelectedWarship()
  }

  def handleReadyBtnOnAction(e: ActionEvent): Unit = {
    clearState()
    localPlayer.notifyReadyToStartCombat()
//    players foreach { player =>
//      player.notifyReadyToStartCombat()
//    }
  }

  def handleBackBtnOnAction(e: ActionEvent): Unit = {
    val (root, _) = MainMenuController()
    rootPane.getScene.root = root
    gameSystem match {
      case Some(system) => system.terminate()
      case None =>
    }
    gameUpdateLoop.terminateLoop()
  }
}

object GameController {
  sealed trait GameState { val index: Int }
  final case class Awake() extends GameState { val index: Int = 0 }
  final case class FinishedInit() extends GameState { val index: Int = 1 }
  final case class PlacementEditorMode() extends GameState { val index: Int = 2 }
  final case class CombatMode() extends GameState { val index: Int = 3 }
  final case class GameEnded() extends GameState { val index: Int = 4 }

  def apply(): (Parent, GameController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[GameController]("/battleship/client/view/game_single_player_view.fxml")
    (root, controller)
  }
}

trait GameController {
  val rootPane: Pane
  val gridContainer1: VBox
  val gridContainer2: VBox
  val battleshipPlacementControlPanel: HBox
  val gameEventReadOnlyTextArea: TextArea
  val battleshipFireControlPanel: HBox

  protected  val subscriptions: ArrayBuffer[Subscription] = new ArrayBuffer[Subscription]()
  protected  var selectedShip: Int = -1

  protected var gameState : GameController.GameState = GameController.Awake()
  protected var gameSystem: Option[ActorSystem[ClientGuardian.Command]] = None

  protected var localPlayer: PlayerModel = _
  protected var localController: FleetGridController = _
  protected var opponentPlayer: PlayerModel = _
  protected var opponentController: FleetGridController = _
  protected var players: Array[PlayerModel] = _
  protected var controllers: Array[FleetGridController] = _

  protected val currentTurnPlayerProp: ObjectProperty[Int] = new ObjectProperty[Int]()
  currentTurnPlayerProp.value = -1

  protected val turnsTakenProp: ObjectProperty[Int] = new ObjectProperty[Int]()
  protected val timeGameStartedProp: ObjectProperty[Long] = new ObjectProperty[Long]()

  def init(stage: Stage, localPlayer: PlayerModel): Unit = {
    stage.resizable = false

    this.localPlayer = localPlayer
    players = if (localPlayer.playerIndex == 0) {
      opponentPlayer = PlayerModel(1, "Opponent")
      Array(localPlayer, opponentPlayer)
    } else {
      opponentPlayer = PlayerModel(0, "Opponent")
      Array(opponentPlayer, localPlayer)
    }
    localPlayer.opponent = opponentPlayer

    controllers = Array((gridContainer1, players(0)), (gridContainer2, players(1))) map {x =>
      val (gridContainer, player) = x

      val (fleetViewRoot, controller) = FleetGridController()
      gridContainer.children.add(fleetViewRoot)
      controller.initGrid(player)

      controller
    }
    localController = controllers(localPlayer.playerIndex)
    opponentController = controllers(opponentPlayer.playerIndex)

    BattleshipClient.clientSystem match {
      case Some(system) =>
        system ! ClientGuardian.CreateMultiPlayerGame(localPlayer)
      case None =>
    }
  }

//  def init(stage: Stage): Unit = {
//    stage.resizable = false
//
//    localPlayer = PlayerModel(0, "Side A")
//    players = Array(localPlayer, PlayerModel(1, "Opponent"))
//    val playerOpponentDummies: Array[PlayerModel] = players map { x => x.copy() }
//
//    localPlayer.opponent = playerOpponentDummies(1)
//    players(1).opponent = playerOpponentDummies(0)
//
//    controllers = Array((gridContainer1, localPlayer), (gridContainer2, players(1))) map {x =>
//      val (gridContainer, player) = x
//
//      val (fleetViewRoot, controller) = FleetGridController()
//      gridContainer.children.add(fleetViewRoot)
//      controller.initGrid(player)
//
//      controller
//    }
//    localController = controllers(localPlayer.playerIndex)
//    opponentController = controllers(opponentPlayer.playerIndex)
//
//    val clientProfile = ClientProfileModel("Side A", "", -1, -1)
//    val system = ActorSystem(ClientGuardian(clientProfile), "Battleship")
//    system ! ClientGuardian.CreateSinglePlayerGame(players)
//    gameSystem = Some(system)
//  }


  protected val gameEventText : ObservableBuffer[String] = new ObservableBuffer[String]()
  gameEventText onChange {
    // https://stackoverflow.com/questions/6193960/new-line-character-in-scala
    val newLine = sys.props("line.separator")
    gameEventReadOnlyTextArea.text.value = ""
    gameEventReadOnlyTextArea.appendText(gameEventText.mkString(newLine))
  }

  protected def clearState(): Unit = {
    subscriptions.foreach(x => x.cancel())
    subscriptions.clear()

    battleshipPlacementControlPanel.visible = false
    battleshipFireControlPanel.visible = false
    selectedShip = -1
  }

  protected def initGame() : Unit = {
    // Draw where you placed your ships
    localController.drawers ++= Array(localPlayer.drawShipPositions(), localPlayer.drawHitPositions())
//    opponentController.drawers ++= Array(opponentPlayer.drawShipPositions())

    // Initialise the warships
    localPlayer.initWarships(WarshipDesignModel.defaultWarships)

    // Flip the grid on the right
    controllers(1).rootPane.scaleX = -1
  }

  protected def startEditingWarshipPlacement(): Unit = {
    clearState()
    battleshipPlacementControlPanel.visible = true

    subscriptions += (localController.onMouseClicked onChange { (_, _, _) =>
      selectedShip = localPlayer.warshipPlacementMap(localController.hoveredIndex)
    })
  }

  protected def moveSelectedWarship(direction: Vector2Int): Unit = if (selectedShip > -1) {
    val warship = localPlayer.warships(selectedShip)

    val newPosition = warship.cellPosition + direction
    val newPlacementRect = Rect(warship.placementRect.size, newPosition)


    localPlayer.placeWarshipAt(selectedShip, newPlacementRect, warship.orientation)
    localController.refreshCanvas()
  }

  protected def rotateSelectedWarship(): Unit = if (selectedShip > -1) {
    val warship = localPlayer.warships(selectedShip)

    val newPlacementRect = Rect(Vector2(warship.placementRect.size.y, warship.placementRect.size.x), warship.cellPosition)
    val newOrientation = (warship.orientation + 90) % 360

    localPlayer.placeWarshipAt(selectedShip, newPlacementRect, newOrientation)
    localController.refreshCanvas()
  }

  protected def startGame(): Unit = {
    clearState()
    battleshipFireControlPanel.visible = true
    gameState = GameController.CombatMode()
    timeGameStartedProp.value = System.currentTimeMillis()

    // Draw the positions you have shot at
    opponentController.drawers += opponentPlayer.drawHitPositions()

    subscriptions += opponentController.onMouseClicked onChange { (_, _, _) =>
      if (localPlayer.myHitStatus == -1) {
        val hitStatus = localPlayer.shootAt(opponentController.hoveredIndex)
        if (hitStatus > -1) {
          if (hitStatus == 1) {
            gameEventText append "Hit enemy warship!"
          } else {
            gameEventText append "Missed!"
          }
          gameEventText append "Waiting for opponent!"
          turnsTakenProp.value += 1
        }
      }
    }

    if (localPlayer.playerIndex == 0) {
      gameEventText append "Your turn!"
    }  else {
      // block you from doing anything
      gameEventText append "Waiting for opponent!"
      localPlayer.myHitStatus = 2
    }
  }

//  protected def startGame_Single(): Unit = {
//    clearState()
//    battleshipFireControlPanel.visible = true
//    gameState = GameController.CombatMode()
//
//    var hitSubscription: Option[Subscription] = None
//    subscriptions += currentTurnPlayerProp onChange { (_, _, playerIndex) =>
//      hitSubscription match {
//        case Some(x) => x.cancel()
//        case None =>
//      }
//
//      val player = players(playerIndex)
//      val nextPlayerIndex =  player.opponent.playerIndex
//      val controller = controllers(nextPlayerIndex)
//      if (playerIndex == localPlayer.playerIndex) {
//        gameEventText append "Your turn!"
//      } else {
//        gameEventText append "Waiting for opponent to fire!"
//      }
//      hitSubscription = Some(controller.onMouseClicked onChange { (_, _, _) =>
//        val hitStatus = player.shootAt(controller.hoveredIndex)
//        if (hitStatus > -1) {
//          controller.refreshCanvas()
//          if (playerIndex == localPlayer.playerIndex) {
//            if (hitStatus == 1) {
//              gameEventText append "Hit enemy warship!"
//            } else {
//              gameEventText append "Missed!"
//            }
//          } else {
//            if (hitStatus == 1) {
//              gameEventText append "Opponent hit your warship!"
//            } else {
//              gameEventText append "Opponent missed!"
//            }
//          }
//        }
//      })
//    }
//  }

  protected def endGame(winnerPlayerIndex: Int): Unit = {
    clearState()
    BattleshipClient.disconnectFromServer()

    gameUpdateLoop.terminateLoop()
    Future {
      Platform.runLater({
        val totalDuration = System.currentTimeMillis() - timeGameStartedProp.value
        val totalSeconds = (totalDuration / 1000).toInt
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60

        val metrics = Array(("Turns taken", s"${turnsTakenProp.value}"), ("Duration", s"$minutes:$seconds"))
        val (root, _) = GameOverController(localPlayer.playerIndex == winnerPlayerIndex, metrics)
        rootPane.getScene.root = root
      })
    }
    gameUpdateLoop.terminateLoop()
  }

  protected val gameUpdateLoop :UpdateLoop = UpdateLoop { () =>
    Platform runLater {
      if (localPlayer.playerState == PlayerModel.OpponentOrServerLeft()) {
        gameUpdateLoop.terminateLoop()

        MyAlertDialogController("Network error", "Your opponent has disconnected or the server is down. Disconnecting you from the game.")
        BattleshipClient.disconnectFromServer()

        val (root, _) = MainMenuController()
        rootPane.getScene.root = root
      }

      if (localPlayer.playerState == PlayerModel.ReadyToInit() && gameState == GameController.Awake()) {
        initGame()
        gameState = GameController.FinishedInit()
      }

      if (localPlayer.playerState == PlayerModel.PlacementEditorMode() && gameState == GameController.FinishedInit()) {
        startEditingWarshipPlacement()
        gameState = GameController.PlacementEditorMode()
      }

      if (localPlayer.playerState == PlayerModel.CombatMode() && gameState == GameController.PlacementEditorMode()) {
        startGame()
        gameState = GameController.CombatMode()
      }

      if (localPlayer.opponentHitStatus > -1) {
        if (localPlayer.opponentHitStatus == 1) {
          gameEventText append "Opponent hit your warship!"
        } else {
          gameEventText append "Opponent missed!"
        }

        // reset the hit status, currently your turn
        localPlayer.opponentHitStatus = -1
        localPlayer.myHitStatus = -1
        gameEventText append "Your turn!"
      }

      if (localPlayer.playerState == PlayerModel.Defeated() && gameState == GameController.CombatMode()) {
        endGame(localPlayer.opponent.playerIndex)
        gameState = GameController.GameEnded()
      }
      if (localPlayer.opponent.playerState == PlayerModel.Defeated() && gameState == GameController.CombatMode()) {
        endGame(localPlayer.playerIndex)
        gameState = GameController.GameEnded()
      }

      controllers foreach { controller => controller.refreshCanvas() }
    }
  }
  gameUpdateLoop.run(1000 / 30)

  implicit class WarshipGridDrawer(val target: PlayerModel) {
    private val coordinateSystem = target.coordinateSystem
    private val warshipPlacementMap = target.warshipPlacementMap
    private val warships = target.warships
    private val hitMap = target.hitMap

    def drawShipPositions(): FleetDrawer = (canvas: Canvas) => {
      val gc = canvas.graphicsContext2D
      coordinateSystem.forEachCell((cellPos, rect) => {
        val warshipIndex = warshipPlacementMap(cellPos)
        if (warshipIndex > -1 && !warships(warshipIndex).isDestroyed) {
          gc.fillRect(rect.x, rect.y, rect.width, rect.height)
        }
      })

      warships.foreach(warship => if (!warship.isDestroyed) {
        val image = warship.design.createImage()
        val placementRect = warship.placementRect

        gc.save()
        val scaledRect = placementRect.scale(coordinateSystem.cellSize)
        val r = rotate(warship.orientation, scaledRect.x, scaledRect.y)
        gc.setTransform(r.getMxx, r.getMyx, r.getMxy, r.getMyy, r.getTx, r.getTy)
        // todo reimplement this with maths (sine + cosine)
        warship.orientation % 360 match {
          case 0 => gc.drawImage(image, scaledRect.x, scaledRect.y, scaledRect.width, scaledRect.height)
          case 90 => gc.drawImage(image, scaledRect.x, scaledRect.y - scaledRect.width, scaledRect.height, scaledRect.width)
          case 180 => gc.drawImage(image, scaledRect.x - scaledRect.width, scaledRect.y - scaledRect.height, scaledRect.width, scaledRect.height)
          case 270 => gc.drawImage(image, scaledRect.x - scaledRect.height, scaledRect.y, scaledRect.height, scaledRect.width)
        }
        gc.restore()
      })
    }

    def drawHitPositions(): FleetDrawer = (canvas: Canvas) => {
      val gc = canvas.graphicsContext2D
      coordinateSystem.forEachCell((cellPos, rect) => {
        val previousPaint = gc.fill
        if (hitMap(cellPos) >= 1) {
          gc.fill = Color(1, 0.5, 1, 0.5)
          gc.fillRect(rect.x, rect.y, rect.width, rect.height)
        }
        if (hitMap(cellPos) >= 0) {
          gc.fill = Color(1, 0.5, 0.5, 1)
          val x = Array(0.8, 0.9, 0.2, 0.1) map (x => rect.x + rect.width * x)
          val y = Array(0.1, 0.1, 0.9, 0.9) map (y => rect.y + rect.height * y)
          gc.fillPolygon(x, y, 4)
        }
        gc.fill = previousPaint
      })
    }
  }
}
