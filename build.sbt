import Dependencies.*

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val dependencies = akka ++ slick ++ postgres ++ typesafeConfig ++ akkaTest ++ scalaTest

lazy val root = (project in file("."))
  .settings(
    inThisBuild(Seq(scalaVersion := "2.13.15", version := "0.1.0-SNAPSHOT")),
    name := "RecordManager",
    libraryDependencies ++= dependencies
  )
