package battleship.utils

import com.typesafe.config.{Config, ConfigFactory}

object MyConfigurations {
  val defaultConfig : Config = ConfigFactory.load()
  val emptyConfig : Config = ConfigFactory.parseString("")

  def overridePort(port: Int) : Config = {
    ConfigFactory.parseString(s"""
      akka.remote.artery.canonical.port = $port
    """).withFallback(defaultConfig)
  }

  implicit class ConfigChain (val target : Config = defaultConfig) {
    var systemName = "Battleship"

    def withDefault(): Config = {
      target.withFallback(defaultConfig)
    }

    def withCluster() : Config = {
      ConfigFactory.parseString(
        s"""
            akka.remote.artery.transport = tcp
            akka.actor.provider = "cluster"
            akka.cluster.downing-provider-class = "akka.cluster.sbr.SplitBrainResolverProvider"
      """).withFallback(target)
    }

    // https://doc.akka.io/docs/akka-management/current/bootstrap/local-config.html
    def withPort(port: Int): Config = {
      ConfigFactory.parseString(
        s"""
           akka.remote.artery.canonical.port = $port
      """).withFallback(target)
    }
    def withHostname(hostname: String) : Config = {
      ConfigFactory.parseString(
        s"""
            akka.remote.artery.canonical.hostname = $hostname
      """).withFallback(target)
    }

    def withSystemName(newSystemName: String) : Config = {
      systemName = newSystemName
      target
    }

    def withSeedNode(address: String) : Config = {
      ConfigFactory.parseString(
        s"""
            akka.cluster.seed-nodes = ["akka://$systemName@$address"]
          """).withFallback(target)
    }
  }
}
