ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.0"

lazy val root = (project in file("."))
  .settings(
    name := "iToolsSmsViewer",
    libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31"
  )
