package battleship.server

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import battleship.client.ClientGuardian
import battleship.model.{ClientProfileModel, GameRoomModel, ServerProfileModel}
import battleship.utils.MyServiceKeys.{ClientGuardianKey, GameRoomKey, ServerGuardianKey}
import battleship.utils.SerializableCommand
import scalafx.collections.ObservableBuffer

import scala.language.higherKinds

object ServerLobby {
  sealed trait Command extends SerializableCommand

  sealed trait ManageServersCommand extends Command
  final case class AddServerProfile(index: Int, totalModels: Int, model: ServerProfileModel) extends ManageServersCommand
  final case class UpdateServers() extends ManageServersCommand

  sealed trait ManageClientsCommand extends Command
  final case class AddClientProfile(index: Int, totalModels: Int, model: ClientProfileModel) extends ManageClientsCommand
  final case class UpdateClients() extends ManageClientsCommand

  sealed trait ManageGameRoomsCommand extends Command
  final case class AddGameRoom(index: Int, totalModels: Int, model: GameRoomModel) extends ManageGameRoomsCommand
  final case class UpdateRooms() extends ManageGameRoomsCommand
  final case class HandleJoinRequest(gameRoomName: String, whom: ClientProfileModel) extends ManageGameRoomsCommand

  final case class RespondWithLobbyData(whom: ClientProfileModel) extends Command

  private final case class ReceptionistEventResponse(listing: Receptionist.Listing) extends Command

  val serverProfiles: ObservableBuffer[Option[ServerProfileModel]] = new ObservableBuffer[Option[ServerProfileModel]]()
  var clientProfilesLastUpdate : Long = 0
  val clientProfiles: ObservableBuffer[Option[ClientProfileModel]] = new ObservableBuffer[Option[ClientProfileModel]]()
  clientProfiles onChange {
    clientProfilesLastUpdate = System.currentTimeMillis()
  }
  var gameRoomsLastUpdate : Long = 0
  val gameRooms: ObservableBuffer[Option[GameRoomModel]] = new ObservableBuffer[Option[GameRoomModel]]()
  gameRooms onChange {
    gameRoomsLastUpdate = System.currentTimeMillis()
  }

  def apply(profile: ServerProfileModel): Behavior[Command] = Behaviors.setup { context =>
    profile.lobbyRef = context.self

    val receptionistResponseAdapter = context.messageAdapter[Receptionist.Listing](ReceptionistEventResponse.apply)
    context.system.receptionist ! Receptionist.Subscribe(ServerGuardianKey, receptionistResponseAdapter)
    context.system.receptionist ! Receptionist.Subscribe(ClientGuardianKey, receptionistResponseAdapter)
    context.system.receptionist ! Receptionist.Subscribe(GameRoomKey, receptionistResponseAdapter)

    def activeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = {
      message match {
        case RespondWithLobbyData(whom) =>
          whom.actorRef ! ClientGuardian.ReceiveLobbyData(clientProfiles.toArray, clientProfilesLastUpdate, gameRooms.toArray, gameRoomsLastUpdate)
          Behaviors.same
        case message: ManageServersCommand => handleManageServersCommand(context, message)
        case message: ManageClientsCommand => handleManageClientsCommand(context, message)
        case message: ManageGameRoomsCommand => handleManageGameRoomsCommand(context, message)
        case ReceptionistEventResponse(listing) => handleReceptionistListing(context, listing)
        case _ => Behaviors.unhandled
      }
    }

    def handleManageServersCommand(context: ActorContext[Command], message: ManageServersCommand): Behavior[Command] = message match {
      case AddServerProfile(index, totalModels, model) =>
        addModelTo(serverProfiles, index, totalModels, model)
        Behaviors.same
      case UpdateServers() =>
        context.system.receptionist ! Receptionist.find(ServerGuardianKey, receptionistResponseAdapter)
        Behaviors.same
    }
    def handleManageClientsCommand(context: ActorContext[Command], message: ManageClientsCommand): Behavior[Command] = message match {
      case AddClientProfile(index, totalModels, model) =>
        addModelTo(clientProfiles, index, totalModels, model)
        Behaviors.same
      case UpdateClients() =>
        context.system.receptionist ! Receptionist.find(ClientGuardianKey, receptionistResponseAdapter)
        Behaviors.same
    }
    def handleManageGameRoomsCommand(context: ActorContext[Command], message: ManageGameRoomsCommand): Behavior[Command] = message match {
      case AddGameRoom(index, totalModels, model) =>
        addModelTo(gameRooms, index, totalModels, model)
        println(gameRooms)
        Behaviors.same
      case UpdateRooms() =>
        context.system.receptionist ! Receptionist.find(GameRoomKey, receptionistResponseAdapter)
        Behaviors.same
      case HandleJoinRequest(gameRoomName, whom) =>
        val gameRoomActor = gameRooms find {
          case Some(value) => value.idName == gameRoomName
          case None => false
        } match {
          case Some(value) =>
            value.get.actorRef
          case None =>
            context.spawnAnonymous(GameRoom(GameRoomModel(gameRoomName, profile.ipAddressWithPort),  context.self))
        }
        gameRoomActor ! GameRoom.HandleJoinRequest(whom)
        Behaviors.same
    }
    def handleReceptionistListing(context: ActorContext[Command], listing: Receptionist.Listing): Behavior[Command] = listing match {
      case ServerGuardianKey.Listing(listings) =>
        foreachElemInListing(listings, clientProfiles.clear, (counter, totalElems) => ServerGuardian.SendModelTo(counter, totalElems, context.self))
        Behaviors.same
      case ClientGuardianKey.Listing(listings) =>
        foreachElemInListing(listings, clientProfiles.clear, { (counter, totalElems) =>
          var assignedServerIndex = counter % serverProfiles.length
          var assignedServer : ActorRef[Command] = null
          while (assignedServer == null) {
            serverProfiles(assignedServerIndex) match {
              case Some(serverProfile) =>
                assignedServer = serverProfile.lobbyRef
              case None =>
                assignedServerIndex += 1
            }
          }
          ClientGuardian.SendModelTo(counter, totalElems, context.self, assignedServer)
        })
        Behaviors.same
      case GameRoomKey.Listing(listings) =>
        foreachElemInListing(listings, gameRooms.clear, (counter, totalElems) => GameRoom.SendModelTo(counter, totalElems, context.self))
        Behaviors.same
    }

    Behaviors receive activeBehaviour
  }

  private def addModelTo[T](modelBuffer: ObservableBuffer[Option[T]], index: Int, totalElements: Int, value: T): Unit = {
    while (modelBuffer.length < totalElements) {
      modelBuffer += None
    }
    if (modelBuffer.length > totalElements) {
      modelBuffer.trimEnd(modelBuffer.length - totalElements)
    }

    modelBuffer(index) = Some(value)
  }
  private def foreachElemInListing[T](listings: Set[ActorRef[T]], onEmpty : () => Unit, f : (Int, Int) => T) : Unit = {
    val totalElem = listings.size
    if (totalElem == 0) {
      onEmpty()
    } else {
      var counter = 0
      listings.foreach(elem => {
        elem ! f(counter, totalElem)
        counter += 1
      })
    }
  }
}
