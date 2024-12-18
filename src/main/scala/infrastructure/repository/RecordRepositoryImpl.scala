package infrastructure.repository

import domain.model.Record
import domain.repository.RecordRepository
import infrastructure.model.RecordsRow
import infrastructure.tables.RecordsTable
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.{ExecutionContext, Future}

class RecordRepositoryImpl(db: Database)(implicit val executor: ExecutionContext) extends RecordRepository {
  private val records = RecordsTable.records

  override def create(data: Record): Future[Long] = {
    val row         = RecordsRow.fromDomain(data)
    val insertQuery = (records returning records.map(_.id)) += row
    db.run(insertQuery)
  }
}
