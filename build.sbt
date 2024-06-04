name := """play-java-seed"""
organization := "Books-Service"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies += "org.mongodb" % "mongo-java-driver" % "3.12.14"
libraryDependencies ++= Seq(
  "org.mongodb" % "mongo-java-driver" % "3.12.0",
  "dev.morphia.morphia" % "core" % "1.5.8",
  "org.easytesting" % "fest-assert" % "1.4" % "test",
  javaWs
)
libraryDependencies += "com.typesafe.play" % "play-ws_2.11" % play.core.PlayVersion.current



