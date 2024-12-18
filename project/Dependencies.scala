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

  lazy val typesafeConfig: Seq[ModuleID] = Seq("com.typesafe" % "config" % configV)

  lazy val akkaTest: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http-testkit"        % akkaHttpV % Test,
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaV     % Test,
  )

  lazy val scalaTest: Seq[ModuleID] = Seq("org.scalatest" %% "scalatest" % scalaTestV % "test")
}
