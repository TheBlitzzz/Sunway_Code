package battleship.client

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import battleship.model.PlayerModel
import battleship.utils.SerializableCommand
import battleship.utils.maths.Vector2Int


object GameClient {

  sealed trait Command extends SerializableCommand

  final case class InitWarships(opponentRef: ActorRef[GameClient.Command]) extends Command
  final case class NotifyInitWarshipsFinished() extends Command

  final case class EnterPlacementEditorMode() extends Command
  final case class NotifyPlacementMapChanges() extends Command
  final case class NotifyReadyToStartCombat() extends Command

  final case class EnterCombatMode() extends Command
  final case class NotifyFiredAtOpponent(cellPosition: Vector2Int) extends Command
  final case class UpdateHitMap(playerIndex: Int, cellPosition: Vector2Int, hitValue : Int) extends Command
  final case class UpdateWarshipShipHealth(warshipIndex: Int, warshipNewHealth: Int) extends Command
  final case class NotifyPlayerLost(playerIndex: Int) extends Command
  final case class EndGame() extends Command

  final case class OverwritePlayerModel(value: PlayerModel) extends Command
  final case class Terminate(terminateSignal: Int) extends Command
  final case class OnOpponentOrServerLeft() extends Command

  def apply(player: PlayerModel, gameServer: ActorRef[GameServer.Command]): Behavior[Command] = Behaviors.setup { context =>
    player.actorRef = context.self
    gameServer ! GameServer.HandlePlayerJoined(player)
    context.watchWith(gameServer, OnOpponentOrServerLeft())

    def handleCommonCommands(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case OverwritePlayerModel(value) =>
        if (value.playerIndex == player.playerIndex) {
          player.overwriteWith(value)
        } else if (value.playerIndex == player.opponent.playerIndex) {
          player.opponent.overwriteWith(value)
        }
        Behaviors.same
      case OnOpponentOrServerLeft() =>
        player.playerState = PlayerModel.OpponentOrServerLeft()
        context.self ! Terminate(-1)
        Behaviors.same
      case Terminate(terminateSignal) =>
        terminateSignal match {
          case -1 =>
          // Something went wrong in the cluster
        }
        Behaviors.stopped
      case _ => Behaviors.unhandled
    }

    def placementEditorModeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = {
      message match {
        case NotifyPlacementMapChanges() =>
          gameServer ! GameServer.UpdatePlayerPlacementMap(player.playerIndex, player.warshipPlacementMap)
          gameServer ! GameServer.UpdatePlayerWarships(player.playerIndex, player.warships)
          Behaviors.same
        case NotifyReadyToStartCombat() =>
          player.playerState = PlayerModel.ReadyToStart()
          gameServer ! GameServer.UpdatePlayerState(player.playerIndex, player.playerState)
          Behaviors.receive(activeBehaviour)
        case _ => handleCommonCommands(context, message)
      }
    }

    def combatModeBehavior(context: ActorContext[Command], message: Command): Behavior[Command] = {
      message match {
        case NotifyFiredAtOpponent(cellPosition) =>
          // the shootAt method will assign the hitStatus value to indicate the player's turn is over
          gameServer ! GameServer.UpdatePlayerHitMap(player.playerIndex, player.opponent.playerIndex, cellPosition)
          Behaviors.same
        case UpdateHitMap(playerIndex, cellPosition, hitValue) =>
          if (playerIndex == player.playerIndex) {
            // The hitStatus is cleared in the view controller
            player.opponentHitStatus = hitValue
            player.hitMap(cellPosition) = hitValue
          } else if (playerIndex == player.opponent.playerIndex) {
            // Update the value
            player.opponent.hitMap(cellPosition) = hitValue
          }
          Behaviors.same
        case UpdateWarshipShipHealth(warshipIndex, warshipNewHealth) =>
          player.warships(warshipIndex).healthPoints = warshipNewHealth
          Behaviors.same
        case NotifyPlayerLost(playerIndex) =>
          if (player.playerIndex == playerIndex) {
            player.playerState = PlayerModel.Defeated()
          } else if (player.opponent.playerIndex == playerIndex) {
            player.opponent.playerState = PlayerModel.Defeated()
          }
          context.self ! Terminate(0)
          Behaviors.same
        case _ => handleCommonCommands(context, message)
      }
    }

    def activeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case InitWarships(opponentRef) =>
        player.playerState = PlayerModel.ReadyToInit()
        player.opponent.actorRef = opponentRef
        context.watchWith(opponentRef, OnOpponentOrServerLeft())
        gameServer ! GameServer.UpdatePlayerState(player.playerIndex, player.playerState)
        Behaviors.same
      case NotifyInitWarshipsFinished() =>
        // Warships initialised, ready to edit placement
        player.playerState = PlayerModel.ReadyToEnterPlacementEditorMode()
        gameServer ! GameServer.UpdatePlayerState(player.playerIndex, player.playerState)

        gameServer ! GameServer.UpdatePlayerWarships(player.playerIndex, player.warships)
        gameServer ! GameServer.UpdatePlayerPlacementMap(player.playerIndex, player.warshipPlacementMap)
        Behaviors.same
      case EnterPlacementEditorMode() =>
        player.playerState = PlayerModel.PlacementEditorMode()
        gameServer ! GameServer.UpdatePlayerState(player.playerIndex, player.playerState)
        Behaviors receive placementEditorModeBehaviour
      case EnterCombatMode() =>
        player.playerState = PlayerModel.CombatMode()
        gameServer ! GameServer.UpdatePlayerState(player.playerIndex, player.playerState)
        Behaviors receive combatModeBehavior
      case _ => handleCommonCommands(context, message)
    }

    Behaviors receive activeBehaviour
  }
}
