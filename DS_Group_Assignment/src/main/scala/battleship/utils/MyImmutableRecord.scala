package battleship.utils

import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.scene.control.{TableColumn, TableView, TreeItem, TreeTableColumn, TreeTableView}

object MyImmutableRecord {
  private val emptyStringProp = new StringProperty()

  def apply(keyValueProperties: (String, String)*) = new MyImmutableRecord(keyValueProperties.toMap)

  // TODO ADD COMMENTS

  /**
   *
   * */
  def pairWithBuffer[T, U](models: ObservableBuffer[Option[T]], converter: ToImmutableRecordConverter[T, U]): ObservableBuffer[U] = {
    val observedModels: ObservableBuffer[U] = new ObservableBuffer[U]() {}
    models.onChange((_, changes) => {
      changes.foreach {
        case ObservableBuffer.Add(position, added) =>
          var counter = position
          added.foreach(x => {
            x match {
              case Some(value) => observedModels.add(counter, converter.toRecord(value))
              case None => observedModels.add(counter, converter.createDummy())
            }
            counter += 1
          })
        case ObservableBuffer.Remove(position, removed) =>
          observedModels.remove(position, removed.size)
        case ObservableBuffer.Reorder(start, end, permutation) =>
          val originalValues = observedModels.slice(start, end)
          for (counter <- start to end) {
            observedModels(counter) = originalValues(permutation(counter))
          }
        case ObservableBuffer.Update(from, to) =>
      }
    })
    models.foreach {
      case Some(x) => observedModels += converter.toRecord(x)
      case None => converter.createDummy()
    }

    observedModels
  }

  def bindToTable(observableBuffer: ObservableBuffer[MyImmutableRecord], table: TableView[MyImmutableRecord], columns: TableColumn[MyImmutableRecord, String]*): Unit = {
    table.setItems(observableBuffer)
    columns.foreach(column => {
      column.cellValueFactory = x => x.value.stringProperties.get(column.text.value) match {
        case Some(x) => x
        case None => emptyStringProp
      }
    })
  }

  def bindToTreeTable(rootItem: TreeItem[MyImmutableRecord], table: TreeTableView[MyImmutableRecord], columns: TreeTableColumn[MyImmutableRecord, String]*): Unit = {
    table.root = rootItem
    columns.foreach(column => {
      column.cellValueFactory = x => {
        if (x.value != null) {
          x.value.value.value.stringProperties.get(column.text.value) match {
            case Some(x) => x
            case None => emptyStringProp
          }
        } else {
          emptyStringProp
        }
      }
    })
  }
}


trait ToImmutableRecordConverter[T, U] {
  def toRecord(originalValue: T): U

  def createDummy(): U
}

class MyImmutableRecord(values: Map[String, String]) {
  private val stringProperties = values.mapValues(x => new StringProperty(x))

  def stringVal(key: String): String = values.get(key) match {
    case Some(x) => x
    case None => ""
  }
}
