package billSplitter.util

import scala.math.BigDecimal.double2bigDecimal
import scala.util.{Failure, Success, Try}

object InputValidationUtil {
  def nullChecking (x : String): Boolean = x == null || x.isEmpty

  def parseDoubleChecking(x : String) : Boolean = {
    parseToDouble(x) match {
      case Success(x) => true
      case Failure(exception) => false
    }
  }

  def parseToDouble(x : String) : Try[Double] = {
    Try(x.toDouble)
  }

  def doubleToString(x: Double, decimalPlaces: Int = 2) : String = {
    // https://www.reddit.com/r/scala/comments/hbtplv/rounding_a_decimal_to_2_decimal_places/
    x.setScale(2, BigDecimal.RoundingMode.CEILING).toString
  }
}
