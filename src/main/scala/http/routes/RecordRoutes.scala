package http.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{ Directives, Route }
import cats.data.Validated
import com.typesafe.scalalogging.LazyLogging
import commons.JsonFormatter._
import domain.model.Record
import domain.repository.RecordRepository
import spray.json.enrichAny

import scala.util.{ Failure, Success }

class RecordRoutes(recordRepo: RecordRepository) extends Directives with LazyLogging {

  val routes: Route = pathPrefix("records") {
    pathEnd {
      concat(
        post {
          entity(as[Record]) { record =>
            Record.validate(record) match {
              case Validated.Valid(validRec) =>
                onComplete(recordRepo.create(validRec)) {
                  case Success(id) =>
                    complete(StatusCodes.Created, s"Record created with ID: $id")
                  case Failure(ex) =>
                    logger.error(s"Error during record creation: ${ex.getMessage}")
                    complete(StatusCodes.InternalServerError, s"Error during record creation: ${ex.getMessage}")
                }
              case Validated.Invalid(e) =>
                val errorMess = e.map(err => err.message).toList.mkString(", ")
                logger.error(s"400 Bad Request: Validation failed: $errorMess")
                complete(StatusCodes.BadRequest, s"Validation failed: $errorMess")
            }
          }
        },
        get {
          onComplete(recordRepo.getRecordToProcess) {
            case Success(optRecord) =>
              optRecord match {
                case Some(value) => complete(StatusCodes.OK, value.toJson)
                case None        => complete(StatusCodes.NoContent)
              }
            case Failure(ex) =>
              logger.error(s"Fetching error: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError, s"Fetching error: ${ex.getMessage}")
          }
        }
      )
    }
  }
}
