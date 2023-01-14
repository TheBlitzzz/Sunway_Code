package battleship.model

import akka.actor.typed.ActorRef
import battleship.server.{ServerGuardian, ServerLobby}

import java.time.{Instant, ZoneId, ZonedDateTime}
import java.time.format.DateTimeFormatter

case class ServerProfileModel(ipAddress: String, port: Int, timeJoined: Long, actorRef: ActorRef[ServerGuardian.Command]) {
  var lobbyRef : ActorRef[ServerLobby.Command] = _

  def timeJoinedString : String = {
    // https://stackoverflow.com/questions/42203276/convert-current-time-in-milliseconds-to-date-time-format-in-scala/42203921

    // First, convert to date time object
    val instant = Instant.ofEpochMilli(timeJoined)
    val zonedDateTimeUtc = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"))

    // Then formate the string
    val dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME
    val zonedDateTime = dateTimeFormatter.format(zonedDateTimeUtc)

    zonedDateTime
  }

  val ipAddressWithPort : String = s"$ipAddress:$port"
}
