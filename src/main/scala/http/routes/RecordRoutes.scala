package http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import cats.data.Validated
import domain.model.Record
import domain.model.RecordFormatter._
import domain.repository.RecordRepository

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }

class RecordRoutes(recordRepo: RecordRepository)(implicit ec: ExecutionContext) extends Directives {

  val routes: Route = pathPrefix("records") {
    pathEnd {
      post {
        entity(as[Record]) { record =>
          Record.validate(record) match {
            case Validated.Valid(validRec) =>
              onComplete(recordRepo.create(validRec)) {
                case Success(id) =>
                  complete(StatusCodes.Created, s"Record created with ID: $id")
                case Failure(ex) =>
                  complete(StatusCodes.InternalServerError, s"Error during record creation: ${ex.getMessage}")
              }
            case Validated.Invalid(e) =>
              val errorMess = e.map(err => err.message).toList.mkString(", ")
              complete(StatusCodes.BadRequest, s"Validation failed: $errorMess")
          }
        }
      }
    }
  }
}
