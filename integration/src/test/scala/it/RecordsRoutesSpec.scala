package it

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.{ typed, ActorSystem }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, StatusCodes }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import commons.JsonFormatter._
import domain.model.{ PersonReport, PhoneReport, Record, Report }
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

  "Creating new records" should {
    "be possible if record has valid fields" in {
      when(recordRepoMock.create(any[Record])) thenReturn Future.successful(1L)

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status shouldEqual StatusCodes.Created
        responseAs[String] shouldEqual "Record created with ID: 1"
      }
    }

    "not create a record if input record is in invalid format" in {
      val invalidEntity = recordEntity.copy(name = "", phoneNum = "+481234", amount = BigDecimal(-1000.0))
      val invalidJson   = invalidEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, invalidJson)) ~> recordRoutesMock ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Name cannot be empty, Phone number must be in a valid format, Amount must be greater than zero"
      }
    }

    "not create a record if input record has empty name" in {
      val emptyNameEntity = recordEntity.copy(name = "")
      val invalidJson     = emptyNameEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, invalidJson)) ~> recordRoutesMock ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Name cannot be empty"
      }
    }

    "not create a record if input record has numbers in name" in {
      val invalidNameEntity = recordEntity.copy(name = "John123")
      val invalidJson       = invalidNameEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, invalidJson)) ~> recordRoutesMock ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Name cannot contain numbers"
      }
    }

    "not create a record if input record has wrong phone number format" in {
      val invalidNumberEntity = recordEntity.copy(phoneNum = "+4812345")
      val invalidJson         = invalidNumberEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, invalidJson)) ~> recordRoutesMock ~> check {
        status shouldEqual StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Phone number must be in a valid format"
      }
    }

    "not create a record if input record amount is less than 0" in {
      val invalidAmountEntity = recordEntity.copy(amount = BigDecimal(-1000.0))
      val invalidJson         = invalidAmountEntity.toJson.toString()

      Post("/records", HttpEntity(ContentTypes.`application/json`, invalidJson)) ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.BadRequest
        responseAs[String] shouldEqual "Validation failed: Amount must be greater than zero"
      }
    }

    "fail with InternalServerError if it fails completely" in {
      when(recordRepoMock.create(any[Record])) thenReturn Future.failed(new Exception("Database connection error"))

      Post("/records", HttpEntity(ContentTypes.`application/json`, validJson)) ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.InternalServerError
        responseAs[String] shouldEqual "An unexpected error occurred. Please try again later"
      }
    }
  }

  "Fetching record for processing" should {
    "return OK with the record when found" in {
      when(recordRepoMock.getRecordToProcess) thenReturn Future.successful(Some(recordEntity))

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe validJson
      }
    }

    "return NotFound when no record is found" in {
      when(recordRepoMock.getRecordToProcess) thenReturn Future.successful(None)

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.NotFound
        responseAs[String] shouldEqual "No records available for processing"
      }
    }

    "fail with InternalServerError if it fails completely" in {
      when(recordRepoMock.getRecordToProcess) thenReturn Future.failed(new Exception("Database connection error"))

      Get("/records") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.InternalServerError
        responseAs[String] shouldBe "An unexpected error occurred. Please try again later"
      }
    }
  }

  "Generating report" should {
    "return report with all records" in {
      when(recordRepoMock.getReport(false)) thenReturn Future.successful(Some(reportEntity))

      Get("/report?processedOnly=false") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe reportEntity.toJson.toString()
      }
    }

    "return report with only processed records" in {
      when(recordRepoMock.getReport(true)) thenReturn Future.successful(Some(processedReportEntity))

      Get("/report?processedOnly=true") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.OK
        responseAs[String] shouldBe processedReportEntity.toJson.toString()
      }
    }

    "fail with NotFound if there is no data available for generating report" in {
      when(recordRepoMock.getReport(false)) thenReturn Future.successful(None)

      Get("/report?processedOnly=false") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.NotFound
        responseAs[String] shouldBe "Report not generated: no data available or all records were filtered out"
      }
    }

    "fail with InternalServerError if it fails completely" in {
      when(recordRepoMock.getReport(false)) thenReturn Future.failed(new Exception("Database connection error"))

      Get("/report?processedOnly=false") ~> recordRoutesMock ~> check {
        status shouldBe StatusCodes.InternalServerError
        responseAs[String] shouldBe "An unexpected error occurred. Please try again later"
      }
    }
  }
}

object RecordsRoutesData {
  lazy val testKit: ActorTestKit                       = ActorTestKit("recordRoutesSpec")
  implicit def typedSystem: typed.ActorSystem[Nothing] = testKit.system
  def createActorSystem(): ActorSystem                 = typedSystem.classicSystem

  val recordRepoMock: RecordRepository = mock(classOf[RecordRepository])
  val recordRoutesMock: Route          = new RecordRoutes(recordRepoMock).routes

  val recordEntity: Record = Record("John", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
  val validJson: String    = recordEntity.toJson.toString()

  val processedReportEntity: Report = Report(
    Seq(PhoneReport("+48123456789", Seq(PersonReport("Alice", 600.0, LocalDateTime.of(2024, 12, 19, 23, 23)))))
  )
  val reportEntity: Report = Report(
    Seq(
      PhoneReport(
        "+48123456789",
        Seq(
          PersonReport("John", 100.00, LocalDateTime.of(2024, 12, 20, 13, 30)),
          PersonReport("Alice", 1200.00, LocalDateTime.of(2024, 12, 20, 14, 0))
        )
      )
    )
  )
}
