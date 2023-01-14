package battleship.server

import akka.actor.typed.receptionist.Receptionist
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.cluster.ClusterEvent.MemberEvent
import akka.cluster.typed.{Cluster, Subscribe}
import battleship.model.ServerProfileModel
import battleship.utils.MyServiceKeys.ServerGuardianKey
import battleship.utils.SerializableCommand

object ServerGuardian {

  sealed trait Command extends SerializableCommand

  final case class StartLobby(cluster: Cluster) extends Command

  final case class DebugLog(logMsg : String = "Hello") extends Command
  final case class TerminateServer() extends Command
  final case class SendModelTo(index : Int, totalProfiles : Int, whom: ActorRef[ServerLobby.Command]) extends Command

  def apply(ipAddress : String, port : Int, timeJoined : Long): Behavior[Command] = Behaviors.setup { context =>
    val profile = ServerProfileModel(ipAddress, port, timeJoined, context.self)

    context.system.receptionist ! Receptionist.Register(ServerGuardianKey, context.self)

    val serverLobby = context.spawnAnonymous(ServerLobby(profile))

    val clusterRef = context.spawnAnonymous(MyCluster())

    def activeBehaviour(context: ActorContext[Command], message: Command): Behavior[Command] = {
      message match {
        case DebugLog(logMsg : String) =>
          context.log.info(logMsg)
          Behaviors.same
        case StartLobby(cluster) =>
          cluster.subscriptions ! Subscribe(clusterRef, classOf[MemberEvent])
          Behaviors.same
        case SendModelTo(index : Int, totalModels : Int, whom : ActorRef[ServerLobby.Command]) =>
          whom ! ServerLobby.AddServerProfile(index, totalModels, profile)
          Behaviors.same
        case TerminateServer() =>
          Behaviors.stopped
        case _ => Behaviors.unhandled
      }
    }

    Behaviors.receive(activeBehaviour)
  }
}
