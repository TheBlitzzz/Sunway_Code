package battleship.utils.maths

import scalafx.scene.canvas.GraphicsContext

import scala.math.floor

case class GridCoordinateSystem(rect: Rect = Rect(Vector2(400.0, 400.0)), dimensions: Vector2Int = Vector2Int(15, 15), offset: Vector2 = Vector2(0, 0)) {
  val cellSize: Vector2 = rect.size / dimensions

  def toCellCoordinates(actualCoordinates : Vector2): Vector2Int = {
    val coordinatesOnGrid = actualCoordinates - offset
    val cellX = floor(coordinatesOnGrid.x / cellSize.x).toInt
    val cellY = floor(coordinatesOnGrid.y / cellSize.y).toInt

    Vector2Int(cellX, cellY)
  }

  def toWorldCoordinates(actualCoordinates : Vector2): Vector2 = cellSize * toCellCoordinates(actualCoordinates)

  def forEachCell(action: Vector2Int => Unit): Unit ={
    for (rowIndex <- 0 until rows) {
      for (columnIndex <- 0 until columns) {
        action(Vector2Int(rowIndex, columnIndex))
      }
    }
  }

  def forEachCell(action: (Vector2Int, Rect) => Unit): Unit = forEachCell(cellPosition => action(cellPosition, Rect(cellSize, cellSize * cellPosition)))

  def forEachCellIn(grid: IntGrid)(action: Int => Unit): Unit = forEachCell(cellPosition => action(grid(cellPosition)))


  def rows : Int = dimensions.x
  def columns : Int = dimensions.y

  def cellWidth : Double = cellSize.x
  def cellHeight : Double = cellSize.y
}
