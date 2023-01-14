package battleship.utils.maths

import scala.language.implicitConversions

object Vector2 {
  implicit def toVector2Int(value: Vector2): Vector2Int = Vector2Int(value.x.toInt, value.y.toInt)
  implicit def tupleToVector2(value: (Double, Double)): Vector2 = Vector2(value._1, value._2)

  val zero : Vector2 = Vector2(0, 0)
  val one : Vector2 = Vector2(1, 1)
  val minusOne : Vector2 = Vector2(-1, -1)
  val left : Vector2 = Vector2(-1, 0)
  val right : Vector2 = Vector2(1, 0)
  val up : Vector2 = Vector2(0, -1)
  val down : Vector2 = Vector2(0, 1)
}

case class Vector2(x : Double, y : Double) {
  def *(multiplier: Double): Vector2 = Vector2(x * multiplier, y * multiplier)
  def *(value: Vector2) : Vector2 = Vector2(x * value.x, y * value.y)
  def *(value: Vector2Int) : Vector2 = Vector2(x * value.x, y * value.y)

  def /(multiplier: Double): Vector2 = Vector2(x / multiplier, y / multiplier)
  def /(value: Vector2) : Vector2 = Vector2(x / value.x, y / value.y)
  def /(value: Vector2Int) : Vector2 = Vector2(x / value.x, y / value.y)

  def +(value: Vector2) : Vector2 = Vector2(x + value.x, y + value.y)
  def +(value: Vector2Int) : Vector2 = Vector2(x + value.x, y + value.y)

  def -(value: Vector2) : Vector2 = Vector2(x - value.x, y - value.y)
  def -(value: Vector2Int) : Vector2 = Vector2(x - value.x, y - value.y)
}
