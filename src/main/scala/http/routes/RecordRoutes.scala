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
      post {
        entity(as[Record]) { record =>
          Record.validate(record) match {
            case Validated.Valid(validRec) =>
              onComplete(recordRepo.create(validRec)) {
                case Success(id) =>
                  complete(StatusCodes.Created, s"Record created with ID: $id")
                case Failure(ex) =>
                  logger.error(s"Error during record creation: ${ex.getMessage}")
                  complete(StatusCodes.InternalServerError, "An unexpected error occurred. Please try again later")
              }
            case Validated.Invalid(e) =>
              val errorMess = e.map(err => err.message).toList.mkString(", ")
              logger.error(s"400 Bad Request: Validation failed: $errorMess")
              complete(StatusCodes.BadRequest, s"Validation failed: $errorMess")
          }
        }
      } ~
        get {
          onComplete(recordRepo.getRecordToProcess) {
            case Success(optRecord) =>
              optRecord match {
                case Some(value) => complete(StatusCodes.OK, value.toJson)
                case None        => complete(StatusCodes.NotFound, "No records available for processing")
              }
            case Failure(ex) =>
              logger.error(s"Fetching error: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError, "An unexpected error occurred. Please try again later")
          }
        }
    }
  } ~
    path("report") {
      parameter("processedOnly".as[Boolean]) { flag =>
        get {
          onComplete(recordRepo.getReport(flag)) {
            case Success(optReport) =>
              optReport match {
                case Some(value) => complete(StatusCodes.OK, value.toJson)
                case None =>
                  complete(
                    StatusCodes.NotFound,
                    "Report not generated: no data available or all records were filtered out"
                  )
              }
            case Failure(ex) =>
              logger.error(s"Report error: ${ex.getMessage}")
              complete(StatusCodes.InternalServerError, "An unexpected error occurred. Please try again later")
          }
        }

      }
    }
}
