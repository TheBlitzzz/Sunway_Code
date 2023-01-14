package battleship.server.view

import battleship.model.ServerProfileModel
import battleship.server.{BattleshipServer, ServerLobby}
import battleship.utils.{MyFxml, MyImmutableRecord, ToImmutableRecordConverter}
import javafx.scene.Parent
import javafx.scene.control.SelectionMode
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{TableColumn, TableView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class ServerClusterView(val serverAddressReadOnlyTextField: TextField,
                        val connectionsTableView: TableView[MyImmutableRecord],
                        val connectionsTableIpAddressCol: TableColumn[MyImmutableRecord, String],
                        val connectionsTablePortCol: TableColumn[MyImmutableRecord, String],
                        val connectionsTableTimeJoinedCol: TableColumn[MyImmutableRecord, String],
                        val remoteIpReadOnlyTextField: TextField,
                        val remotePortReadOnlyTextField: TextField,
                        val remoteTimeJoinedReadOnlyTextField: TextField,
                       ) extends ServerClusterController {
//  def handleGetServerInfoBtnOnAction(e: ActionEvent): Unit = {
//
//  }
}

object ServerClusterController extends ToImmutableRecordConverter[ServerProfileModel, MyImmutableRecord] {
  val observedServers: ObservableBuffer[MyImmutableRecord] = MyImmutableRecord.pairWithBuffer(ServerLobby.serverProfiles, ServerClusterController)

  def apply(): (Parent, ServerClusterController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[ServerClusterController]("/battleship/server/view/server_cluster_view.fxml")
    (root, controller)
  }

  def toRecord(profile: ServerProfileModel): MyImmutableRecord = {
    val ipAddressCol = ("IP Address", profile.ipAddress)
    val portCol = ("Port", profile.port.toString)
    val timeJoinedCol = ("Time Joined", profile.timeJoinedString)
    MyImmutableRecord(ipAddressCol, portCol, timeJoinedCol)
  }

  def createDummy(): MyImmutableRecord = MyImmutableRecord()
}

trait ServerClusterController {
  val serverAddressReadOnlyTextField: TextField
  val connectionsTableView: TableView[MyImmutableRecord]
  val connectionsTableIpAddressCol: TableColumn[MyImmutableRecord, String]
  val connectionsTablePortCol: TableColumn[MyImmutableRecord, String]
  val connectionsTableTimeJoinedCol: TableColumn[MyImmutableRecord, String]
  val remoteIpReadOnlyTextField: TextField
  val remotePortReadOnlyTextField: TextField
  val remoteTimeJoinedReadOnlyTextField: TextField

  val serverStarted: Boolean = BattleshipServer.serverMem.serverStarted
  if (serverStarted) {
    serverAddressReadOnlyTextField.text = BattleshipServer.serverMem.clusterAddress
  } else {
    serverAddressReadOnlyTextField.text = "Not Started"
  }

  MyImmutableRecord.bindToTable(ServerClusterController.observedServers, connectionsTableView,
    connectionsTableIpAddressCol,
    connectionsTablePortCol,
    connectionsTableTimeJoinedCol,
  )
  connectionsTableView.selectionModel.value.setSelectionMode(SelectionMode.SINGLE)
  connectionsTableView.selectionModel.value.selectedItemProperty.onChange((_, _, newVal) => onSelectedServerChanged(newVal))
  onSelectedServerChanged(null)


  protected def onSelectedServerChanged(server: MyImmutableRecord): Unit = {
    if (server == null) {
      remoteIpReadOnlyTextField.text.value = "Nothing selected"
      remotePortReadOnlyTextField.text.value = "Nothing selected"
      remoteTimeJoinedReadOnlyTextField.text.value = "Nothing selected"
    } else {
      remoteIpReadOnlyTextField.text.value = server.stringVal("IP Address")
      remotePortReadOnlyTextField.text.value = server.stringVal("Port")
      remoteTimeJoinedReadOnlyTextField.text.value = server.stringVal("Time Joined")
    }
  }

}
