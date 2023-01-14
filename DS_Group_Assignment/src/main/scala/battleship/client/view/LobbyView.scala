package battleship.client.view

import battleship.client.view.dialogs.{CreateRoomDialogController, RoomInfoDialogController}
import battleship.client.{BattleshipClient, ClientGuardian}
import battleship.model.{ClientProfileModel, PlayerModel}
import battleship.utils.{MyFxml, MyImmutableRecord, UpdateLoop}
import javafx.scene.Parent
import javafx.scene.control.SelectionMode
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property.ObjectProperty
import scalafx.event.ActionEvent
import scalafx.scene.control._
import scalafx.scene.layout.Pane
import scalafxml.core.macros.sfxml

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@sfxml
class LobbyView(val rootPane: Pane,
                val roomTreeTableView: TreeTableView[MyImmutableRecord],
                val roomNameColumn: TreeTableColumn[MyImmutableRecord, String],
                val roomClientUsernameColumn: TreeTableColumn[MyImmutableRecord, String],
                val roomClientIpColumn: TreeTableColumn[MyImmutableRecord, String],
                val createRoomBtn: Button,
                val roomInfoBtn: Button,
                val leaveRoomBtn: Button,
                val serverAddressReadOnlyTextField: TextField,
                val localAddressReadOnlyTextField: TextField,
                val usernameReadOnlyTextField: TextField,
                val joinedRoomReadOnlyTextField: TextField,
                val lobbyEventReadOnlyTextArea: TextArea,
               ) extends LobbyController {

  def handleCreateRoomBtnOnAction(e: ActionEvent): Unit = {
    val roomName = CreateRoomDialogController()
    if (!roomName.isBlank) {
      BattleshipClient.clientSystem foreach { lobbySystem =>
        lobbySystem ! ClientGuardian.RequestToJoinGameRoom(roomName)
      }
      joinedRoomProp.value = roomName
    }
  }

  def handleRoomInfoBtnOnAction(e: ActionEvent): Unit = {
    val roomName = selectedRoomNameProp.value

    val playersInRoom = new ArrayBuffer[ClientProfileModel]()
    ClientGuardian.clientsInLobby.foreach {
      case Some(clientProfileModel) =>
        if (clientProfileModel.joinedRoom == roomName) {
          playersInRoom += clientProfileModel
        }
      case None =>
    }

    println(ClientGuardian.roomsInLobby)
    val roomModel = (ClientGuardian.roomsInLobby find {
      case Some(room) => room.idName == roomName
      case None => false
    }).get.get

    val playerJoined = RoomInfoDialogController(roomModel, playersInRoom, joinedRoomProp.value.isBlank)
    if (playerJoined) {
      BattleshipClient.clientSystem foreach { lobbySystem =>
        lobbySystem ! ClientGuardian.RequestToJoinGameRoom(roomName)
      }
      joinedRoomProp.value = roomName
    }
  }

  def handleLeaveRoomBtnOnAction(e: ActionEvent): Unit ={
    BattleshipClient.clientSystem foreach { lobbySystem =>
      lobbySystem ! ClientGuardian.LeaveGameRoom()
    }
    joinedRoomProp.value = ""
  }

  def handleBackBtnOnAction(e: ActionEvent): Unit = {
    terminateUpdateLoop()
    BattleshipClient.disconnectFromServer()

    val (root, _) = MainMenuController()
    rootPane.getScene.root = root
  }
}

object LobbyController {
  val roomTreeRoot = new TreeItem[MyImmutableRecord](MyImmutableRecord())
  ClientGuardian.clientsInLobby onChange {
    roomTreeRoot.children.clear()
    ClientGuardian.clientsInLobby.foreach {
      case Some(model) =>
        if (!model.joinedRoom.isBlank) {
          val playerLeaf = LobbyController.observe(model)
          roomTreeRoot.children.find(x => x.value.value.stringVal("Room") == model.joinedRoom) match {
            case Some(treeLeaf) =>
              treeLeaf.children.add(new TreeItem[MyImmutableRecord](playerLeaf))
            case None =>
              val newLeaf = new TreeItem[MyImmutableRecord](LobbyController.observe(model.joinedRoom))
              roomTreeRoot.children.add(newLeaf)
              newLeaf.children.add(new TreeItem[MyImmutableRecord](playerLeaf))
          }
        }
      case None =>
    }
  }

  def apply(): (Parent, LobbyController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[LobbyController]("/battleship/client/view/lobby_view.fxml")
    (root, controller)
  }

  def observe(profile: ClientProfileModel): MyImmutableRecord = {
    val usernameCol = ("Player", profile.username)
    val ipAddressCol = ("IP Address", profile.localIpAddress)
    val joinedRoomCol = ("Joined Room", profile.joinedRoom)
    MyImmutableRecord(usernameCol, ipAddressCol, joinedRoomCol)
  }

  def observe(roomName: String): MyImmutableRecord = {
    val roomNameCol = ("Room", roomName)
    MyImmutableRecord(roomNameCol)
  }
}

trait LobbyController {
  val rootPane: Pane
  val roomTreeTableView: TreeTableView[MyImmutableRecord]
  val roomNameColumn: TreeTableColumn[MyImmutableRecord, String]
  val roomClientUsernameColumn: TreeTableColumn[MyImmutableRecord, String]
  val roomClientIpColumn: TreeTableColumn[MyImmutableRecord, String]
  val createRoomBtn: Button
  val roomInfoBtn: Button
  val leaveRoomBtn: Button
  val serverAddressReadOnlyTextField: TextField
  val localAddressReadOnlyTextField: TextField
  val usernameReadOnlyTextField: TextField
  val joinedRoomReadOnlyTextField: TextField
  val lobbyEventReadOnlyTextArea: TextArea

  protected val selectedRoomNameProp : ObjectProperty[String] = new ObjectProperty[String]()
  protected val joinedRoomProp : ObjectProperty[String] = new ObjectProperty[String]()
  joinedRoomProp onChange {
    val roomName = joinedRoomProp.value
    if (joinedRoomProp.value.isBlank) {
      lobbyEventReadOnlyTextArea.text.value = "Select a room to join!"
      joinedRoomReadOnlyTextField.text.value = "Not in a room"
    } else {
      lobbyEventReadOnlyTextArea.text.value = "Joined room! Waiting for match..."
      joinedRoomReadOnlyTextField.text.value = roomName
    }
  }
  joinedRoomProp.value = ""

  MyImmutableRecord.bindToTreeTable(LobbyController.roomTreeRoot, roomTreeTableView,
    roomNameColumn,
    roomClientUsernameColumn,
    roomClientIpColumn,
  )
  roomTreeTableView.selectionModel.value.setSelectionMode(SelectionMode.SINGLE)
  roomTreeTableView.selectionModel.value.selectedItemProperty.onChange((_, _, newVal) => onSelectedRoomChanged(newVal))
  onSelectedRoomChanged(null)

  BattleshipClient.profile match {
    case Some(profile) =>
      serverAddressReadOnlyTextField.text.value = profile.clusterAddress match {
        case Some(clusterAddress) => clusterAddress
        case None => "Not connected to a cluster"
      }
      localAddressReadOnlyTextField.text.value = profile.fullLocalIpAddress
      usernameReadOnlyTextField.text.value = profile.username
    case None =>
      serverAddressReadOnlyTextField.text.value = "Not connected to a cluster"
      localAddressReadOnlyTextField.text.value = "Not connected"
      usernameReadOnlyTextField.text.value = "Anonymous"
  }

  protected def onSelectedRoomChanged(treeItem: TreeItem[MyImmutableRecord]): Unit = {
    if (treeItem == null) {
      selectedRoomNameProp.value = ""
      roomInfoBtn.disable = true
    } else {
      val treeRecord = treeItem.value.value
      val roomName =  if (treeRecord.stringVal("Room").isBlank) {
        treeRecord.stringVal("Joined Room") // is Player
      } else {
        treeRecord.stringVal("Room") // is Room
      }

      selectedRoomNameProp.value = roomName
      roomInfoBtn.disable = false
    }
  }

  private val mainUpdateLoop = UpdateLoop { () =>
    Platform runLater {
      BattleshipClient.profile foreach { profile =>
        if (profile.joinedRoom == "") {
          createRoomBtn.disable = false
          leaveRoomBtn.disable = true
        } else {
          createRoomBtn.disable = true
          leaveRoomBtn.disable = false
        }

        profile.playerModel foreach { playerModel =>
          createRoomBtn.disable = true
          roomInfoBtn.disable = true
          leaveRoomBtn.disable = true
          goToGameView(playerModel)
          terminateUpdateLoop()
        }
      }
    }
  }
  private def goToGameView(playerModel: PlayerModel): Unit = {
    Future {
      val secondsToWait = 3
      for (second <- 0 until secondsToWait) {
        Platform runLater {
          lobbyEventReadOnlyTextArea.text.value = s"Found match! Starting match in ${secondsToWait - second}..."
        }
        Thread.sleep(1000)
      }

      Platform runLater {
        val (root, controller) = GameController()
        rootPane.getScene.root = root
        controller.init(BattleshipClient.stage, playerModel)
      }
    }
  }
  mainUpdateLoop.run(1000 / 20)

  private val refreshRoomUpdateLoop = UpdateLoop { () =>
    // Retrieve data from server periodically
    BattleshipClient.clientSystem foreach  { lobbySystem =>
      lobbySystem ! ClientGuardian.RequestLobbyData()
    }
  }
  refreshRoomUpdateLoop.run(1000)

  def terminateUpdateLoop(): Unit = {
    mainUpdateLoop.terminateLoop()
    refreshRoomUpdateLoop.terminateLoop()
  }
}
