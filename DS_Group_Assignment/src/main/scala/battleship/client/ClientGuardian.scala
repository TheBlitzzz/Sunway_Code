package battleship.client

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.{ClusterEvent, Member}
import akka.cluster.ClusterEvent.{ClusterDomainEvent, ReachabilityEvent}
import akka.cluster.typed.{Cluster, Subscribe}
import battleship.model.{ClientProfileModel, GameRoomModel, PlayerModel}
import battleship.server.{GameRoom, ServerLobby}
import battleship.utils.MyServiceKeys.ClientGuardianKey
import battleship.utils.SerializableCommand
import scalafx.collections.ObservableBuffer

object ClientGuardian {

  sealed trait Command extends SerializableCommand
  final case class SendModelTo(index : Int, totalProfiles : Int, requestFrom: ActorRef[battleship.server.ServerLobby.Command], assignedServer: ActorRef[battleship.server.ServerLobby.Command]) extends Command

  sealed trait LobbyDataCommand extends Command
  final case class RequestLobbyData() extends LobbyDataCommand
  final case class ReceiveLobbyData(clientsInLobbyData: Array[Option[ClientProfileModel]], clientsLastUpdate: Long, roomsInLobbyData : Array[Option[GameRoomModel]], roomsLastUpdate: Long) extends LobbyDataCommand

  sealed trait GameRoomCommand extends Command
  final case class RequestToJoinGameRoom(gameRoomName: String) extends GameRoomCommand
  final case class JoinGameRoom(gameRoomName: String, gameRoomActor : ActorRef[GameRoom.Command]) extends GameRoomCommand
  final case class StartGame(playerIndex: Int, gameServer: ActorRef[GameServer.Command]) extends GameRoomCommand
  final case class LeaveGameRoom() extends GameRoomCommand
  final case class CreateMultiPlayerGame(player: PlayerModel) extends Command

  final case class CreateSinglePlayerGame(players: Array[PlayerModel]) extends Command

  var clientsInLobbyLastUpdate : Long = 0
  val clientsInLobby: ObservableBuffer[Option[ClientProfileModel]] = new ObservableBuffer[Option[ClientProfileModel]]()
  var roomsInLobbyLastUpdate : Long = 0
  val roomsInLobby : ObservableBuffer[Option[GameRoomModel]] = new ObservableBuffer[Option[GameRoomModel]]()

  val reachableMembers: ObservableBuffer[Member] = new ObservableBuffer[Member]()
  val unreachableMembers: ObservableBuffer[Member] = new ObservableBuffer[Member]()

  def apply(profile: ClientProfileModel, cluster: Option[Cluster] = None): Behavior[Command] = Behaviors.setup { context =>
    profile.actorRef = context.self
    context.system.receptionist ! Receptionist.Register(ClientGuardianKey, context.self)

    var myGameServer : ActorRef[GameServer.Command] = null

//    val clusterResponseAdapter = context.messageAdapter[ReachabilityEvent](ClusterEventResponse.apply)
//    cluster foreach { cluster =>
//      cluster.subscriptions ! Subscribe(clusterResponseAdapter, classOf[ReachabilityEvent])
//    }

    def onlineBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case SendModelTo(index, totalModels, requestFrom, assignedServer) =>
        profile.serverLobbyRef = Some(assignedServer)
        requestFrom ! battleship.server.ServerLobby.AddClientProfile(index, totalModels, profile)
        context.self ! RequestLobbyData()
        Behaviors.same
      case CreateMultiPlayerGame(player) =>
        if (myGameServer != null) {
          context.spawnAnonymous(GameClient(player, myGameServer))
        }
        Behaviors.same
      case message: LobbyDataCommand => handleLobbyDataCommand(context, message)
      case message: GameRoomCommand => handleGameRoomCommand(context, message)
      case _ => Behaviors.unhandled
    }

    def offlineBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = message match {
      case CreateSinglePlayerGame(players) =>
        val gameServerActor = context.spawnAnonymous(GameServer())
        players foreach { player =>
          context.spawnAnonymous(GameClient(player, gameServerActor))
        }
        Behaviors.same
      case _ => Behaviors.unhandled
    }

    def handleLobbyDataCommand(context: ActorContext[Command], message: LobbyDataCommand): Behavior[Command] = message match {
      case RequestLobbyData() =>
        profile.serverLobbyRef foreach { serverLobby =>
          serverLobby ! ServerLobby.RespondWithLobbyData(profile)
        }
        Behaviors.same
      case ReceiveLobbyData(clientsInLobbyData, clientsLastUpdate, roomsInLobbyData, roomsLastUpdate) =>
        if (clientsInLobbyLastUpdate < clientsLastUpdate) {
          clientsInLobby.clear()
          clientsInLobby ++= clientsInLobbyData
          clientsInLobbyLastUpdate = clientsLastUpdate
        }

        if (roomsInLobbyLastUpdate < roomsLastUpdate) {
          roomsInLobby.clear()
          roomsInLobby ++= roomsInLobbyData
          roomsInLobbyLastUpdate = roomsLastUpdate
        }
        Behaviors.same
    }
    def handleGameRoomCommand(context: ActorContext[Command], message: GameRoomCommand): Behavior[Command] = message match {
      case RequestToJoinGameRoom(gameRoomName) =>
        profile.serverLobbyRef foreach { serverLobby =>
          serverLobby ! ServerLobby.HandleJoinRequest(gameRoomName, profile)
        }
        Behaviors.same
      case JoinGameRoom(gameRoomName, gameRoomActor) =>
        profile.serverLobbyRef foreach { serverLobby =>
          profile.joinedRoom = gameRoomName
          profile.gameRoomActor = gameRoomActor
          serverLobby ! ServerLobby.UpdateClients()
        }
        Behaviors.same
      case StartGame(playerIndex, gameServer) =>
        myGameServer = gameServer
        profile.playerModel = Some(PlayerModel(playerIndex, profile.username))
        Behaviors.same
      case LeaveGameRoom() =>
        profile.serverLobbyRef foreach  { serverLobby =>
          profile.joinedRoom = ""
          profile.gameRoomActor ! GameRoom.HandleLeaveRequest(profile)
          serverLobby ! ServerLobby.UpdateClients()
        }
        Behaviors.same
    }

    if (profile.isConnectedToCluster) {
      Behaviors receive onlineBehaviour
    } else {
      Behaviors receive offlineBehaviour
    }
  }
}
