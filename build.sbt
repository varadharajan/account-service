import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "in.varadharajan"
ThisBuild / organizationName := "in.varadharajan"

lazy val root = (project in file("."))
  .settings(
    name := "account-service",
    libraryDependencies ++= akka
      ++ scalaSTM
      ++ logging
      ++ Seq(scalaTest, scalaMock)
  )
