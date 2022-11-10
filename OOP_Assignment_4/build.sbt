name := "BillSplitterApp"

version := "0.1"

scalaVersion := "2.12.3"

offline := true

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)

libraryDependencies ++= Seq(
  "org.scalafx" % "scalafx_2.12" % "8.0.144-R12",
  "org.scalafx" % "scalafxml-core-sfx8_2.12" % "0.4",
  "org.scalikejdbc" %% "scalikejdbc"       % "3.1.0",
  "com.h2database"  %  "h2"                % "1.4.196",
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "org.apache.derby" % "derby" % "10.13.1.1"
)

fork := true
