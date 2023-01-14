package battleship.model

import akka.actor.typed.ActorRef
import battleship.client.ClientGuardian
import battleship.server.{GameRoom, ServerLobby}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId, ZonedDateTime}

case class ClientProfileModel(username : String, localIpAddress: String, port: Int, timeJoined: Long, clusterAddress : Option[String] = None) {
  var serverLobbyRef: Option[ActorRef[ServerLobby.Command]] = None
  var joinedRoom : String = ""
  var gameRoomActor : ActorRef[GameRoom.Command] = _
  var actorRef: ActorRef[ClientGuardian.Command] = _

  def isConnectedToCluster :Boolean = clusterAddress match {
    case Some(_) => true
    case None => false
  }

  var playerModel: Option[PlayerModel] = None

  def timeJoinedString : String = {
    // https://stackoverflow.com/questions/42203276/convert-current-time-in-milliseconds-to-date-time-format-in-scala/42203921

    // First, convert to date time object
    val instant = Instant.ofEpochMilli(timeJoined)
    val zonedDateTimeUtc = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

    // Then format the string
    val dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    val zonedDateTime = dateTimeFormatter.format(zonedDateTimeUtc)

    zonedDateTime
  }

  def uniqueId : String = s"$fullLocalIpAddress-$timeJoined"

  def fullLocalIpAddress : String = s"$localIpAddress:$port"
}
