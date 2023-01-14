package battleship.utils

import javafx.scene.Parent
import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object MyFxml {
  /**
   * Loads the FXML file with a [[FXMLLoader]] instead of using a [[scalafxml.core.FXMLView]]
   * The file is retrieved using the getResourcesAsStream function.
   * Therefore, the FXML file must be located in the resources folder under src/main
   *
   * Note : Always remember to start with '/'
   *
   * @param filePath to where the FXML file is located
   * @return the root of the FXML page as [[Node]], the [[FXMLLoader]] and the [[Controller]]
   */
  def loadFxmlNode[Node, Controller](filePath : String): (FXMLLoader, Node, Controller) = {
    // Instead of FXMLView, we create a new ScalaFXML loader
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(getClass.getResourceAsStream(filePath))
    (loader, loader.getRoot[Node], loader.getController[Controller]())
  }

  /**
   * Loads the FXML file with a [[FXMLLoader]] instead of using a [[scalafxml.core.FXMLView]]
   * The file is retrieved using the getResourcesAsStream function.
   * Therefore, the FXML file must be located in the resources folder under src/main
   *
   * Note : Always remember to start with '/'
   *
   * @param filePath to where the FXML file is located
   * @return the root of the FXML page as [[javafx.scene.Parent]], the [[FXMLLoader]] and the [[Controller]]
   */
  def loadFxmlRoot[Controller](filePath : String): (FXMLLoader, Parent, Controller) = {
    // Instead of FXMLView, we create a new ScalaFXML loader
    val loader = new FXMLLoader(null, NoDependencyResolver)
    loader.load(getClass.getResourceAsStream(filePath))
    (loader, loader.getRoot[javafx.scene.Parent], loader.getController[Controller])
  }
}
