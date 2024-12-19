package infrastructure.repository

import commons.PrioritiesConfigLoader
import commons.PrioritiesConfigLoader.PrioritiesList
import domain.model.Record
import domain.repository.RecordRepository
import infrastructure.model.RecordsRow
import infrastructure.tables.RecordsTable
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime
import scala.concurrent.{ ExecutionContext, Future }

class RecordRepositoryImpl(db: Database)(implicit val executor: ExecutionContext) extends RecordRepository {
  private val tableQuery = RecordsTable.records

  override def create(data: Record): Future[Long] = {
    val row         = RecordsRow.fromDomain(data)
    val insertQuery = (tableQuery returning tableQuery.map(_.id)) += row
    db.run(insertQuery)
  }

  override def getRecordToProcess(): Future[Option[Record]] = {
    val currentTime    = LocalDateTime.now()
    val threeDaysAgo   = currentTime.minusDays(3)
    val priolitiesList = PrioritiesConfigLoader.loadPriorities()

    val query = tableQuery
      .filter(_.processedAt.isEmpty)
      .filterNot(r => filterRecords(r, threeDaysAgo))
    val results = db.run(query.result)

    results.flatMap { records =>
      val recordsWithPriority = records.map { record =>
        val priority = getPriority(record.amount, priolitiesList)
        (record, priority)
      }

      val result = recordsWithPriority.sortBy(-_._2).headOption.map(_._1)
      result match {
        case Some(record) => processRecord(record, currentTime)
        case None         => Future.successful(None)
      }
    }
  }

  private def filterRecords(r: RecordsTable, threeDaysAgo: LocalDateTime): Rep[Boolean] =
    tableQuery.filter(t => t.phoneNumber === r.phoneNumber && t.processedAt > threeDaysAgo).exists

  private def processRecord(record: RecordsRow, time: LocalDateTime): Future[Some[Record]] = {
    val updateQuery = tableQuery.filter(_.id === record.id).map(_.processedAt).update(Some(time))
    db.run(updateQuery).map(_ => Some(record.toDomain))
  }

  private def getPriority(amount: BigDecimal, lst: PrioritiesList): Int =
    lst.priorities.find { priority =>
      amount >= priority.range._1 && amount <= priority.range._2
    }.map(_.priority).getOrElse(Int.MaxValue) // TODO think about this default value

}
