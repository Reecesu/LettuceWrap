name := """scala-backend"""
organization := "CU-boulder"
version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.10"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "ch.megard" %% "akka-http-cors" % "1.2.0"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "CU-boulder.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "CU-boulder.binders._"