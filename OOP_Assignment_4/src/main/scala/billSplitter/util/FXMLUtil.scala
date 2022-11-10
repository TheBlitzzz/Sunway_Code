package billSplitter.util

import scalafxml.core.{FXMLLoader, NoDependencyResolver}

object FXMLUtil {
  def loadFXML(resourcePath : String): FXMLLoader = {
    // transform path of the FXML file into URI for resource location.
    val resource = getClass.getResourceAsStream(resourcePath)
    // initialize the loader object.
    val loader = new FXMLLoader(null, NoDependencyResolver)
    // Load the layout from the URI file
    loader.load(resource)
    loader
  }
}
