package battleship.utils.maths

class IntGrid(val dimensions: Vector2Int, defaultValue: Int = -1) {
  val cells: Array[Array[Int]] = Array.fill(dimensions.x)(Array.fill(dimensions.y)(defaultValue))

  def apply(x: Int, y: Int): Int = cells(x)(y)
  def apply(cellCoordinates : Vector2Int): Int = cells(cellCoordinates.x)(cellCoordinates.y)

  def update(x: Int, y: Int, value: Int): Unit = cells(x)(y) = value
  def update(cellCoordinates : Vector2Int, value: Int): Unit = cells(cellCoordinates.x)(cellCoordinates.y) = value

  def updateAll(value: Int): Unit = updateInRect(Rect(dimensions), value)

  def updateInRect(rect: Rect, value: Int): Unit ={
    for (rowIndex <- rect.x.toInt until rect.endPoint.x.toInt) {
      for (colIndex <- rect.y.toInt until rect.endPoint.y.toInt) {
        update(rowIndex, colIndex, value)
      }
    }
  }

  def copy(grid: IntGrid): Unit ={
    for (rowIndex <- 0 until dimensions.x) {
      for (colIndex <- 0 until dimensions.y) {
        update(rowIndex, colIndex, grid(rowIndex, colIndex))
      }
    }
  }
}
