package it

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.{ typed, ActorSystem }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import commons.JsonFormatter._
import commons.PrioritiesLoader
import domain.model.Record
import domain.repository.RecordRepository
import http.routes.RecordRoutes
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ mock, when }
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import spray.json.enrichAny

import java.time.LocalDateTime
import scala.concurrent.Future

class RecordsRoutesSpec extends AnyWordSpecLike with Matchers with ScalatestRouteTest {
  import RecordsRoutesData._

  "Saving new records" should {
    "be possible if record has valid fields" in {
      val recordEntity = Record("John", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      val validJson    = recordEntity.toJson.toString()
        when(recordRepoMock.create(any[Record])).thenReturn(Future.successful(1L))

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status should ===(StatusCodes.Created)
        responseAs[String] shouldEqual "Record created with ID: 1"
      }
    }

    "not create a record if input record is in invalid format" in {
      val recordEntity = Record("", "+481234", BigDecimal(-1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      val validJson    = recordEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status should ===(StatusCodes.BadRequest)
        responseAs[String] shouldEqual "Validation failed: Name cannot be empty, Phone number must be in a valid format, Amount must be greater than zero"
      }
    }

    "not create a record if input record has empty name" in {
      val recordEntity = Record("", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      val validJson    = recordEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status should ===(StatusCodes.BadRequest)
        responseAs[String] shouldEqual "Validation failed: Name cannot be empty"
      }
    }

    "not create a record if input record has wrong phone number format" in {
      val recordEntity = Record("John", "+4812345", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      val validJson    = recordEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status should ===(StatusCodes.BadRequest)
        responseAs[String] shouldEqual "Validation failed: Phone number must be in a valid format"
      }
    }

    "not create a record if input record amount is less than 0" in {
      val recordEntity = Record("John", "+48123456789", BigDecimal(-1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      val validJson    = recordEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Amount must be greater than zero"
      }
    }
  }

  "Fetching record for processing" should {
    "return OK with the record when found" in {
      val recordEntity = Record("John", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
      when(recordRepoMock.getRecordToProcess) thenReturn (Future.successful(Some(recordEntity)))
      val validJson = recordEntity.toJson.toString()

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe validJson
      }
    }

    "return NoContent when no record is found" in {
      when(recordRepoMock.getRecordToProcess) thenReturn (Future.successful(None))

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.NoContent
      }
    }

    "fail with InternalServerError if it fails completely" in {
      when(recordRepoMock.getRecordToProcess) thenReturn (Future.failed(new Exception("Database connection error")))

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.InternalServerError
        responseAs[String] shouldBe "Fetching error: Database connection error"
      }
    }
  }
}

object RecordsRoutesData {
  lazy val testKit: ActorTestKit                       = ActorTestKit("recordRoutesSpec")
  implicit def typedSystem: typed.ActorSystem[Nothing] = testKit.system
  def createActorSystem(): ActorSystem                 = typedSystem.classicSystem

  val recordRepoMock: RecordRepository = mock(classOf[RecordRepository])
  val prioLoaderMock: PrioritiesLoader = mock(classOf[PrioritiesLoader])
  val recordRoutesMock: Route          = new RecordRoutes(recordRepoMock).routes
}
