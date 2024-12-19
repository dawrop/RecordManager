package infrastructure.tables

import infrastructure.model.RecordsRow
import slick.jdbc.PostgresProfile.api._

import java.time.LocalDateTime

class RecordsTable(tag: Tag) extends Table[RecordsRow](tag, "records") {
  def id          = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name        = column[String]("name")
  def phoneNumber = column[String]("phone_number")
  def amount      = column[BigDecimal]("amount")
  def processedAt = column[Option[LocalDateTime]]("processed_at")
  def createdAt   = column[LocalDateTime]("created_at")

  override def * = (id.?, name, phoneNumber, amount, processedAt, createdAt) <> (RecordsRow.tupled, RecordsRow.unapply)
}

object RecordsTable {
  val records = TableQuery[RecordsTable]
}
