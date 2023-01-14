ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.10"


addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

lazy val root = (project in file("."))
  .settings(
    name := "DS_Group_Assignment",
  )

val AkkaVersion = "2.7.0"
val AkkaManagementVersion = "1.2.0"
val JavaJdkVersion = "15"
// https://mvnrepository.com/artifact/org.openjfx/javafx-fxml
libraryDependencies += "org.openjfx" % "javafx-fxml" % JavaJdkVersion
libraryDependencies += "org.openjfx" % "javafx-controls" % JavaJdkVersion
libraryDependencies += "org.openjfx" % "javafx-media" % JavaJdkVersion
libraryDependencies += "org.scalafx" %% "scalafx" % "16.0.0-R24"
libraryDependencies += "org.scalafx" %% "scalafxml-core-sfx8" % "0.5"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
// https://doc.akka.io/docs/akka/2.7.0/typed/cluster.html
libraryDependencies += "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion
// https://stackoverflow.com/questions/42598459/failed-to-load-class-org-slf4j-impl-staticloggerbinder-message-error-from-slf4
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
// https://doc.akka.io/docs/akka/current/serialization-jackson.html
libraryDependencies += "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion

// https://doc.akka.io/docs/akka-management/current/bootstrap/index.html
libraryDependencies += "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion
libraryDependencies += "com.typesafe.akka" %% "akka-discovery" % AkkaVersion