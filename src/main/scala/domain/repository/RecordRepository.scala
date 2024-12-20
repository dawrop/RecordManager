package domain.repository

import domain.model.Record

import scala.concurrent.Future

trait RecordRepository {
  def create(data: Record): Future[Long]
  def getRecordToProcess: Future[Option[Record]]
}
