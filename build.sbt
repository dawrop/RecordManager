import Dependencies.*

ThisBuild / scalaVersion := "2.13.15"
ThisBuild / version := "0.1.0-SNAPSHOT"

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

lazy val dependencies     = akka ++ slick ++ postgres ++ typesafeConfig ++ akkaTest ++ scalaTest ++ cats ++ logging
lazy val testDependencies = scalaTest.map(_ % "test,it") ++ akkaTest ++ mockito ++ scalaMock ++ testContainers ++ h2

lazy val root = (project in file("."))
  .settings(name := "RecordManager", libraryDependencies ++= dependencies)

lazy val integrationTest = (project in file("integration"))
  .dependsOn(root)
  .configs(Test)
  .settings(inConfig(Test)(Defaults.testSettings))
  .settings(libraryDependencies ++= testDependencies)

addCommandAlias("itTestAll", ";project integrationTest;test;project root")
addCommandAlias("itTestRoutes", ";project integrationTest;testOnly *RecordsRoutesSpec*;project root")
addCommandAlias("itTestRepo", ";project integrationTest;testOnly *RecordsRepoSpec*;project root")

addCommandAlias("fmtRoot", ";scalafmt;scalafmtSbt;test:scalafmt;it:scalafmt")
addCommandAlias(
  "fmtIntegration",
  ";project integrationTest;scalafmt;scalafmtSbt;test:scalafmt;it:scalafmt;project root"
)

addCommandAlias("fmtAll", ";fmtRoot;fmtIntegration")
