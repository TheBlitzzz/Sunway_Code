package battleship.server

import akka.cluster.Member
import scalafx.collections.ObservableBuffer

class ServerMem() {
  val membersInCluster : ObservableBuffer[Member] = new ObservableBuffer[Member]()

  var localAddress = ""
  var clusterAddress = ""
  var serverStarted = false
}
