package infrastructure.repository

import commons.PrioritiesConfigLoader
import commons.PrioritiesConfigLoader.PrioritiesList
import domain.model.{ PersonReport, PhoneReport, Record, Report }
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

  override def getRecordToProcess: Future[Option[Record]] = {
    val currentTime    = LocalDateTime.now()
    val threeDaysAgo   = currentTime.minusDays(3)
    val prioritiesList = PrioritiesConfigLoader.loadPriorities()

    val query = tableQuery
      .filter(_.processedAt.isEmpty)
      .filterNot(r => filterRecords(r, threeDaysAgo))
    val results = db.run(query.result)

    results.flatMap { records =>
      val recordsWithPriority = records.map { record =>
        val priority = getPriority(record.amount, prioritiesList)
        (record, priority)
      }

      val result = recordsWithPriority.sortBy(_._2).headOption.map(_._1)
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

  override def getReport(processedOnly: Boolean): Future[Option[Report]] = {
    val query       = getQuery(processedOnly)
    val reportQuery = getReportQuery(query)

    db.run(reportQuery.result).map { rows =>
      if (rows.isEmpty) None
      else Some(transform(rows))
    }
  }

  private def getQuery(processedOnly: Boolean): Query[RecordsTable, RecordsTable#TableElementType, Seq] =
    if (processedOnly) tableQuery.filter(_.processedAt.isDefined)
    else tableQuery

  private def getReportQuery(query: Query[RecordsTable, RecordsTable#TableElementType, Seq]): Query[
    (Rep[String], Rep[String], Rep[BigDecimal], Rep[LocalDateTime]),
    (String, String, BigDecimal, LocalDateTime),
    Seq
  ] =
    query
      .groupBy(r => (r.phoneNumber, r.name))
      .map { case ((phone, name), records) =>
        (
          phone,
          name,
          records.map(_.amount).sum.getOrElse(BigDecimal(0)),
          records.map(_.createdAt).max.getOrElse(LocalDateTime.now)
        )
      }

  private def transform(rows: Seq[(String, String, BigDecimal, LocalDateTime)]): Report = {
    val phoneReports = rows
      .groupBy(_._1)
      .map { case (phone, groupedRows) =>
        val personReports = groupedRows.map { case (_, name, total, latest) =>
          PersonReport(name, total, latest)
        }
        PhoneReport(phone, personReports)
      }
      .toSeq
    Report(phoneReports)
  }
}
