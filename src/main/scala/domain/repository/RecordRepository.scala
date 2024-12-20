package domain.repository

import domain.model.{ Record, Report }

import scala.concurrent.Future

trait RecordRepository {
  def create(data: Record): Future[Long]
  def getRecordToProcess: Future[Option[Record]]
  def getReport(processedOnly: Boolean): Future[Option[Report]]
}
