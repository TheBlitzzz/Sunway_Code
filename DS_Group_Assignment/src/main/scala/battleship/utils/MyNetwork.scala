package battleship.utils

import scalafx.application.Platform
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.ComboBox

import java.net.{InetAddress, NetworkInterface}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

object MyNetwork {
  def findNetworkAddresses() : Future[Traversable[InetAddress]] = Future {
    var networkAddresses = new Array[InetAddress](0)

    var threads = new Array[Future[Any]](0)
    NetworkInterface.getNetworkInterfaces.asIterator.forEachRemaining(networkInterface => {
      // filters out 127.0.0.1 and inactive interfaces
      if (!networkInterface.isLoopback && networkInterface.isUp) {
        threads :+= Future { networkInterface.getInetAddresses.asIterator.forEachRemaining(networkAddress => {
          if (networkAddress.isSiteLocalAddress) {
            networkAddresses :+= networkAddress
          }
        }) }
      }
    })

    // Wait for everything to finish before returning
    threads.foreach(x => Await.ready(x, Duration.Inf))
    networkAddresses.toSet
  }

  def populateComboBoxWithNetworkInterfaces(observableBuffer: ObservableBuffer[String], targetComboBox: ComboBox[String]): Unit = {
    findNetworkAddresses().onComplete {
      case Success(networkAddresses) =>
        Platform.runLater {
          observableBuffer.clear()
          networkAddresses.foreach(address => {
            observableBuffer.append(address.getHostAddress)
          })
          targetComboBox.selectionModel.value.select(0)
        }
      case Failure(exception) =>
        println(s"Warning : Failed to get network interfaces : $exception")
    }
  }
}
