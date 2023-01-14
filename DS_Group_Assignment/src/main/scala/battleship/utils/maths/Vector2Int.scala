package battleship.utils.maths

import scala.language.implicitConversions

object Vector2Int {
  implicit def toVector2(value: Vector2Int): Vector2 = Vector2(value.x, value.y)
  implicit def tupleToVector2Int(value: (Int, Int)): Vector2Int = Vector2Int(value._1, value._2)

  val zero : Vector2Int = Vector2Int(0, 0)
  val one : Vector2Int = Vector2Int(1, 1)
  val minusOne : Vector2Int = Vector2Int(-1, -1)
  val left : Vector2Int = Vector2Int(-1, 0)
  val right : Vector2Int = Vector2Int(1, 0)
  val up : Vector2Int = Vector2Int(0, -1)
  val down : Vector2Int = Vector2Int(0, 1)
}

case class Vector2Int(x : Int, y : Int) {
  def *(multiplier: Int): Vector2Int = Vector2Int(x * multiplier, y * multiplier)
  def *(multiplier: Double): Vector2 = Vector2(x * multiplier, y * multiplier)
  def *(value: Vector2Int) : Vector2Int = Vector2Int(x * value.x, y * value.y)

  def /(multiplier: Int): Vector2Int = Vector2Int(x / multiplier, y / multiplier)
  def /(multiplier: Double): Vector2 = Vector2(x / multiplier, y / multiplier)
  def /(value: Vector2Int) : Vector2Int = Vector2Int(x / value.x, y / value.y)

  def +(value: Vector2Int) : Vector2Int = Vector2Int(x + value.x, y + value.y)
  def -(value: Vector2Int) : Vector2Int = Vector2Int(x - value.x, y - value.y)
}
