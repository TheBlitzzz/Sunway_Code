package battleship.client.view.nodes

import battleship.client.view.nodes.FleetGridController.{defaultGridBorderPaint, defaultGridFillPaint, defaultIndicatorRectFill}
import battleship.model.PlayerModel
import battleship.utils.MyFxml
import battleship.utils.maths.Vector2Int
import javafx.scene.Parent
import scalafx.beans.property.ObjectProperty
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.paint.{Color, Paint}
import scalafx.scene.shape.Rectangle
import scalafxml.core.macros.sfxml

import scala.collection.mutable.ArrayBuffer

@sfxml
class FleetGridView(val rootPane: Pane,
                    val canvas: Canvas,
                    val indicatorRect: Rectangle,
                   ) extends FleetGridController {

  def handleCanvasOnMouseClicked(event : MouseEvent): Unit = onMouseClicked.value = event
  def handleCanvasOnMouseEntered(event : MouseEvent): Unit = onMouseEntered.value = event
  def handleCanvasOnMouseMoved(event : MouseEvent): Unit = onMouseMoved.value = event
  def handleCanvasOnMouseExited(event : MouseEvent): Unit = onMouseExited.value = event
}

object FleetGridController {
  private val defaultGridBorderPaint: Paint = Color(42 / 255.0, 34 / 255.0, 41 / 255.0, 1)
  private val defaultGridFillPaint: Paint = Color(89 / 255.0, 63 / 255.0, 88 / 255.0, 1)
  private val defaultIndicatorRectFill: Paint = Color(97 / 255.0, 147 / 255.0, 198 / 255.0, 0.5)

  def apply(): (Parent, FleetGridController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[FleetGridController]("/battleship/client/view/nodes/fleet_grid_view.fxml")
    (root, controller)
  }
}

trait FleetGridController {
  val rootPane: Pane
  val canvas: Canvas
  val indicatorRect: Rectangle

  val onMouseClicked : ObjectProperty[MouseEvent] = new ObjectProperty[MouseEvent]()
  val onMouseMoved : ObjectProperty[MouseEvent] = new ObjectProperty[MouseEvent]()
  val onMouseEntered : ObjectProperty[MouseEvent] = new ObjectProperty[MouseEvent]()
  val onMouseExited : ObjectProperty[MouseEvent] = new ObjectProperty[MouseEvent]()

  val drawers : ArrayBuffer[FleetDrawer] = new ArrayBuffer[FleetDrawer]()

  var hoveredIndex : Vector2Int = Vector2Int.minusOne
  var player : PlayerModel = _

  indicatorRect.fill = defaultIndicatorRectFill
  canvas.graphicsContext2D.stroke = defaultGridBorderPaint
  canvas.graphicsContext2D.fill = defaultGridFillPaint

  def initGrid(player: PlayerModel): Unit = {
    this.player = player

    indicatorRect.width = player.coordinateSystem.cellWidth
    indicatorRect.height = player.coordinateSystem.cellHeight

    canvas.width = player.coordinateSystem.rect.size.x
    canvas.height = player.coordinateSystem.rect.size.y

    onMouseEntered onChange{ (_, _, _) =>
      indicatorRect.visible.value = true
    }

    onMouseMoved onChange{ (_, _, event : MouseEvent) =>
      hoveredIndex = player.coordinateSystem.toCellCoordinates((event.x, event.y))
      if (hoveredIndex.x < 0 || hoveredIndex.x >= player.coordinateSystem.rows || hoveredIndex.y < 0 || hoveredIndex.y >= player.coordinateSystem.columns) {
        hoveredIndex = Vector2Int.minusOne
      } else {
        val cellPosition = player.coordinateSystem.toWorldCoordinates((event.x, event.y))
        indicatorRect.translateX = cellPosition.x
        indicatorRect.translateY = cellPosition.y
      }
    }

    onMouseExited onChange { (_, _, _) =>
      hoveredIndex = Vector2Int.minusOne
      indicatorRect.visible.value = false
    }

    refreshCanvas()
  }

  def refreshCanvas(): Unit = {
    canvas.graphicsContext2D.clearRect(0, 0, canvas.width.value, canvas.height.value)
    drawers.foreach(x => x.drawOnCanvas(canvas))
    player.coordinateSystem.forEachCell((_, rect) => canvas.graphicsContext2D.strokeRect(rect.x, rect.y, rect.endPointX, rect.endPointY))
  }
}

trait FleetDrawer {
  def drawOnCanvas(canvas: Canvas) : Unit
}
