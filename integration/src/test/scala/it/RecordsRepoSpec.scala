package it

import domain.model.Record
import infrastructure.model.RecordsRow
import infrastructure.repository.RecordRepositoryImpl
import infrastructure.tables.RecordsTable
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

class RecordsRepoSpec extends AnyWordSpecLike with Matchers {
  import RecordsRepoSpecData._

  "Creating records" should {
    val validRecord = Record("John", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
    val result      = Await.result(recordRepo.create(validRecord), 2.seconds)

    "successfully create a record and return its ID" in {
      result shouldEqual 1L
    }
  }

  "Fetching records for processing" when {
    val tableQuery = RecordsTable.records
    Await.result(db.run(tableQuery ++= testRecords), 2.seconds)

    "a record has the highest priority" should {
      val result = Await.result(recordRepo.getRecordToProcess, 2.seconds)

      "fetch the record with the highest priority" in {
        result shouldEqual Some(
          Record("John", "+48123456789", BigDecimal(1000.0), LocalDateTime.of(2024, 12, 19, 12, 30))
        )
      }

      "mark the previously returned record as processed" in {
        val result      = Await.result(db.run(tableQuery.filter(_.id === 1L).result.headOption), 2.seconds)
        val processedAt = result.flatMap(_.processedAt)
        result shouldEqual Some(
          RecordsRow(
            Some(1),
            "John",
            "+48123456789",
            BigDecimal(1000.0),
            processedAt,
            LocalDateTime.of(2024, 12, 19, 12, 30)
          )
        )
      }
    }

    "when the process is run again" should {
      val result = Await.result(recordRepo.getRecordToProcess, 2.seconds)

      "return Alice's record because John's record has already been processed" in {
        result shouldEqual Some(
          Record("Alice", "+48987654321", BigDecimal(600.0), LocalDateTime.of(2024, 12, 19, 12, 30))
        )
      }
    }

    "when no records are available for processing" should {
      val result = Await.result(recordRepo.getRecordToProcess, 2.seconds)

      "return None as no records meet the conditions" in {
        result shouldEqual None
      }
    }
  }
}

object RecordsRepoSpecData {
  val db         = Database.forConfig("h2mem1")
  val recordRepo = new RecordRepositoryImpl(db)

  val schemaSetup = RecordsTable.records.schema.createIfNotExists
  Await.result(db.run(schemaSetup), 2.seconds)

  val testRecords: Seq[RecordsRow] = Seq(
    RecordsRow.fromDomain(Record("John", "+48123456789", BigDecimal(700.0), LocalDateTime.of(2024, 12, 19, 12, 30))),
    RecordsRow.fromDomain(Record("Alice", "+48987654321", BigDecimal(600.0), LocalDateTime.of(2024, 12, 19, 12, 30)))
  )
}