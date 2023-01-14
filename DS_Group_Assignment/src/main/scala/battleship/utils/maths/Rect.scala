package battleship.utils.maths

object Rect {
  def fromMidpoint(size: Vector2, midpoint: Vector2 = Vector2.zero): Rect = Rect(midpoint / 2, size)
}

// Remember that the translations on the y-axis ARE REVERSED, meaning it does not start from the bottom left, a higher y-value results in a lower position
case class Rect(size: Vector2, start: Vector2 = Vector2.zero) {
  def scale(value: Vector2): Rect = Rect(size * value, start * value)

  def containsPoint(point: Vector2): Boolean = {
    if (point.x < x) return false
    if (point.y < y) return false

    if (point.x > endPointX) return false
    if (point.y > endPointY) return false

    true
  }

  def containsRect(rect: Rect): Boolean = {
    if (rect.x < x) return false
    if (rect.y < y) return false

    if (rect.endPointX > endPointX) return false
    if (rect.endPointY > endPointY) return false

    true
  }

  def overlapRect(rect: Rect): Boolean = {
    if (rect.endPointX < x) return false
    if (rect.endPointY < y) return false

    if (rect.x > endPointX) return false
    if (rect.y > endPointY) return false

    true
  }

  def width: Double = size.x
  def height: Double = size.y

  def x: Double = start.x
  def y: Double = start.y

  def midpoint: Vector2 = start / 2
  def midpointX: Double = midpoint.x
  def midpointY: Double = midpoint.y

  def endPoint: Vector2 = start + size
  def endPointX: Double = endPoint.x
  def endPointY: Double = endPoint.y
}
