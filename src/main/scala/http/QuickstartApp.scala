package http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import http.routes.RecordRoutes
import infrastructure.repository.RecordRepositoryImpl

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object QuickstartApp {
  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    import system.executionContext

    val futureBinding = Http().newServerAt("localhost", 8080).bind(routes)

    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(exception) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", exception)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val db               = DbSetup.setup()
    val recordRepository = new RecordRepositoryImpl(db)
    val routes           = new RecordRoutes(recordRepository).routes

    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "quickstart-system")
    startHttpServer(routes)

    Await.result(system.whenTerminated, Duration.Inf)
  }
}
