package battleship.server

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import battleship.client.{ClientGuardian, GameServer}
import battleship.model.{ClientProfileModel, GameRoomModel}
import battleship.utils.MyServiceKeys.GameRoomKey
import battleship.utils.SerializableCommand
import scalafx.collections.ObservableBuffer

object GameRoom {
  sealed trait Command extends SerializableCommand

  final case class SendModelTo(index : Int, totalProfiles : Int, whom: ActorRef[battleship.server.ServerLobby.Command]) extends Command
  final case class HandleJoinRequest(clientProfileModel: ClientProfileModel) extends Command
  final case class HandleLeaveRequest(clientProfileModel: ClientProfileModel) extends Command
  final case class TerminateRoom() extends Command

  // Added min/max players for future scalability
  def apply(gameRoom: GameRoomModel, serverLobby : ActorRef[ServerLobby.Command], minPlayers : Int = 2, maxPlayers : Int = 2): Behavior[Command] = Behaviors.setup { context =>
    gameRoom.actorRef = context.self

    val clientProfiles : ObservableBuffer[ClientProfileModel] = new ObservableBuffer[ClientProfileModel]()
    var gameStarted = false
    clientProfiles onChange {
      if (clientProfiles.length == minPlayers) {
        // start game
        gameStarted = true
        val gameServer = context.spawnAnonymous(GameServer())
        clientProfiles.indices foreach { index =>
          clientProfiles(index).actorRef ! ClientGuardian.StartGame(index, gameServer)
        }
//      } else if (gameStarted && clientProfiles.length < minPlayers) {
        // end game earlier
      }
    }

    context.system.receptionist ! Receptionist.Register(GameRoomKey, context.self)

    def handleCommonCommands(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case SendModelTo(index, totalModels, whom) =>
        whom ! ServerLobby.AddGameRoom(index, totalModels, gameRoom)
        Behaviors.same
      case HandleLeaveRequest(clientProfileModel) =>
        gameRoom.clientUniqueIds -= clientProfileModel.uniqueId
        println(gameRoom.clientUniqueIds)
        if (gameRoom.clientUniqueIds.isEmpty) {
          // Delete room if it's empty
          Behaviors.stopped
        } else {
          Behaviors.same
        }
      case TerminateRoom() =>
        Behaviors.stopped
      case _ =>
        Behaviors.unhandled
    }

    def activeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case HandleJoinRequest(clientProfileModel) =>
        clientProfileModel.actorRef ! ClientGuardian.JoinGameRoom(gameRoom.idName, context.self)
        context.watchWith(clientProfileModel.actorRef, HandleLeaveRequest(clientProfileModel))

        gameRoom.clientUniqueIds += clientProfileModel.uniqueId
        clientProfiles += clientProfileModel
        serverLobby ! ServerLobby.UpdateRooms()

        Behaviors.same
      case _ => handleCommonCommands(context, message)
    }

    Behaviors receive activeBehaviour
  }
}
