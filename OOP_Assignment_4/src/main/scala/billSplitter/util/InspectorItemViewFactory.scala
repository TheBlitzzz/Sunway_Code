package billSplitter.util

import billSplitter.model.InspectorItem
import scalafx.scene.control.{ListCell, ListView}

object InspectorItemViewFactory {
  // https://docs.oracle.com/javafx/2/ui_controls/list-view.htm
  // https://www.preining.info/blog/2017/10/scalafx-listview-with-cellfactory/
  def listCellFactory[T <: InspectorItem](listView: ListView[T]): ListCell[T] = {
    new ListCell[T]() {
      item.onChange({
        text = if (item.value == null) "" else item.value.displayName
      })
    }
  }
}
