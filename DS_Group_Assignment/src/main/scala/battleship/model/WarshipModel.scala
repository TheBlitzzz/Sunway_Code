package battleship.model

import battleship.utils.maths.{Rect, Vector2Int}

class WarshipModel(val indexId : Int, val design: WarshipDesignModel) {
  var healthPoints: Int = design.shipWidth * design.shipLength
  def isDestroyed : Boolean = healthPoints == 0

  /**
   * The top left cell position of the warship. (0, 0) if at the top left.
   */
  def cellPosition: Vector2Int = placementRect.start

  private var _orientation : Double = 0.0
  def orientation : Double = _orientation

  private var _placementRect: Rect = Rect(design.shipSize)
  def placementRect: Rect = _placementRect
  private def placementRect_=(value: Rect): Unit = _placementRect = value

  // Use sine to calculate the position, and use midpoint instead
  def setTransform(cellPosition: Vector2Int, orientation: Double): Unit = {
    _orientation = orientation

    placementRect = if (orientation % 180 == 0) {
      Rect(design.shipSize, cellPosition)
    } else {
      Rect(Vector2Int(design.shipLength, design.shipWidth), cellPosition)
    }
  }
}
