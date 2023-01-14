package battleship.model

import akka.actor.typed.ActorRef
import battleship.client.GameClient
import battleship.utils.maths.{IntGrid, GridCoordinateSystem, Rect, Vector2Int}

import scala.collection.mutable.ArrayBuffer

object PlayerModel {
  case class PlayerState(index: Int )

  def Awake() : PlayerState = PlayerState(0)
  def ReadyToInit() : PlayerState = PlayerState(1)
  def ReadyToEnterPlacementEditorMode() : PlayerState = PlayerState(2)
  def PlacementEditorMode() : PlayerState = PlayerState(3)
  def ReadyToStart() : PlayerState = PlayerState(4)
  def CombatMode() : PlayerState = PlayerState(5)
  def Defeated() : PlayerState = PlayerState(6)
  def OpponentOrServerLeft() : PlayerState = PlayerState(1000)

//  final case class Awake() extends PlayerState { val index: Int = 0 }
//  final case class ReadyToInit() extends PlayerState { val index: Int = 1 }
//  final case class ReadyToEnterPlacementEditorMode() extends PlayerState { val index: Int = 2 }
//  final case class PlacementEditorMode() extends PlayerState { val index: Int = 3 }
//  final case class ReadyToStart() extends PlayerState { val index: Int = 4 }
//  final case class CombatMode() extends PlayerState { val index: Int = 5 }
//  final case class Defeated() extends PlayerState { val index: Int = 6 }
}

case class PlayerModel(playerIndex: Int, id: String, coordinateSystem: GridCoordinateSystem = GridCoordinateSystem()) {
  var actorRef: ActorRef[GameClient.Command] = _

  var myHitStatus : Int = -1
  var opponentHitStatus : Int = -1

  var playerState: PlayerModel.PlayerState = PlayerModel.Awake()
  var opponent: PlayerModel = _

  val hitMap: IntGrid = new IntGrid(coordinateSystem.dimensions, -1)
  val warshipPlacementMap: IntGrid = new IntGrid(coordinateSystem.dimensions, -1)
  val warships: ArrayBuffer[WarshipModel] = new ArrayBuffer[WarshipModel]()

  def copy(): PlayerModel = {
    val copied = PlayerModel(playerIndex, id, coordinateSystem)
    copied.actorRef = actorRef
    copied.playerState = playerState
    copied.hitMap.copy(hitMap)
    copied.warshipPlacementMap.copy(warshipPlacementMap)
    copied.warships.clear()
    copied.warships ++= warships
    copied
  }

  def overwriteWith(value: PlayerModel): Unit = {
    playerState = value.playerState
    hitMap.copy(value.hitMap)
    warshipPlacementMap.copy(value.warshipPlacementMap)
    warships.clear()
    warships ++= value.warships
  }

  def initWarships(warshipDesigns: Array[WarshipDesignModel]): Unit = {
    warships.clear()
    warships ++= warshipDesigns.indices.map { index => new WarshipModel(index, warshipDesigns(index)) }

    // Find new position for the warship
    warships.foreach { warship =>
      warship.setTransform(Vector2Int(2, warship.indexId * 2 + 1), 90)
      warshipPlacementMap.updateInRect(warship.placementRect, warship.indexId)
    }

    actorRef ! GameClient.NotifyInitWarshipsFinished()
  }

  def placeWarshipAt(warshipIndex: Int, newPlacementRect: Rect, orientation: Double): Boolean = {
    val warship = warships(warshipIndex)
    if (!validateShipPlacement(warshipIndex, newPlacementRect)) return false

    // Remove the warship's previous placement
    // No need to call actor ref because it is only called once after updating the placement
    // During game, it is automatically removed when the ship is destroyed
    removeWarshipPlacement(warshipIndex)

    // Calculate the warship's new placement
    warship.setTransform(newPlacementRect.start, orientation)

    // Mark the warship's new placement on the map
    warshipPlacementMap.updateInRect(newPlacementRect, warshipIndex)
    actorRef ! GameClient.NotifyPlacementMapChanges()
    true
  }

  def notifyReadyToStartCombat(): Unit = {
    // In multiplayer scenario, the dummy player doesn't have an actor ref
    if (actorRef == null) return
    actorRef ! GameClient.NotifyReadyToStartCombat()
  }

  def removeWarshipPlacement(warshipIndex: Int): Unit = {
    val warship = warships(warshipIndex)
    // Get its current position
    val oldPlacementRect = warship.placementRect
    // Loop through the grid to remove the ref
    warshipPlacementMap.updateInRect(oldPlacementRect, -1)
  }

  protected def validateShipPlacement(warshipIndex: Int, newPlacementRect: Rect): Boolean = {
    val warship = warships(warshipIndex)
    if (!Rect(coordinateSystem.dimensions).containsRect(newPlacementRect)) return false

    for (rowIndex <- newPlacementRect.x.toInt until newPlacementRect.endPointX.toInt) {
      for (colIndex <- newPlacementRect.y.toInt until newPlacementRect.endPointY.toInt) {
        val warshipOnGridIndex = warshipPlacementMap(rowIndex, colIndex)
        if (warshipOnGridIndex > -1 && warshipOnGridIndex != warship.indexId) return false
      }
    }

    true
  }

  def shootAt(position: Vector2Int): Int = {
    val hitValue = opponent.hitMap(position)
    if (hitValue == -1) {
      actorRef ! GameClient.NotifyFiredAtOpponent(position)
      if (opponent.warshipPlacementMap(position) > -1) {
        myHitStatus = 1
        return 1
      } else {
        myHitStatus = 0
        return 0
      }
    }
    -1
  }
}
