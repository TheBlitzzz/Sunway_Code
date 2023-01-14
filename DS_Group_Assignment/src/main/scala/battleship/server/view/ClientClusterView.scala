package battleship.server.view

import battleship.model.ClientProfileModel
import battleship.server.ServerLobby
import battleship.utils.{MyFxml, MyImmutableRecord, ToImmutableRecordConverter}
import javafx.scene.Parent
import javafx.scene.control.SelectionMode
import scalafx.Includes._
import scalafx.collections.ObservableBuffer
import scalafx.event.ActionEvent
import scalafx.scene.control.{TableColumn, TableView, TextField}
import scalafxml.core.macros.sfxml

@sfxml
class ClientClusterView(val connectionsTableView: TableView[MyImmutableRecord],
                        val connectionsTableUsernameCol: TableColumn[MyImmutableRecord, String],
                        val connectionsTableIpAddressCol: TableColumn[MyImmutableRecord, String],
                        val connectionsTablePortCol: TableColumn[MyImmutableRecord, String],
                        val connectionsTableTimeJoinedCol: TableColumn[MyImmutableRecord, String],
                        val clientUsernameReadOnlyTextField: TextField,
                        val clientIpReadOnlyTextField: TextField,
                        val clientPortReadOnlyTextField: TextField,
                       ) extends ClientClusterController {

//  def handleGetClientInfoBtnOnAction(e: ActionEvent): Unit = {
//
//  }
//
//  def handleKickClientBtnOnAction(e: ActionEvent): Unit = {
//
//  }
}

object ClientClusterController extends ToImmutableRecordConverter[ClientProfileModel, MyImmutableRecord] {
  var observedClients: ObservableBuffer[MyImmutableRecord] = MyImmutableRecord.pairWithBuffer(ServerLobby.clientProfiles, ClientClusterController)

  def apply(): (Parent, ClientClusterController) = {
    val (_, root, controller) = MyFxml.loadFxmlRoot[ClientClusterController]("/battleship/server/view/client_cluster_view.fxml")
    (root, controller)
  }

  def toRecord(profile: ClientProfileModel): MyImmutableRecord = {
    val usernameCol = ("Username", profile.username)
    val ipAddressCol = ("IP Address", profile.localIpAddress)
    val portCol = ("Port", profile.port.toString)
    val timeJoinedCol = ("Time Joined", profile.timeJoinedString)
    MyImmutableRecord(usernameCol, ipAddressCol, portCol, timeJoinedCol)
  }

  def createDummy() : MyImmutableRecord = MyImmutableRecord()
}

trait ClientClusterController {
  val connectionsTableView: TableView[MyImmutableRecord]
  val connectionsTableUsernameCol: TableColumn[MyImmutableRecord, String]
  val connectionsTableIpAddressCol: TableColumn[MyImmutableRecord, String]
  val connectionsTablePortCol: TableColumn[MyImmutableRecord, String]
  val connectionsTableTimeJoinedCol: TableColumn[MyImmutableRecord, String]
  val clientUsernameReadOnlyTextField: TextField
  val clientIpReadOnlyTextField: TextField
  val clientPortReadOnlyTextField: TextField

  MyImmutableRecord.bindToTable(ClientClusterController.observedClients, connectionsTableView,
    connectionsTableUsernameCol,
    connectionsTableIpAddressCol,
    connectionsTablePortCol,
    connectionsTableTimeJoinedCol,
  )
  connectionsTableView.selectionModel.value.setSelectionMode(SelectionMode.SINGLE)
  connectionsTableView.selectionModel.value.selectedItemProperty.onChange((_, _, newVal) => onSelectedClientChanged(newVal))
  onSelectedClientChanged(null)

  protected def onSelectedClientChanged(client: MyImmutableRecord): Unit = {
    if (client == null) {
      clientUsernameReadOnlyTextField.text.value = "Nothing selected"
      clientIpReadOnlyTextField.text.value = "Nothing selected"
      clientPortReadOnlyTextField.text.value = "Nothing selected"
    } else {
      clientUsernameReadOnlyTextField.text.value = client.stringVal("Username")
      clientIpReadOnlyTextField.text.value = client.stringVal("IP Address")
      clientPortReadOnlyTextField.text.value = client.stringVal("Port")
    }
  }
}
