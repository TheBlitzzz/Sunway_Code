# Notes from students

This md file is intended for our references as students during our studies and developing this project.
This file will mainly focus on the Scala, Scalafx and ScalaFXML.

# Scala
Scala is a OOP and Functional programming language that is built to create a scalable Java. <br>
It runs on top of the JVM and can execute any Java code, thus leveraging on existing packages and libraries. <br>
For this project, we'll be using the Scala versions of the Java UI framework JavaFx and JavaFXML, which are ScalaFx and ScalaFXML respectively.

To use ScalaFXML, we'll need the macros plugin, which can be included in the `build.sbt` file.

> addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

# ScalaFx

## DialogPane
-> Button type such as Apply, Cancel, etc are the default buttons.<br>
-> Can use `dialog.setResultConverter()` to return values from the dialog.<br>
-> Alternatively, define custom buttons, add `dialog.close()` to `dialog.onHidden`, and define a custom `dialog.setResult()` for each button 

## Canvas
-> Draw stuff onto the screen <br>
-> Can bind the size of the `canvas` to a `targetNode` with the following code <br>
> canvas.widthProperty.bind(targetNode.widthProperty)
> canvas.heightProperty.bind(targetNode.heightProperty)

-> Get the graphics context to actually draw to the canvas
> val gc = canvas.getGraphicsContext2D

-> The graphics context comes with functions to draw various shapes on the screen, such as :
> gc.setFill(Color(0.5, 0, 0, 1))
 
-> Moreover, you can set the fill colour using classes from scalafx.scene.paint
> gc.fillOval(posX, posY, width, height)


# ScalaFXML
The ScalaFXML library functions to make life easier when creating UI elements. <br>
FXML is an XML-based markup language to define the ScalaFx components.

-> We can use `fx:include` to load a FXML file within another FXML file such as
> <fx:include source="/battleship/client/view/nodes/fleet_grid_view.fxml"/>

https://stackoverflow.com/questions/24607969/mouse-events-get-ignored-on-the-underlying-layer

-> We can use the `mouseTransparent` property to prevent nodes from blocking events passed down to the underlying nodes

-> We can add a `graphic` attribute to a button to specify how it looks. Use the `SVGPath` tag to define an SVG image. <br>
Link : https://stackoverflow.com/questions/35604436/how-to-style-a-svg-using-css-in-javafx-fxml

## CSS Styles
-> Notes to add css styles <br>
-> Making the text area background to be transparent
> .text-area { -fx-background-color: rgba(53,89,119,0.4); }
> 
> .text-area .scroll-pane { -fx-background-color: transparent; }
> 
> .text-area .scroll-pane .viewport { -fx-background-color: transparent; }
> 
> .text-area .scroll-pane .content { -fx-background-color: transparent; }

Link : https://stackoverflow.com/questions/21936585/transparent-background-of-a-textarea-in-javafx-8

## Architecture
ScalaFXML supports a View-Controller architecture where we define how a page looks in views,
and the program logic with Controller classes.  <br>
- Specify the controller the FXML file with fx:id at the root node. <br/>
- Annotate the controller class with a `@sfxml` macros 
- Obtain the controller class using the `FXMLLoader`

##Getting the Controller
Since the macro will alter the `Controller` class, we'll need to find a way to reference the class again.
To do so, we'll use a trait to act as the middle man.

> trait SampleController {
> 
>   val sampleTextField : TextField
> 
>   def doSomething() : Unit = {
> 
>     // program logic
> 
>   }
> 
> }
> 
> @sfxml
> class SampleView (val sampleTextField : TextField) {
> 
>   def handleBtnOnAction(e: ActionEvent) : Unit = {
> 
>     doSomething()
> 
>   }
> 
> }
