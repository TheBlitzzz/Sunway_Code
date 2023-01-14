package battleship.client

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import battleship.model.{PlayerModel, WarshipModel}
import battleship.utils.SerializableCommand
import battleship.utils.maths.{IntGrid, Rect, Vector2Int}
import scalafx.beans.property.ObjectProperty
import scalafx.collections.ObservableBuffer

import scala.collection.mutable.ArrayBuffer

object GameServer {

  sealed trait Command extends SerializableCommand

  final case class HandlePlayerJoined(playerModel: PlayerModel) extends Command

  final case class UpdatePlayerWarships(playerIndex: Int, warships: ArrayBuffer[WarshipModel]) extends Command
  final case class UpdatePlayerPlacementMap(playerIndex: Int, warshipPlacementMap: IntGrid) extends Command
  final case class UpdatePlayerHitMap(playerIndex: Int, opponentIndex: Int, cellPosition: Vector2Int) extends Command
  final case class UpdatePlayerState(playerIndex: Int, playerState: PlayerModel.PlayerState) extends Command
  final case class HandleFiredAt(playerIndex: Int, cellPosition: Vector2Int, opponentIndex: Int) extends Command

  final case class OnPlayerLeftAbruptly(playerIndex: Int) extends Command
  final case class Terminate(terminateSignal: Int) extends Command

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    val currentTurnPlayerProp: ObjectProperty[Int] = new ObjectProperty[Int]()
    currentTurnPlayerProp.value = 0

    val players = new ObservableBuffer[PlayerModel]()
    val playerStates = new ObservableBuffer[PlayerModel.PlayerState]()

    def overwritePlayerModel(player: PlayerModel): Unit = {
      player.actorRef ! GameClient.OverwritePlayerModel(player)
    }

    def synchronize(): Unit = {
      players foreach { player =>
        players foreach { data => {
          player.actorRef ! GameClient.OverwritePlayerModel(data)
        }}
      }
    }

    players onChange { (buffer, _) =>
      playerStates.clear()
      playerStates ++= buffer map (x => if (x == null) PlayerModel.PlayerState(-1) else x.playerState)
    }
    playerStates onChange { (buffer, _) =>
      // Waiting for all the players to be active
      if ((players count (x => (x != null) && x.playerState == PlayerModel.Awake())) == 2) {
        // start editing
        players(0).actorRef ! GameClient.InitWarships(players(1).actorRef)
        players(1).actorRef ! GameClient.InitWarships(players(0).actorRef)
      } else if ((buffer count (x => x == PlayerModel.ReadyToEnterPlacementEditorMode())) == 2) {
        // start editing
        players foreach (player => {
          player.actorRef ! GameClient.EnterPlacementEditorMode()
        })
      } else if ((buffer count (x => x == PlayerModel.ReadyToStart())) == 2) {
        // start game
        synchronize()
        players foreach (player => {
          player.actorRef ! GameClient.EnterCombatMode()
        })
      }
    }

    var gameOver = false

    def activeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case HandlePlayerJoined(playerModel) =>
        // Clone it because it will create a race condition if using local multiplayer
        while (players.length <= playerModel.playerIndex) {
          players += null
        }
        players(playerModel.playerIndex) = playerModel.copy()
        context.watchWith(playerModel.actorRef, OnPlayerLeftAbruptly(playerModel.playerIndex))

        Behaviors.same
      case UpdatePlayerWarships(playerIndex, warships) =>
        players(playerIndex).warships.clear()
        players(playerIndex).warships ++= warships
        Behaviors.same
      case UpdatePlayerPlacementMap(playerIndex, warshipPlacementMap) =>
        if (players(playerIndex).warshipPlacementMap.dimensions == warshipPlacementMap.dimensions) {
          // if validate pass, no need to tell actor again
          players(playerIndex).warshipPlacementMap.copy(warshipPlacementMap)
        } else {
          // else, give actor correct data
          overwritePlayerModel(players(playerIndex))
        }
        Behaviors.same

      case UpdatePlayerHitMap(playerIndex, opponentIndex, cellPosition) =>
        if (currentTurnPlayerProp.value == playerIndex) {
          val player = players(playerIndex)
          val opponent = players(opponentIndex)
          if (Rect(opponent.hitMap.dimensions).containsPoint(cellPosition)) {
            // Validate if position is within the hit map
            // if validate pass, no need to tell actor again
            val warshipIndex = opponent.warshipPlacementMap(cellPosition)
            val hitValue = if (warshipIndex != -1) {
              // Hit something
              1
            } else {
              // Missed
              0
            }

            opponent.hitMap(cellPosition) = hitValue
            player.actorRef ! GameClient.UpdateHitMap(opponentIndex, cellPosition, hitValue)
            opponent.actorRef ! GameClient.UpdateHitMap(opponentIndex, cellPosition, hitValue)

            if (hitValue == 1) {
              // Handle warship shot
              val warship = opponent.warships(warshipIndex)
              warship.healthPoints -= 1
              if (warship.isDestroyed) {
                opponent.removeWarshipPlacement(warshipIndex)
              }
              opponent.actorRef ! GameClient.UpdateWarshipShipHealth(warshipIndex, warship.healthPoints)

              // Check if player lost condition
              val playerLost = opponent.warships.count { warship => !warship.isDestroyed } == 0
              if (playerLost) {
                gameOver = true
                opponent.actorRef ! GameClient.NotifyPlayerLost(opponentIndex)
                player.actorRef ! GameClient.NotifyPlayerLost(opponentIndex)
                context.self ! Terminate(0)
              }
            }
          } else {
            // else, give actor correct data
            overwritePlayerModel(players(opponentIndex))
          }

          currentTurnPlayerProp.value = opponent.playerIndex
        } else {
          overwritePlayerModel(players(opponentIndex))
        }
        // go to next player
        Behaviors.same
      case UpdatePlayerState(playerIndex, playerState) =>
        players(playerIndex).playerState = playerState
        playerStates(playerIndex) = playerState
        Behaviors.same
      case OnPlayerLeftAbruptly(playerIndex)  =>
        context.self ! Terminate(-1)
        Behaviors.same
      case Terminate(terminateSignal) =>
        terminateSignal match {
          case -1 =>
            // Shutdown server, client already watching their opponent, so no issue
        }
        Behaviors.stopped
      case _ => Behaviors.unhandled
    }

    Behaviors receive activeBehaviour
  }
}
