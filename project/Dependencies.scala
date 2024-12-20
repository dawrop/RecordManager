import Versions.*
import sbt.*

object Dependencies {
  lazy val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http"            % akkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpV,
    "com.typesafe.akka" %% "akka-actor-typed"     % akkaV,
    "com.typesafe.akka" %% "akka-stream"          % akkaV
  )

  lazy val slick: Seq[ModuleID] = Seq(
    "com.typesafe.slick"  %% "slick"          % slickV,
    "com.typesafe.slick"  %% "slick-hikaricp" % slickV,
    "com.github.tminglei" %% "slick-pg"       % slickPgV
  )

  lazy val postgres: Seq[ModuleID] = Seq("org.postgresql" % "postgresql" % postgresqlV)

  lazy val h2: Seq[ModuleID] = Seq("com.h2database" % "h2" % h2V % Test)

  lazy val typesafeConfig: Seq[ModuleID] = Seq("com.typesafe" % "config" % configV)

  lazy val cats: Seq[ModuleID] = Seq("org.typelevel" %% "cats-core" % catsV)

  lazy val scalaTest: Seq[ModuleID] = Seq("org.scalatest" %% "scalatest" % scalaTestV)

  lazy val akkaTest: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpV % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV     % Test
  )

  lazy val mockito: Seq[ModuleID] = Seq("org.mockito" % "mockito-core" % mockitoV % Test)

  lazy val scalaMock: Seq[ModuleID] = Seq("org.scalamock" %% "scalamock" % scalaMockV % Test)

  lazy val testContainers: Seq[ModuleID] = Seq(
    "com.dimafeng"      %% "testcontainers-scala-scalatest"  % testContV         % Test,
    "com.dimafeng"      %% "testcontainers-scala-postgresql" % testContV         % Test,
    "org.testcontainers" % "postgresql"                      % testContPostgresV % Test
  )

  lazy val logging: Seq[ModuleID] = Seq(
    "com.typesafe.scala-logging" %% "scala-logging"   % scalaLoggingV,
    "ch.qos.logback"              % "logback-classic" % logbackV
  )
}
