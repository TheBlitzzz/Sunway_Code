package battleship.model

import akka.actor.typed.ActorRef
import battleship.server.GameRoom

import scala.collection.mutable.ArrayBuffer

case class GameRoomModel(idName: String, serverAddress: String) {
  val clientUniqueIds : ArrayBuffer[String] = new ArrayBuffer[String]()

  var actorRef: ActorRef[GameRoom.Command] = _

  def availableToJoin(): Boolean = clientUniqueIds.length < 2
}
