package battleship.utils

import akka.actor.typed.receptionist.ServiceKey
import battleship.client.ClientGuardian
import battleship.server.{GameRoom, ServerGuardian}

object MyServiceKeys {
  val ServerGuardianKey: ServiceKey[ServerGuardian.Command] = ServiceKey[ServerGuardian.Command]("Server Guardian Service")
  val ClientGuardianKey: ServiceKey[ClientGuardian.Command] = ServiceKey[ClientGuardian.Command]("Client Guardian Service")
  val GameRoomKey: ServiceKey[GameRoom.Command] = ServiceKey[GameRoom.Command]("Game Room Service")
}
