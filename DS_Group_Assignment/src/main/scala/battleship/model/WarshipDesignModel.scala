package battleship.model

import battleship.utils.SerializableCommand
import battleship.utils.maths.Vector2Int
import javafx.scene.image.{Image => jfxImage}
import scalafx.Includes._
import scalafx.scene.image.Image

/**
 * The type of warship, such as a battleship or destroyer
 */
case class WarshipDesignModel(warshipName: String, shipWidth: Int, shipLength: Int) {
  val shipSize: Vector2Int = Vector2Int(shipWidth, shipLength)

  def maxHealth: Int = shipWidth * shipLength

  def fileName: String = s"warship_${warshipName.toLowerCase.replace(" ", "_")}"
  def createImage(): Image = new jfxImage(getClass.getResourceAsStream(s"/battleship/client/images/$fileName.png"))
}

object WarshipDesignModel {
  val defaultWarships : Array[WarshipDesignModel] = Array(WarshipDesignModel.Destroyer(), WarshipDesignModel.Cruiser(), WarshipDesignModel.Battleship(), WarshipDesignModel.Submarine(), WarshipDesignModel.AircraftCarrier())
  val shortDemo : Array[WarshipDesignModel] = Array(WarshipDesignModel.Destroyer(), Cruiser())

  def Destroyer() : WarshipDesignModel = WarshipDesignModel("Destroyer", 1, 2)
  def Cruiser() : WarshipDesignModel = WarshipDesignModel("Cruiser", 1, 3)
  def Submarine() : WarshipDesignModel = WarshipDesignModel("Submarine", 1, 3)
  def Battleship() : WarshipDesignModel = WarshipDesignModel("Battleship", 1, 4)
  def AircraftCarrier() : WarshipDesignModel = WarshipDesignModel("Aircraft Carrier", 2, 5)
}
