package battleship.utils


import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UpdateLoop {
  private var appHasTerminated = false

  def apply(onUpdate : () => Unit): UpdateLoop = {
    val updateLoop = new UpdateLoop()
    updateLoop.onUpdateEvent += onUpdate
    updateLoop
  }

  def notifyAppHasTerminated(): Unit ={
    appHasTerminated = true
  }
}

class UpdateLoop() {
  val onUpdateEvent : ArrayBuffer[() => Unit] = new ArrayBuffer[() => Unit]()

  private var isRunning = false

  private var terminateLoopOnNextRecursion = false
  def terminateLoop(): Unit = {
    if (isRunning) {
      terminateLoopOnNextRecursion = true
    }
  }

  def run(interval: Int = 1000 / 60): Unit = {
    if (isRunning) return

    startLoop(interval)
    isRunning = true
  }

  def append(onUpdate : () => Unit): () => Unit = {
    onUpdateEvent += onUpdate

    () => onUpdateEvent -= onUpdate
  }

  def clearLoop(): Unit = {
    onUpdateEvent.clear()
  }

  private def startLoop(interval: Int): Unit ={
    Future{
      if (!terminateLoopOnNextRecursion && !UpdateLoop.appHasTerminated) {
        onUpdateEvent foreach { onUpdate =>
          onUpdate()
        }
        Thread.sleep(interval)

        startLoop(interval)
      } else {
        isRunning = false
      }
    }
  }
}
